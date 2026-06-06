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
                GoWork booking
                Renter: %s
                Profile photo: %s
                Room: %s
                Organization: %s
                From: %s
                To: %s
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
                .queryParam("size", "240x240")
                .queryParam("data", qrData)
                .build()
                .encode()
                .toUriString();
        return restTemplate.getForObject(url, byte[].class);
    }
}
