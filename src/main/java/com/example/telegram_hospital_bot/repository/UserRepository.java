package com.example.telegram_hospital_bot.repository;

import com.example.telegram_hospital_bot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByTelegramId(long telegramId);
}
