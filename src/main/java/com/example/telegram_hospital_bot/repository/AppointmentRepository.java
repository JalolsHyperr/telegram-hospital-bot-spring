package com.example.telegram_hospital_bot.repository;

import com.example.telegram_hospital_bot.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByUserId(Long userId);

    @Query("SELECT a FROM Appointment a WHERE a.registered_date BETWEEN :dateFrom AND :dateTo")
    List<Appointment> findByRegisteredDateBetween(@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo);

    List<Appointment> findAllByDoctorId(Long doctor_id);

    List<Appointment> findAllByUserId(Long user_id);
}