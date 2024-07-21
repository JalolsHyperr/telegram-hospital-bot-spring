package com.example.telegram_hospital_bot.service;

import com.example.telegram_hospital_bot.model.User;
import com.example.telegram_hospital_bot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> getUserByTelegramId(long telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }
}
