package io.github.nimv1.repair.controller;

import io.github.nimv1.repair.entity.RepairOrder;
import io.github.nimv1.repair.entity.User;
import io.github.nimv1.repair.service.RepairOrderService;
import io.github.nimv1.repair.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Контроллер для работы с заявками на ремонт.
 */
@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final RepairOrderService orderService;
    private final UserService userService;

    @GetMapping
    public String listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Page<RepairOrder> orders = orderService.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        model.addAttribute("orders", orders);
        return "orders/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String newOrderForm(Model model) {
        model.addAttribute("order", new RepairOrder());
        return "orders/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String createOrder(@Valid @ModelAttribute RepairOrder order,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "orders/form";
        }
        orderService.createOrder(order);
        redirectAttributes.addFlashAttribute("success", "Заявка успешно создана");
        return "redirect:/orders";
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        RepairOrder order = orderService.findById(id)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));
        model.addAttribute("order", order);
        model.addAttribute("technicians", userService.findAllTechnicians());
        return "orders/view";
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String acceptOrder(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        User manager = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        orderService.acceptOrder(id, manager);
        redirectAttributes.addFlashAttribute("success", "Заявка принята");
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DISPATCHER')")
    public String assignTechnician(@PathVariable Long id,
                                   @RequestParam Long technicianId,
                                   RedirectAttributes redirectAttributes) {
        User technician = userService.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Техник не найден"));
        orderService.assignTechnician(id, technician);
        redirectAttributes.addFlashAttribute("success", "Техник назначен");
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/schedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DISPATCHER', 'TECHNICIAN')")
    public String scheduleVisit(@PathVariable Long id,
                                @RequestParam String scheduledAt,
                                RedirectAttributes redirectAttributes) {
        orderService.scheduleVisit(id, LocalDateTime.parse(scheduledAt));
        redirectAttributes.addFlashAttribute("success", "Визит запланирован");
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public String startRepair(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        orderService.startRepair(id);
        redirectAttributes.addFlashAttribute("success", "Ремонт начат");
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public String completeRepair(@PathVariable Long id,
                                 @RequestParam String repairNotes,
                                 @RequestParam(required = false) String partsUsed,
                                 @RequestParam BigDecimal finalCost,
                                 RedirectAttributes redirectAttributes) {
        orderService.completeRepair(id, repairNotes, partsUsed, finalCost);
        redirectAttributes.addFlashAttribute("success", "Ремонт завершён");
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String cancelOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        orderService.cancelOrder(id);
        redirectAttributes.addFlashAttribute("success", "Заявка отменена");
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/waiting-parts")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public String setWaitingParts(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        orderService.setWaitingParts(id);
        redirectAttributes.addFlashAttribute("success", "Статус изменён на 'Ожидание запчастей'");
        return "redirect:/orders/" + id;
    }
}
