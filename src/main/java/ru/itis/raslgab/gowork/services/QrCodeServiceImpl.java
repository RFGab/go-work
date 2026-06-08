package ru.itis.raslgab.gowork.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.itis.raslgab.gowork.dto.BookingMailDto;

@Service
@RequiredArgsConstructor
public class QrCodeServiceImpl implements QrCodeService {
    private final RestTemplate restTemplate;

    @Override
    public byte[] generateBookingQr(BookingMailDto booking) {
        String qrData = """
                Бронь GoWork
                Арендатор: %s
                Фото профиля: %s
                Комната: %s
                Организация: %s
                С: %s
                До: %s
                """.formatted(
                booking.getRenterFullName(),
                booking.getRenterProfilePhoto(),
                booking.getRoomName(),
                booking.getOrganizationName(),
                booking.getTimeStart(),
                booking.getTimeFinish()
        );

        String url = UriComponentsBuilder
                .fromUriString("https://api.qrserver.com/v1/create-qr-code/")
                .queryParam("size", "300x300")
                .queryParam("data", qrData)
                .build()
                .encode()
                .toUriString();
        return restTemplate.getForObject(url, byte[].class);
    }
}
