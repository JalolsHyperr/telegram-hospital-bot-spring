package com.example.telegram_hospital_bot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String specialty;
    private LocalTime startTime;
    private LocalTime endTime;

    @OneToMany(mappedBy = "doctor")
    private List<Appointment> appointments;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    public List<List<InlineKeyboardButton>> getWorkingDays() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DayOfWeek currentDayOfWeek = today.getDayOfWeek();
        DayOfWeek startOfWeek = DayOfWeek.MONDAY;
        int daysToStartOfWeek = (currentDayOfWeek.getValue() + 6) % 7;

        for (int i = 0; i < 5; i++) {
            LocalDate dayDate = today.plusDays((startOfWeek.getValue() + i - daysToStartOfWeek) % 7);
            String buttonText = dayDate.getDayOfWeek().toString();
            String dateStr = dayDate.toString();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(buttonText);
            button.setCallbackData("show_doctor_free_hours:doctorId=" + this.id + ":date=" + dateStr);
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            keyboard.add(row);
        }

        return keyboard;
    }

    public List<List<InlineKeyboardButton>> getFreeHours(String dateStr) {
        List<LocalTime> bookedHours = appointments.stream()
                .map(Appointment::getHour)
                .collect(Collectors.toList());

        LocalTime currentTime = this.getStartTime();
        LocalTime endTime = this.getEndTime();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        while (currentTime.isBefore(endTime)) {
            if (!bookedHours.contains(currentTime)) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(currentTime.toString());
                button.setCallbackData("create_appointment:doctorId=" + this.id + ":date=" + dateStr + ":hour=" + currentTime.toString());
                List<InlineKeyboardButton> row = new ArrayList<>();
                row.add(button);
                keyboard.add(row);
            }
            currentTime = currentTime.plusHours(1);  // Assuming 1-hour slots
        }

        return keyboard;
    }
}
