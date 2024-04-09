package com.midas.app.repositories;

import com.midas.app.models.Account;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, UUID> {
  Optional<Account> findByEmail(String email);
}
