package ru.itis.raslgab.gowork.controllers.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import ru.itis.raslgab.gowork.forms.BookingApprovalForm;
import ru.itis.raslgab.gowork.services.BookingApprovalService;

@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingDecisionController {
    private final BookingApprovalService bookingApprovalService;

    @GetMapping("/{bookingId}/decision")
    public String decisionForm(@PathVariable Long bookingId,
                               @RequestParam String action,
                               Model model) {
        addDecisionAttributes(bookingId, action, model);
        if (!model.containsAttribute("approvalForm")) {
            model.addAttribute("approvalForm", BookingApprovalForm.builder().build());
        }
        return "bookings/decision";
    }

    @PostMapping("/{bookingId}/decision")
    public String confirmDecision(@PathVariable Long bookingId,
                                  @RequestParam String action,
                                  @Valid @ModelAttribute("approvalForm") BookingApprovalForm form,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addDecisionAttributes(bookingId, action, model);
            return "bookings/decision";
        }

        try {
            bookingApprovalService.confirmDecision(bookingId, action, form.getCode());
            redirectAttributes.addFlashAttribute("successMessage", "Решение по заявке сохранено");
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            bindingResult.reject("approval.invalid", e.getMessage());
            addDecisionAttributes(bookingId, action, model);
            return "bookings/decision";
        }
    }

    private void addDecisionAttributes(Long bookingId, String action, Model model) {
        model.addAttribute("decision", bookingApprovalService.getApprovalPage(bookingId, action));
    }
}
