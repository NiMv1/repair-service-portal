package io.github.nimv1.repair.controller;

import io.github.nimv1.repair.entity.RepairOrder;
import io.github.nimv1.repair.entity.User;
import io.github.nimv1.repair.service.RepairOrderService;
import io.github.nimv1.repair.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер главной страницы (дашборда).
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final RepairOrderService orderService;
    private final UserService userService;

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        model.addAttribute("user", user);
        model.addAttribute("newOrdersCount", orderService.countByStatus(RepairOrder.OrderStatus.NEW));
        model.addAttribute("inProgressCount", orderService.countByStatus(RepairOrder.OrderStatus.IN_PROGRESS));
        model.addAttribute("completedCount", orderService.countByStatus(RepairOrder.OrderStatus.COMPLETED));

        // Для техника показываем его активные заявки
        if (user.getRole() == User.Role.TECHNICIAN) {
            model.addAttribute("myOrders", orderService.findActiveOrdersForTechnician(user.getId()));
        }

        // Для менеджера показываем новые заявки
        if (user.getRole() == User.Role.MANAGER || user.getRole() == User.Role.ADMIN) {
            model.addAttribute("newOrders", orderService.findNewOrders());
        }

        return "dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
