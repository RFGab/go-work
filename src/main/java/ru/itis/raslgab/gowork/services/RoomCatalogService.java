package ru.itis.raslgab.gowork.services;

import org.springframework.data.domain.Page;
import ru.itis.raslgab.gowork.dto.CityOptionDto;
import ru.itis.raslgab.gowork.dto.RoomCatalogItemDto;
import ru.itis.raslgab.gowork.forms.RoomCatalogFilterForm;

import java.util.List;

public interface RoomCatalogService {
    Page<RoomCatalogItemDto> getCatalog(RoomCatalogFilterForm filter);

    List<CityOptionDto> getCityOptions();
}
