package com.example.telegram_hospital_bot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String name;
    private String contact_info;
    private long chat_id;

    public User(
            String username, String name, String contactInfo, long chatId
    ) {
        this.username = username;
        this.name = name;
        this.contact_info = contactInfo;
        this.chat_id = chatId;
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

    public String getContactInfo() {
        return contact_info;
    }

    public void setContactInfo(String contactInfo) {
        this.contact_info = contactInfo;
    }

    public long getChatId() {
        return chat_id;
    }

    public void setChatId(long chatId) {
        this.chat_id = chatId;
    }
}
