package ru.itis.raslgab.gowork.services;

import org.springframework.data.domain.Sort;
import ru.itis.raslgab.gowork.dto.AdminPageDto;
import ru.itis.raslgab.gowork.forms.admin.AdminEntityForm;

import java.util.Map;

public interface AdminService {
    Map<String, Long> getDashboardCounts();

    AdminPageDto getPage(String entity, Integer page, Integer size, String sort, Sort.Direction direction);

    void create(String entity, AdminEntityForm form);

    void update(String entity, Long id, AdminEntityForm form);

    void delete(String entity, Long id, Long currentUserId);
}
