package org.example.dingdongserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dingdongserver.domain.User;
import org.example.dingdongserver.dto.UserResponse;
import org.example.dingdongserver.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        log.info("GET /user/{}", id);

        User user = userService.getUser(id);

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname()
        );
    }

    @PostMapping
    public UserResponse createUser(@RequestParam String email,
                                   @RequestParam String nickname) {
        log.info("POST /user email={}, nickname={}", email, nickname);

        User user = userService.createUser(email, nickname);

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname()
        );
    }
}