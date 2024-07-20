package com.example.telegram_hospital_bot.service;

import com.example.telegram_hospital_bot.model.Appointment;
import com.example.telegram_hospital_bot.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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

    public List<Appointment> getAllAppointmentsByDate(
            Date dateFrom, Date dateTo
    ) {
        return appointmentRepository.findByRegisteredDateBetween(dateFrom, dateTo);
    }

    public void cancelAppointment(Long appointmentId) {
        appointmentRepository.deleteById(appointmentId);
    }
}
