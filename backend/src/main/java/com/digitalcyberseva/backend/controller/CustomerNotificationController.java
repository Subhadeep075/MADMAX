package com.digitalcyberseva.backend.controller;

import com.digitalcyberseva.backend.dto.ApiMessage;
import com.digitalcyberseva.backend.dto.CustomerNotificationResponse;
import com.digitalcyberseva.backend.entity.User;
import com.digitalcyberseva.backend.service.CurrentUserService;
import com.digitalcyberseva.backend.service.CustomerNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class CustomerNotificationController {

    private final CustomerNotificationService customerNotificationService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public List<CustomerNotificationResponse> getMyNotifications(@RequestParam(defaultValue = "20") int limit) {
        User currentUser = currentUserService.getCurrentUser();
        return customerNotificationService.getUserNotifications(currentUser.getId(), limit);
    }

    @PutMapping("/{id}/read")
    public ApiMessage markAsRead(@PathVariable Long id) {
        User currentUser = currentUserService.getCurrentUser();
        return customerNotificationService.markAsRead(currentUser.getId(), id);
    }
}
