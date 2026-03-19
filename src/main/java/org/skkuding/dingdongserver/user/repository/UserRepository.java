package org.skkuding.dingdongserver.user.repository;

import org.skkuding.dingdongserver.auth.domain.AuthProvider;
import org.skkuding.dingdongserver.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndProviderSubject(AuthProvider provider, String providerSubject);
}
