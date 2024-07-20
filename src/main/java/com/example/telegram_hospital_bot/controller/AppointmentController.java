package com.example.telegram_hospital_bot.controller;

import com.example.telegram_hospital_bot.model.Appointment;
import com.example.telegram_hospital_bot.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/byDate")
    public List<Appointment> getAllAppointmentsByDate(
            @RequestParam("dateFrom") Date dateFrom,
            @RequestParam("dateTo") Date dateTo
    ) {
        return appointmentService.getAllAppointmentsByDate(dateFrom, dateTo);
    }

}
