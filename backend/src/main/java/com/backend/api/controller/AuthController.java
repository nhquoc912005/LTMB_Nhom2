package com.backend.api.controller;

import com.backend.api.entity.Account;
import com.backend.api.repository.AccountRepository;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AccountRepository repository;

    public AuthController(AccountRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Optional<Account> accountOpt = repository.findByUsername(request.getUsername());
        Map<String, Object> response = new HashMap<>();

        if (accountOpt.isPresent() && accountOpt.get().getPassword().equals(request.getPassword())) {
            response.put("success", true);
            response.put("message", "Đăng nhập thành công");
            response.put("username", accountOpt.get().getFullName());
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Tài khoản hoặc mật khẩu không chính xác");
            return ResponseEntity.status(401).body(response);
        }
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}
