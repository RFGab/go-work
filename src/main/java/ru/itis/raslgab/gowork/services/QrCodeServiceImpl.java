package ru.itis.raslgab.gowork.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.itis.raslgab.gowork.dto.BookingMailDto;

import java.net.URI;

@Service
@Slf4j
@RequiredArgsConstructor
public class QrCodeServiceImpl implements QrCodeService {
    private static final int QR_REQUEST_ATTEMPTS = 2;

    private final RestTemplate restTemplate;

    @Override
    public byte[] generateBookingQr(BookingMailDto booking) {
        String qrData = buildQrData(booking);

        URI uri = UriComponentsBuilder
                .fromUriString("https://api.qrserver.com/v1/create-qr-code/")
                .queryParam("size", "300x300")
                .queryParam("format", "png")
                .queryParam("charset-source", "UTF-8")
                .queryParam("charset-target", "UTF-8")
                .queryParam("ecc", "M")
                .queryParam("data", qrData)
                .build()
                .encode()
                .toUri();

        log.debug("QR request for bookingId={} has urlLength={}, dataLength={}",
                booking.getBookingId(), uri.toString().length(), qrData.length());

        for (int attempt = 1; attempt <= QR_REQUEST_ATTEMPTS; attempt++) {
            try {
                ResponseEntity<byte[]> response = restTemplate.getForEntity(uri, byte[].class);
                byte[] body = response.getBody();
                if (response.getStatusCode().is2xxSuccessful() && body != null && body.length > 0) {
                    return body;
                }
                log.warn("QR service returned empty response for bookingId={}, attempt={}", booking.getBookingId(), attempt);
            } catch (RestClientException e) {
                log.warn("QR service is unavailable for bookingId={}, attempt={}: {}", booking.getBookingId(), attempt, e.getMessage());
            }
        }
        return new byte[0];
    }

    private String buildQrData(BookingMailDto booking) {
        return """
                GoWork booking #%s
                renter=%s
                photo=%s
                room=%s
                from=%s
                to=%s
                """.formatted(
                booking.getBookingId(),
                value(booking.getRenterFullName()),
                value(booking.getRenterProfilePhoto()),
                value(booking.getRoomName()),
                booking.getTimeStart(),
                booking.getTimeFinish()
        ).trim();
    }

    private String value(String text) {
        return text == null || text.isBlank() ? "-" : text.trim();
    }
}
