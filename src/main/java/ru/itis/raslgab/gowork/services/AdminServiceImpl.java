package ru.itis.raslgab.gowork.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.raslgab.gowork.dto.AdminFieldDto;
import ru.itis.raslgab.gowork.dto.AdminOptionDto;
import ru.itis.raslgab.gowork.dto.AdminPageDto;
import ru.itis.raslgab.gowork.dto.AdminRowDto;
import ru.itis.raslgab.gowork.forms.admin.AdminEntityForm;
import ru.itis.raslgab.gowork.models.Booking;
import ru.itis.raslgab.gowork.models.City;
import ru.itis.raslgab.gowork.models.Option;
import ru.itis.raslgab.gowork.models.Organization;
import ru.itis.raslgab.gowork.models.Review;
import ru.itis.raslgab.gowork.models.Room;
import ru.itis.raslgab.gowork.models.User;
import ru.itis.raslgab.gowork.models.enums.BookingStatus;
import ru.itis.raslgab.gowork.models.enums.OptionCategory;
import ru.itis.raslgab.gowork.models.enums.OrganizationStatus;
import ru.itis.raslgab.gowork.models.enums.RoleEnum;
import ru.itis.raslgab.gowork.models.enums.RoomStatus;
import ru.itis.raslgab.gowork.repositories.BookingRepo;
import ru.itis.raslgab.gowork.repositories.CityRepo;
import ru.itis.raslgab.gowork.repositories.OptionRepo;
import ru.itis.raslgab.gowork.repositories.OrganizationRepo;
import ru.itis.raslgab.gowork.repositories.ReviewRepo;
import ru.itis.raslgab.gowork.repositories.RoomRepo;
import ru.itis.raslgab.gowork.repositories.UserRepo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    private final UserRepo userRepo;
    private final OrganizationRepo organizationRepo;
    private final RoomRepo roomRepo;
    private final BookingRepo bookingRepo;
    private final CityRepo cityRepo;
    private final OptionRepo optionRepo;
    private final ReviewRepo reviewRepo;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getDashboardCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("users", userRepo.count());
        counts.put("organizations", organizationRepo.count());
        counts.put("rooms", roomRepo.count());
        counts.put("bookings", bookingRepo.count());
        counts.put("cities", cityRepo.count());
        counts.put("options", optionRepo.count());
        counts.put("reviews", reviewRepo.count());
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminPageDto getPage(String entity, Integer page, Integer size, String sort, Sort.Direction direction) {
        PageRequest pageRequest = PageRequest.of(safePage(page), safeSize(size), Sort.by(direction == null ? Sort.Direction.ASC : direction, safeSort(entity, sort)));
        return switch (entity) {
            case "users" -> AdminPageDto.builder()
                    .entity(entity)
                    .title("Пользователи")
                    .canCreate(false)
                    .fields(userFields())
                    .rows(userRepo.findAll(pageRequest).map(this::userRow))
                    .build();
            case "organizations" -> AdminPageDto.builder()
                    .entity(entity)
                    .title("Организации")
                    .canCreate(true)
                    .fields(organizationFields())
                    .rows(organizationRepo.findAll(pageRequest).map(this::organizationRow))
                    .build();
            case "rooms" -> AdminPageDto.builder()
                    .entity(entity)
                    .title("Комнаты")
                    .canCreate(true)
                    .fields(roomFields())
                    .rows(roomRepo.findAll(pageRequest).map(this::roomRow))
                    .build();
            case "bookings" -> AdminPageDto.builder()
                    .entity(entity)
                    .title("Брони")
                    .canCreate(true)
                    .fields(bookingFields())
                    .rows(bookingRepo.findAll(pageRequest).map(this::bookingRow))
                    .build();
            case "cities" -> AdminPageDto.builder()
                    .entity(entity)
                    .title("Города")
                    .canCreate(true)
                    .fields(cityFields())
                    .rows(cityRepo.findAll(pageRequest).map(this::cityRow))
                    .build();
            case "options" -> AdminPageDto.builder()
                    .entity(entity)
                    .title("Опции")
                    .canCreate(true)
                    .fields(optionFields())
                    .rows(optionRepo.findAll(pageRequest).map(this::optionRow))
                    .build();
            case "reviews" -> AdminPageDto.builder()
                    .entity(entity)
                    .title("Отзывы")
                    .canCreate(true)
                    .fields(reviewFields())
                    .rows(reviewRepo.findAll(pageRequest).map(this::reviewRow))
                    .build();
            default -> throw new IllegalArgumentException("Неизвестный раздел админки");
        };
    }

    @Override
    @Transactional
    public void create(String entity, AdminEntityForm form) {
        switch (entity) {
            case "users" -> throw new IllegalArgumentException("Пользователи должны регистрироваться сами");
            case "organizations" -> organizationRepo.save(Organization.builder()
                    .name(required(form.getName()))
                    .description(trimToNull(form.getDescription()))
                    .city(getOrCreateCity(form.getCityName()))
                    .contactEmail(trimToNull(form.getContactEmail()))
                    .contactPhone(trimToNull(form.getContactPhone()))
                    .owner(findUser(form.getOwnerId()))
                    .status(form.getOrganizationStatus() == null ? OrganizationStatus.ACTIVE : form.getOrganizationStatus())
                    .build());
            case "rooms" -> roomRepo.save(Room.builder()
                    .name(required(form.getName()))
                    .description(trimToNull(form.getDescription()))
                    .peopleCapacity(form.getPeopleCapacity())
                    .pricePerHour(form.getPricePerHour())
                    .organization(findOrganization(form.getOrganizationId()))
                    .dayStart(form.getDayStart() == null ? 9 : form.getDayStart())
                    .dayEnd(form.getDayEnd() == null ? 17 : form.getDayEnd())
                    .status(form.getRoomStatus() == null ? RoomStatus.AVAILABLE : form.getRoomStatus())
                    .build());
            case "bookings" -> bookingRepo.save(Booking.builder()
                    .room(findRoom(form.getRoomId()))
                    .renter(findUser(form.getRenterId()))
                    .timeStart(form.getTimeStart())
                    .timeFinish(form.getTimeFinish())
                    .numOfPeople(form.getNumOfPeople())
                    .comment(trimToNull(form.getComment()))
                    .status(form.getBookingStatus() == null ? BookingStatus.PENDING : form.getBookingStatus())
                    .build());
            case "cities" -> cityRepo.save(City.builder()
                    .name(required(form.getName()))
                    .utc(form.getUtc() == null ? 3 : form.getUtc())
                    .build());
            case "options" -> optionRepo.save(Option.builder()
                    .name(required(form.getName()))
                    .category(form.getCategory() == null ? OptionCategory.OFFICE_TOOLS : form.getCategory())
                    .build());
            case "reviews" -> reviewRepo.save(Review.builder()
                    .author(findUser(form.getAuthorId()))
                    .organization(findOrganization(form.getOrganizationId()))
                    .rating(form.getRating())
                    .text(required(form.getText()))
                    .build());
            default -> throw new IllegalArgumentException("Неизвестный раздел админки");
        }
    }

    @Override
    @Transactional
    public void update(String entity, Long id, AdminEntityForm form) {
        switch (entity) {
            case "users" -> updateUser(id, form);
            case "organizations" -> updateOrganization(id, form);
            case "rooms" -> updateRoom(id, form);
            case "bookings" -> updateBooking(id, form);
            case "cities" -> updateCity(id, form);
            case "options" -> updateOption(id, form);
            case "reviews" -> updateReview(id, form);
            default -> throw new IllegalArgumentException("Неизвестный раздел админки");
        }
    }

    @Override
    @Transactional
    public void delete(String entity, Long id, Long currentUserId) {
        switch (entity) {
            case "users" -> {
                if (id.equals(currentUserId)) {
                    throw new AccessDeniedException("Нельзя удалить самого себя");
                }
                userRepo.deleteById(id);
            }
            case "organizations" -> organizationRepo.deleteById(id);
            case "rooms" -> roomRepo.deleteById(id);
            case "bookings" -> bookingRepo.deleteById(id);
            case "cities" -> cityRepo.deleteById(id);
            case "options" -> optionRepo.deleteById(id);
            case "reviews" -> reviewRepo.deleteById(id);
            default -> throw new IllegalArgumentException("Неизвестный раздел админки");
        }
    }

    private void updateUser(Long id, AdminEntityForm form) {
        User user = findUser(id);
        user.setFirstName(required(form.getFirstName()));
        user.setLastName(required(form.getLastName()));
        user.setEmail(required(form.getEmail()).toLowerCase());
        user.setPhone(required(form.getPhone()));
        user.setRole(form.getRole() == null ? RoleEnum.USER : form.getRole());
        user.setBlocked(Boolean.TRUE.equals(form.getBlocked()));
        user.setConfirmed(Boolean.TRUE.equals(form.getConfirmed()));
    }

    private void updateOrganization(Long id, AdminEntityForm form) {
        Organization organization = findOrganization(id);
        organization.setName(required(form.getName()));
        organization.setDescription(trimToNull(form.getDescription()));
        organization.setCity(getOrCreateCity(form.getCityName()));
        organization.setContactEmail(trimToNull(form.getContactEmail()));
        organization.setContactPhone(trimToNull(form.getContactPhone()));
        organization.setOwner(findUser(form.getOwnerId()));
        organization.setStatus(form.getOrganizationStatus() == null ? OrganizationStatus.ACTIVE : form.getOrganizationStatus());
    }

    private void updateRoom(Long id, AdminEntityForm form) {
        Room room = findRoom(id);
        room.setName(required(form.getName()));
        room.setDescription(trimToNull(form.getDescription()));
        room.setPeopleCapacity(form.getPeopleCapacity());
        room.setPricePerHour(form.getPricePerHour());
        room.setOrganization(findOrganization(form.getOrganizationId()));
        room.setDayStart(form.getDayStart() == null ? 9 : form.getDayStart());
        room.setDayEnd(form.getDayEnd() == null ? 17 : form.getDayEnd());
        room.setStatus(form.getRoomStatus() == null ? RoomStatus.AVAILABLE : form.getRoomStatus());
    }

    private void updateBooking(Long id, AdminEntityForm form) {
        Booking booking = findBooking(id);
        booking.setRoom(findRoom(form.getRoomId()));
        booking.setRenter(findUser(form.getRenterId()));
        booking.setTimeStart(form.getTimeStart());
        booking.setTimeFinish(form.getTimeFinish());
        booking.setNumOfPeople(form.getNumOfPeople());
        booking.setComment(trimToNull(form.getComment()));
        booking.setStatus(form.getBookingStatus() == null ? BookingStatus.PENDING : form.getBookingStatus());
    }

    private void updateCity(Long id, AdminEntityForm form) {
        City city = findCity(id);
        city.setName(required(form.getName()));
        city.setUtc(form.getUtc() == null ? 3 : form.getUtc());
    }

    private void updateOption(Long id, AdminEntityForm form) {
        Option option = findOption(id);
        option.setName(required(form.getName()));
        option.setCategory(form.getCategory() == null ? OptionCategory.OFFICE_TOOLS : form.getCategory());
    }

    private void updateReview(Long id, AdminEntityForm form) {
        Review review = findReview(id);
        review.setAuthor(findUser(form.getAuthorId()));
        review.setOrganization(findOrganization(form.getOrganizationId()));
        review.setRating(form.getRating());
        review.setText(required(form.getText()));
    }

    private List<AdminFieldDto> userFields() {
        return List.of(
                field("firstName", "Имя", "text"),
                field("lastName", "Фамилия", "text"),
                field("email", "Email", "email"),
                field("phone", "Телефон", "text"),
                select("role", "Роль", enumOptions(RoleEnum.values())),
                field("blocked", "Заблокирован", "checkbox"),
                field("confirmed", "Подтвержден", "checkbox")
        );
    }

    private List<AdminFieldDto> organizationFields() {
        return List.of(
                field("name", "Название", "text"),
                field("description", "Описание", "textarea"),
                field("cityName", "Город", "text"),
                field("contactEmail", "Email", "email"),
                field("contactPhone", "Телефон", "text"),
                select("ownerId", "Владелец", userOptions()),
                select("organizationStatus", "Статус", enumOptions(OrganizationStatus.values()))
        );
    }

    private List<AdminFieldDto> roomFields() {
        return List.of(
                field("name", "Название", "text"),
                field("description", "Описание", "textarea"),
                field("peopleCapacity", "Вместимость", "number"),
                field("pricePerHour", "Цена/час", "number"),
                field("dayStart", "Начало дня", "number"),
                field("dayEnd", "Конец дня", "number"),
                select("organizationId", "Организация", organizationOptions()),
                select("roomStatus", "Статус", enumOptions(RoomStatus.values()))
        );
    }

    private List<AdminFieldDto> bookingFields() {
        return List.of(
                select("roomId", "Комната", roomOptions()),
                select("renterId", "Арендатор", userOptions()),
                field("timeStart", "Начало", "datetime-local"),
                field("timeFinish", "Конец", "datetime-local"),
                field("numOfPeople", "Людей", "number"),
                field("comment", "Комментарий", "textarea"),
                select("bookingStatus", "Статус", enumOptions(BookingStatus.values()))
        );
    }

    private List<AdminFieldDto> cityFields() {
        return List.of(
                field("name", "Название", "text"),
                field("utc", "UTC", "number")
        );
    }

    private List<AdminFieldDto> optionFields() {
        return List.of(
                field("name", "Название", "text"),
                select("category", "Категория", enumOptions(OptionCategory.values()))
        );
    }

    private List<AdminFieldDto> reviewFields() {
        return List.of(
                select("authorId", "Автор", userOptions()),
                select("organizationId", "Организация", organizationOptions()),
                field("rating", "Оценка", "number"),
                field("text", "Текст", "textarea")
        );
    }

    private AdminRowDto userRow(User user) {
        return row(user.getId(), Map.of(
                "firstName", value(user.getFirstName()),
                "lastName", value(user.getLastName()),
                "email", value(user.getEmail()),
                "phone", value(user.getPhone()),
                "role", value(user.getRole()),
                "blocked", String.valueOf(user.isBlocked()),
                "confirmed", String.valueOf(user.isConfirmed())
        ));
    }

    private AdminRowDto organizationRow(Organization organization) {
        return row(organization.getId(), Map.of(
                "name", value(organization.getName()),
                "description", value(organization.getDescription()),
                "cityName", organization.getCity() == null ? "" : organization.getCity().getName(),
                "contactEmail", value(organization.getContactEmail()),
                "contactPhone", value(organization.getContactPhone()),
                "ownerId", organization.getOwner() == null ? "" : String.valueOf(organization.getOwner().getId()),
                "organizationStatus", value(organization.getStatus())
        ));
    }

    private AdminRowDto roomRow(Room room) {
        return row(room.getId(), Map.of(
                "name", value(room.getName()),
                "description", value(room.getDescription()),
                "peopleCapacity", value(room.getPeopleCapacity()),
                "pricePerHour", value(room.getPricePerHour()),
                "dayStart", value(room.getDayStart()),
                "dayEnd", value(room.getDayEnd()),
                "organizationId", room.getOrganization() == null ? "" : String.valueOf(room.getOrganization().getId()),
                "roomStatus", value(room.getStatus())
        ));
    }

    private AdminRowDto bookingRow(Booking booking) {
        return row(booking.getId(), Map.of(
                "roomId", booking.getRoom() == null ? "" : String.valueOf(booking.getRoom().getId()),
                "renterId", booking.getRenter() == null ? "" : String.valueOf(booking.getRenter().getId()),
                "timeStart", value(booking.getTimeStart()),
                "timeFinish", value(booking.getTimeFinish()),
                "numOfPeople", value(booking.getNumOfPeople()),
                "comment", value(booking.getComment()),
                "bookingStatus", value(booking.getStatus())
        ));
    }

    private AdminRowDto cityRow(City city) {
        return row(city.getId(), Map.of(
                "name", value(city.getName()),
                "utc", value(city.getUtc())
        ));
    }

    private AdminRowDto optionRow(Option option) {
        return row(option.getId(), Map.of(
                "name", value(option.getName()),
                "category", value(option.getCategory())
        ));
    }

    private AdminRowDto reviewRow(Review review) {
        return row(review.getId(), Map.of(
                "authorId", review.getAuthor() == null ? "" : String.valueOf(review.getAuthor().getId()),
                "organizationId", review.getOrganization() == null ? "" : String.valueOf(review.getOrganization().getId()),
                "rating", value(review.getRating()),
                "text", value(review.getText())
        ));
    }

    private AdminRowDto row(Long id, Map<String, String> values) {
        return AdminRowDto.builder().id(id).values(values).build();
    }

    private AdminFieldDto field(String name, String label, String type) {
        return AdminFieldDto.builder().name(name).label(label).type(type).build();
    }

    private AdminFieldDto select(String name, String label, List<AdminOptionDto> options) {
        return AdminFieldDto.builder().name(name).label(label).type("select").options(options).build();
    }

    private List<AdminOptionDto> userOptions() {
        return userRepo.findAll(Sort.by("id")).stream()
                .map(user -> option(user.getId(), user.getId() + " - " + user.getEmail()))
                .toList();
    }

    private List<AdminOptionDto> organizationOptions() {
        return organizationRepo.findAll(Sort.by("id")).stream()
                .map(organization -> option(organization.getId(), organization.getId() + " - " + organization.getName()))
                .toList();
    }

    private List<AdminOptionDto> roomOptions() {
        return roomRepo.findAll(Sort.by("id")).stream()
                .map(room -> option(room.getId(), room.getId() + " - " + room.getName()))
                .toList();
    }

    private AdminOptionDto option(Long value, String label) {
        return AdminOptionDto.builder().value(String.valueOf(value)).label(label).build();
    }

    private <E extends Enum<E>> List<AdminOptionDto> enumOptions(E[] values) {
        return java.util.Arrays.stream(values)
                .map(value -> AdminOptionDto.builder().value(value.name()).label(value.name()).build())
                .toList();
    }

    private User findUser(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }

    private Organization findOrganization(Long id) {
        return organizationRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Организация не найдена"));
    }

    private Room findRoom(Long id) {
        return roomRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Комната не найдена"));
    }

    private Booking findBooking(Long id) {
        return bookingRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Бронь не найдена"));
    }

    private City findCity(Long id) {
        return cityRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Город не найден"));
    }

    private Option findOption(Long id) {
        return optionRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Опция не найдена"));
    }

    private Review findReview(Long id) {
        return reviewRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Отзыв не найден"));
    }

    private City getOrCreateCity(String cityName) {
        String name = required(cityName);
        return cityRepo.findByNameIgnoreCase(name)
                .orElseGet(() -> cityRepo.save(City.builder().name(name).utc(3).build()));
    }

    private String required(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Заполните обязательные поля");
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String value(Object value) {
        return value == null ? "" : value.toString();
    }

    private int safePage(Integer page) {
        return page == null || page < 0 ? DEFAULT_PAGE : page;
    }

    private int safeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private String safeSort(String entity, String sort) {
        if (sort == null || sort.isBlank()) {
            return "id";
        }
        List<String> allowed = switch (entity) {
            case "users" -> List.of("id", "email", "firstName", "lastName", "role");
            case "organizations" -> List.of("id", "name", "status", "contactEmail");
            case "rooms" -> List.of("id", "name", "peopleCapacity", "pricePerHour", "status", "dayStart", "dayEnd");
            case "bookings" -> List.of("id", "timeStart", "timeFinish", "status", "numOfPeople");
            case "cities", "options" -> List.of("id", "name");
            case "reviews" -> List.of("id", "rating", "createdAt");
            default -> List.of("id");
        };
        return allowed.contains(sort) ? sort : "id";
    }
}
