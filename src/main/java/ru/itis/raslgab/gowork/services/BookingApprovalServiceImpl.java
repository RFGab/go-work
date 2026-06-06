package ru.itis.raslgab.gowork.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import ru.itis.raslgab.gowork.dto.BookingApprovalPageDto;
import ru.itis.raslgab.gowork.dto.BookingMailDto;
import ru.itis.raslgab.gowork.models.Booking;
import ru.itis.raslgab.gowork.models.User;
import ru.itis.raslgab.gowork.models.enums.BookingStatus;
import ru.itis.raslgab.gowork.repositories.BookingRepo;
import ru.itis.raslgab.gowork.util.CodeGenerator;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingApprovalServiceImpl implements BookingApprovalService {
    private static final Duration CODE_TTL = Duration.ofHours(24);

    private final BookingRepo bookingRepo;
    private final StringRedisTemplate redisTemplate;
    private final MailService mailService;
    private final QrCodeService qrCodeService;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    @Override
    @Transactional(readOnly = true)
    public void sendOwnerApprovalRequest(Long bookingId) {
        Booking booking = findBooking(bookingId);
        String code = CodeGenerator.generate4Digit();
        redisTemplate.opsForValue().set(redisKey(bookingId), code, CODE_TTL);

        BookingMailDto dto = toMailDto(booking);
        dto.setApprovalCode(code);
        dto.setApproveUrl(decisionUrl(bookingId, "approve"));
        dto.setRejectUrl(decisionUrl(bookingId, "reject"));
        mailService.sendBookingApprovalRequest(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingApprovalPageDto getApprovalPage(Long bookingId, String action) {
        Booking booking = findBooking(bookingId);
        return BookingApprovalPageDto.builder()
                .bookingId(booking.getId())
                .action(normalizeAction(action))
                .actionTitle("approve".equals(normalizeAction(action)) ? "Одобрить бронь" : "Отклонить бронь")
                .renterFullName(fullName(booking.getRenter()))
                .roomName(booking.getRoom().getName())
                .organizationName(booking.getRoom().getOrganization().getName())
                .timeStart(booking.getTimeStart())
                .timeFinish(booking.getTimeFinish())
                .build();
    }

    @Override
    @Transactional
    public void confirmDecision(Long bookingId, String action, String code) {
        Booking booking = findBooking(bookingId);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("По этой заявке уже принято решение");
        }

        String savedCode = redisTemplate.opsForValue().get(redisKey(bookingId));
        if (savedCode == null || code == null || !savedCode.equals(code.trim())) {
            throw new IllegalArgumentException("Неверный код подтверждения");
        }

        BookingMailDto dto = toMailDto(booking);
        String normalizedAction = normalizeAction(action);
        if ("approve".equals(normalizedAction)) {
            booking.setStatus(BookingStatus.CONFIRMED);
            byte[] qrCode = generateQrSafely(dto);
            mailService.sendBookingApproved(dto, qrCode);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
            mailService.sendBookingRejected(dto);
        }
        redisTemplate.delete(redisKey(bookingId));
    }

    private Booking findBooking(Long bookingId) {
        return bookingRepo.findDetailsById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
    }

    private BookingMailDto toMailDto(Booking booking) {
        User renter = booking.getRenter();
        User owner = booking.getRoom().getOrganization().getOwner();
        return BookingMailDto.builder()
                .bookingId(booking.getId())
                .renterFullName(fullName(renter))
                .renterEmail(renter.getEmail())
                .renterPhone(renter.getPhone())
                .renterProfilePhoto("не указано")
                .ownerEmail(owner.getEmail())
                .roomName(booking.getRoom().getName())
                .organizationName(booking.getRoom().getOrganization().getName())
                .timeStart(booking.getTimeStart())
                .timeFinish(booking.getTimeFinish())
                .numOfPeople(booking.getNumOfPeople())
                .comment(booking.getComment())
                .build();
    }

    private String fullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }

    private String decisionUrl(Long bookingId, String action) {
        return UriComponentsBuilder.fromUriString(appBaseUrl)
                .path("/bookings/{bookingId}/decision")
                .queryParam("action", action)
                .buildAndExpand(bookingId)
                .toUriString();
    }

    private String redisKey(Long bookingId) {
        return "booking:approval:" + bookingId;
    }

    private String normalizeAction(String action) {
        if ("approve".equalsIgnoreCase(action)) {
            return "approve";
        }
        if ("reject".equalsIgnoreCase(action)) {
            return "reject";
        }
        throw new IllegalArgumentException("Unknown booking action");
    }

    private byte[] generateQrSafely(BookingMailDto dto) {
        try {
            return qrCodeService.generateBookingQr(dto);
        } catch (Exception e) {
            log.error("Failed to generate QR for bookingId={}", dto.getBookingId(), e);
            return new byte[0];
        }
    }
}
