package ru.itis.raslgab.gowork.controllers.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.itis.raslgab.gowork.dto.OrganizationDetailsDto;
import ru.itis.raslgab.gowork.forms.OrganizationCatalogFilterForm;
import ru.itis.raslgab.gowork.forms.OrganizationCreateForm;
import ru.itis.raslgab.gowork.forms.OrganizationUpdateForm;
import ru.itis.raslgab.gowork.forms.ReviewForm;
import ru.itis.raslgab.gowork.models.enums.OrganizationStatus;
import ru.itis.raslgab.gowork.models.enums.RoleEnum;
import ru.itis.raslgab.gowork.security.UserDetailsImpl;
import ru.itis.raslgab.gowork.services.OrganizationService;
import ru.itis.raslgab.gowork.services.ReviewService;
import ru.itis.raslgab.gowork.services.ReviewSecurityService;
import ru.itis.raslgab.gowork.services.UserActionLogService;

import java.util.List;

@Controller
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService organizationService;
    private final ReviewService reviewService;
    private final ReviewSecurityService reviewSecurityService;
    private final UserActionLogService userActionLogService;

    @GetMapping
    public String catalog(@AuthenticationPrincipal UserDetailsImpl userDetails,
                          @ModelAttribute("filter") OrganizationCatalogFilterForm filter,
                          Model model) {
        Page<?> organizationsPage = organizationService.getCatalog(filter);
        model.addAttribute("organizationsPage", organizationsPage);
        model.addAttribute("organizations", organizationsPage.getContent());
        model.addAttribute("cities", organizationService.getCityOptions());
        model.addAttribute("pageNumbers", getPageNumbers(organizationsPage));
        logIfAuthenticated(userDetails, "ORGANIZATION_CATALOG_OPEN", "cityId=" + filter.getCityId() + ", name=" + filter.getName());
        return "organizations/catalog";
    }

    @GetMapping("/new")
    @PreAuthorize("isAuthenticated()")
    public String createForm(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        userActionLogService.log(userDetails.getUserId(), "ORGANIZATION_CREATE_OPEN", "Organization create page opened");
        if (!model.containsAttribute("organizationForm")) {
            model.addAttribute("organizationForm", OrganizationCreateForm.builder().build());
        }
        model.addAttribute("cities", organizationService.getCityOptions());
        return "organizations/new";
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public String create(@AuthenticationPrincipal UserDetailsImpl userDetails,
                         @Valid @ModelAttribute("organizationForm") OrganizationCreateForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long userId = userDetails.getUserId();

        if (bindingResult.hasErrors()) {
            userActionLogService.log(userId, "ORGANIZATION_CREATE_FAILED", "Validation errors");
            model.addAttribute("cities", organizationService.getCityOptions());
            return "organizations/new";
        }

        Long organizationId = organizationService.createOrganization(userId, form);
        userActionLogService.log(userId, "ORGANIZATION_CREATE_SUCCESS", "organizationId=" + organizationId);
        redirectAttributes.addFlashAttribute("successMessage", "Организация создана");
        return "redirect:/organizations/" + organizationId;
    }

    @GetMapping("/{organizationId}")
    public String show(@AuthenticationPrincipal UserDetailsImpl userDetails,
                       Authentication authentication,
                       @PathVariable Long organizationId,
                       Model model) {
        addOrganizationPageAttributes(userDetails, authentication, organizationId, model);
        logIfAuthenticated(userDetails, "ORGANIZATION_OPEN", "organizationId=" + organizationId);
        return "organizations/show";
    }

    @PostMapping("/{organizationId}")
    @PreAuthorize("@organizationSecurityService.isOwner(#organizationId, authentication)")
    public String update(@AuthenticationPrincipal UserDetailsImpl userDetails,
                         Authentication authentication,
                         @PathVariable Long organizationId,
                         @Valid @ModelAttribute("organizationForm") OrganizationUpdateForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long userId = userDetails.getUserId();

        if (bindingResult.hasErrors()) {
            addOrganizationPageAttributes(userDetails, authentication, organizationId, model);
            userActionLogService.log(userId, "ORGANIZATION_UPDATE_FAILED", "organizationId=" + organizationId + ", validation errors");
            return "organizations/show";
        }

        organizationService.updateOrganization(organizationId, form);
        userActionLogService.log(userId, "ORGANIZATION_UPDATE_SUCCESS", "organizationId=" + organizationId);
        redirectAttributes.addFlashAttribute("successMessage", "Организация обновлена");
        return "redirect:/organizations/" + organizationId;
    }

    @PostMapping("/{organizationId}/status")
    @PreAuthorize("@organizationSecurityService.canManage(#organizationId, authentication)")
    public String updateStatus(@AuthenticationPrincipal UserDetailsImpl userDetails,
                               @PathVariable Long organizationId,
                               @RequestParam OrganizationStatus status,
                               RedirectAttributes redirectAttributes) {
        Long userId = userDetails.getUserId();
        organizationService.updateStatus(organizationId, status);
        userActionLogService.log(userId, "ORGANIZATION_STATUS_UPDATE", "organizationId=" + organizationId + ", status=" + status);
        redirectAttributes.addFlashAttribute("successMessage", "Статус организации обновлен");
        return "redirect:/organizations/" + organizationId;
    }

    @PostMapping("/{organizationId}/logo")
    @PreAuthorize("@organizationSecurityService.canManage(#organizationId, authentication)")
    public String updateLogo(@AuthenticationPrincipal UserDetailsImpl userDetails,
                             @PathVariable Long organizationId,
                             @RequestParam("logo") MultipartFile logo,
                             RedirectAttributes redirectAttributes) {
        Long userId = userDetails.getUserId();
        try {
            organizationService.updateLogo(organizationId, logo);
            userActionLogService.log(userId, "ORGANIZATION_LOGO_UPDATE_SUCCESS", "organizationId=" + organizationId);
            redirectAttributes.addFlashAttribute("successMessage", "Логотип организации обновлен");
        } catch (IllegalArgumentException e) {
            userActionLogService.log(userId, "ORGANIZATION_LOGO_UPDATE_FAILED", "organizationId=" + organizationId + ", " + e.getMessage());
            redirectAttributes.addFlashAttribute("logoError", e.getMessage());
        }
        return "redirect:/organizations/" + organizationId;
    }

    @PostMapping("/{organizationId}/delete")
    @PreAuthorize("@organizationSecurityService.canManage(#organizationId, authentication)")
    public String delete(@AuthenticationPrincipal UserDetailsImpl userDetails,
                         @PathVariable Long organizationId,
                         RedirectAttributes redirectAttributes) {
        Long userId = userDetails.getUserId();

        try {
            organizationService.deleteOrganization(organizationId);
            userActionLogService.log(userId, "ORGANIZATION_DELETE_SUCCESS", "organizationId=" + organizationId);
            redirectAttributes.addFlashAttribute("successMessage", "Организация удалена");
            return "redirect:/profile";
        } catch (IllegalStateException e) {
            userActionLogService.log(userId, "ORGANIZATION_DELETE_FAILED", "organizationId=" + organizationId + ", " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/organizations/" + organizationId;
        }
    }

    private void addOrganizationPageAttributes(UserDetailsImpl userDetails,
                                               Authentication authentication,
                                               Long organizationId,
                                               Model model) {
        OrganizationDetailsDto organization = organizationService.getOrganization(
                organizationId,
                currentUserId(userDetails),
                currentUserRole(userDetails)
        );

        model.addAttribute("organization", organization);
        model.addAttribute("rooms", organizationService.getRooms(organizationId));
        model.addAttribute("statuses", OrganizationStatus.values());
        model.addAttribute("reviews", reviewService.getOrganizationReviews(organizationId, currentUserId(userDetails)));
        model.addAttribute("canCreateReview", reviewSecurityService.canCreate(organizationId, authentication));
        model.addAttribute("reviewForm", ReviewForm.builder().rating(5).build());

        if (organization.isOwner() && !model.containsAttribute("organizationForm")) {
            model.addAttribute("organizationForm", organizationService.getUpdateForm(organizationId));
        }
    }

    private Long currentUserId(UserDetailsImpl userDetails) {
        return userDetails == null ? null : userDetails.getUserId();
    }

    private RoleEnum currentUserRole(UserDetailsImpl userDetails) {
        return userDetails == null ? null : userDetails.getUser().getRole();
    }

    private void logIfAuthenticated(UserDetailsImpl userDetails, String action, String details) {
        if (userDetails != null) {
            userActionLogService.log(userDetails.getUserId(), action, details);
        }
    }

    private List<Integer> getPageNumbers(Page<?> page) {
        return java.util.stream.IntStream.range(0, page.getTotalPages())
                .boxed()
                .toList();
    }
}
