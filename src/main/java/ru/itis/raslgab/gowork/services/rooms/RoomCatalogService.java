package ru.itis.raslgab.gowork.services.rooms;

import org.springframework.data.domain.Page;
import ru.itis.raslgab.gowork.dto.cities.CityOptionDto;
import ru.itis.raslgab.gowork.dto.rooms.RoomCatalogItemDto;
import ru.itis.raslgab.gowork.forms.rooms.RoomCatalogFilterForm;

import java.util.List;

public interface RoomCatalogService {
    Page<RoomCatalogItemDto> getCatalog(RoomCatalogFilterForm filter);

    List<CityOptionDto> getCityOptions();
}
