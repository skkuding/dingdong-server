package org.example.dingdongserver.service;

import lombok.RequiredArgsConstructor;
import org.example.dingdongserver.domain.User;
import org.example.dingdongserver.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(String email, String nickname) {
        User user = new User(email, nickname);
        return userRepository.save(user);
    }

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다. id=" + id));
    }
}