package com.backend.api.repository;

import com.backend.api.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    java.util.Optional<Account> findByUsername(String username);
}
