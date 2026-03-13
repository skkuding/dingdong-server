package org.example.dingdongserver.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String nickname;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public User(String email, String nickname) {
        this.email = email;
        this.nickname = nickname;
        this.createdAt = LocalDateTime.now();
    }
}