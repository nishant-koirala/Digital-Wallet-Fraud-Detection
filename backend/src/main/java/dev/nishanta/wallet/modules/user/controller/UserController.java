package dev.nishanta.wallet.modules.user.controller;

import dev.nishanta.wallet.common.constant.ApiRoutes;
import dev.nishanta.wallet.modules.user.domain.User;
import dev.nishanta.wallet.modules.user.dto.PasswordUpdateRequest;
import dev.nishanta.wallet.modules.user.dto.ProfileUpdateRequest;
import dev.nishanta.wallet.modules.user.repository.UserRepository;
import dev.nishanta.wallet.modules.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public User getProfile(Authentication authentication) {
        User user = getUser(authentication);
        return userService.getUserProfile(user.getId());
    }

    @PutMapping("/profile")
    public void updateProfile(@RequestBody @Valid ProfileUpdateRequest request, Authentication authentication) {
        User user = getUser(authentication);
        userService.updateProfile(user.getId(), request);
    }

    @PutMapping("/password")
    public void changePassword(@RequestBody @Valid PasswordUpdateRequest request, Authentication authentication) {
        User user = getUser(authentication);
        userService.changePassword(user.getId(), request);
    }

    @DeleteMapping("/account")
    public void deleteAccount(Authentication authentication) {
        User user = getUser(authentication);
        userService.deleteAccount(user.getId());
    }

    private User getUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }
}
