package ru.itis.raslgab.gowork.controllers.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.itis.raslgab.gowork.dto.OrganizationDetailsDto;
import ru.itis.raslgab.gowork.forms.OrganizationCreateForm;
import ru.itis.raslgab.gowork.forms.OrganizationUpdateForm;
import ru.itis.raslgab.gowork.models.enums.OrganizationStatus;
import ru.itis.raslgab.gowork.security.UserDetailsImpl;
import ru.itis.raslgab.gowork.services.OrganizationService;
import ru.itis.raslgab.gowork.services.UserActionLogService;

@Controller
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService organizationService;
    private final UserActionLogService userActionLogService;

    @GetMapping
    public String catalog(@AuthenticationPrincipal UserDetailsImpl userDetails,
                          @RequestParam(required = false) Long cityId,
                          Model model) {
        model.addAttribute("organizations", organizationService.getCatalog(cityId));
        model.addAttribute("cities", organizationService.getCityOptions());
        model.addAttribute("selectedCityId", cityId);
        userActionLogService.log(userDetails.getUserId(), "ORGANIZATION_CATALOG_OPEN", "cityId=" + cityId);
        return "organizations/catalog";
    }

    @GetMapping("/new")
    public String createForm(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        userActionLogService.log(userDetails.getUserId(), "ORGANIZATION_CREATE_OPEN", "Organization create page opened");
        if (!model.containsAttribute("organizationForm")) {
            model.addAttribute("organizationForm", OrganizationCreateForm.builder().build());
        }
        return "organizations/new";
    }

    @PostMapping
    public String create(@AuthenticationPrincipal UserDetailsImpl userDetails,
                         @Valid @ModelAttribute("organizationForm") OrganizationCreateForm form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        Long userId = userDetails.getUserId();

        if (bindingResult.hasErrors()) {
            userActionLogService.log(userId, "ORGANIZATION_CREATE_FAILED", "Validation errors");
            return "organizations/new";
        }

        Long organizationId = organizationService.createOrganization(userId, form);
        userActionLogService.log(userId, "ORGANIZATION_CREATE_SUCCESS", "organizationId=" + organizationId);
        redirectAttributes.addFlashAttribute("successMessage", "Организация создана");
        return "redirect:/organizations/" + organizationId;
    }

    @GetMapping("/{organizationId}")
    public String show(@AuthenticationPrincipal UserDetailsImpl userDetails,
                       @PathVariable Long organizationId,
                       Model model) {
        addOrganizationPageAttributes(userDetails, organizationId, model);
        userActionLogService.log(userDetails.getUserId(), "ORGANIZATION_OPEN", "organizationId=" + organizationId);
        return "organizations/show";
    }

    @PostMapping("/{organizationId}")
    public String update(@AuthenticationPrincipal UserDetailsImpl userDetails,
                         @PathVariable Long organizationId,
                         @Valid @ModelAttribute("organizationForm") OrganizationUpdateForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long userId = userDetails.getUserId();

        if (bindingResult.hasErrors()) {
            addOrganizationPageAttributes(userDetails, organizationId, model);
            userActionLogService.log(userId, "ORGANIZATION_UPDATE_FAILED", "organizationId=" + organizationId + ", validation errors");
            return "organizations/show";
        }

        organizationService.updateOrganization(organizationId, userId, form);
        userActionLogService.log(userId, "ORGANIZATION_UPDATE_SUCCESS", "organizationId=" + organizationId);
        redirectAttributes.addFlashAttribute("successMessage", "Организация обновлена");
        return "redirect:/organizations/" + organizationId;
    }

    @PostMapping("/{organizationId}/status")
    public String updateStatus(@AuthenticationPrincipal UserDetailsImpl userDetails,
                               @PathVariable Long organizationId,
                               @RequestParam OrganizationStatus status,
                               RedirectAttributes redirectAttributes) {
        Long userId = userDetails.getUserId();
        organizationService.updateStatus(organizationId, userId, userDetails.getUser().getRole(), status);
        userActionLogService.log(userId, "ORGANIZATION_STATUS_UPDATE", "organizationId=" + organizationId + ", status=" + status);
        redirectAttributes.addFlashAttribute("successMessage", "Статус организации обновлен");
        return "redirect:/organizations/" + organizationId;
    }

    @PostMapping("/{organizationId}/delete")
    public String delete(@AuthenticationPrincipal UserDetailsImpl userDetails,
                         @PathVariable Long organizationId,
                         RedirectAttributes redirectAttributes) {
        Long userId = userDetails.getUserId();

        try {
            organizationService.deleteOrganization(organizationId, userId, userDetails.getUser().getRole());
            userActionLogService.log(userId, "ORGANIZATION_DELETE_SUCCESS", "organizationId=" + organizationId);
            redirectAttributes.addFlashAttribute("successMessage", "Организация удалена");
            return "redirect:/profile";
        } catch (IllegalStateException e) {
            userActionLogService.log(userId, "ORGANIZATION_DELETE_FAILED", "organizationId=" + organizationId + ", " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/organizations/" + organizationId;
        }
    }

    private void addOrganizationPageAttributes(UserDetailsImpl userDetails, Long organizationId, Model model) {
        OrganizationDetailsDto organization = organizationService.getOrganization(
                organizationId,
                userDetails.getUserId(),
                userDetails.getUser().getRole()
        );

        model.addAttribute("organization", organization);
        model.addAttribute("rooms", organizationService.getRooms(organizationId));
        model.addAttribute("statuses", OrganizationStatus.values());

        if (organization.isOwner() && !model.containsAttribute("organizationForm")) {
            model.addAttribute("organizationForm", organizationService.getUpdateForm(organizationId, userDetails.getUserId()));
        }
    }
}
