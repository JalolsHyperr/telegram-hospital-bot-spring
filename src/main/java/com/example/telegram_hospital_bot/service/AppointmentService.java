package com.example.telegram_hospital_bot.service;

import com.example.telegram_hospital_bot.model.Appointment;
import com.example.telegram_hospital_bot.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    public Appointment bookAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getUserAppointments(Long userId) {
        return appointmentRepository.findByUserId(userId);
    }

    public List<Appointment> getAllAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findByRegisteredDateBetween(date);
    }

    public List<Appointment> getAllAppointmentsByDoctorIdAndDate(Long doctorId, LocalDate date) {
        return appointmentRepository.findAllByDoctorIdAndDate(doctorId, date);
    }

    public void cancelAppointment(Long appointmentId) {
        appointmentRepository.deleteById(appointmentId);
    }
    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }
}
