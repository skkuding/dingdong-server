package org.example.dingdongserver.repository;

import org.example.dingdongserver.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}