package com.example.telegram_hospital_bot.repository;

import com.example.telegram_hospital_bot.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}
