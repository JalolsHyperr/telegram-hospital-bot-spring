package com.example.telegram_hospital_bot.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, name = "telegram_id")
    private long telegramId;

    private String username;

    private String name;

    @Column(unique = true, nullable = false, name = "chat_id")
    private long chatId;

    @OneToMany(mappedBy = "user")
    private List<Appointment> appointments;

    public User(
            long telegramId,
            long chatId,
            String username,
            String name
    ) {
        this.telegramId = telegramId;
        this.username = username;
        this.name = name;
        this.chatId = chatId;
    }

    public User() {
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(Long telegramId) {
        this.telegramId = telegramId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }
}
