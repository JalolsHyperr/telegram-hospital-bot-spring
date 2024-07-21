package com.example.telegram_hospital_bot.repository;

import com.example.telegram_hospital_bot.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByUserId(Long userId);

    @Query("SELECT a FROM Appointment a WHERE a.date = :request_date")
    List<Appointment> findByRegisteredDateBetween(@Param("request_date") LocalDate request_date);

    List<Appointment> findAllByDoctorIdAndDate(Long doctor_id, LocalDate date);

    List<Appointment> findAllByUserId(Long user_id);
}