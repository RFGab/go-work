package ru.itis.raslgab.gowork.forms.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ru.itis.raslgab.gowork.models.enums.BookingStatus;
import ru.itis.raslgab.gowork.models.enums.OptionCategory;
import ru.itis.raslgab.gowork.models.enums.OrganizationStatus;
import ru.itis.raslgab.gowork.models.enums.RoleEnum;
import ru.itis.raslgab.gowork.models.enums.RoomStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminEntityForm {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private RoleEnum role;
    private Boolean blocked;
    private Boolean confirmed;

    private String name;
    private String description;
    private String cityName;
    private String contactEmail;
    private String contactPhone;
    private Long ownerId;
    private OrganizationStatus organizationStatus;

    private Integer peopleCapacity;
    private BigDecimal pricePerHour;
    private Long organizationId;
    private RoomStatus roomStatus;
    private Integer dayStart;
    private Integer dayEnd;

    private Long roomId;
    private Long renterId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime timeStart;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime timeFinish;
    private Integer numOfPeople;
    private String comment;
    private BookingStatus bookingStatus;

    private OptionCategory category;
    private Integer utc;

    private Long authorId;
    private Integer rating;
    private String text;
}
