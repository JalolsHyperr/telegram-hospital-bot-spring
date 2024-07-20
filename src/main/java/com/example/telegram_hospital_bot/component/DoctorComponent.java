package com.example.telegram_hospital_bot.bot;

import com.example.telegram_hospital_bot.model.Doctor;
import com.example.telegram_hospital_bot.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Optional;

@Component
public class DoctorComponent extends TelegramLongPollingBot {

    @Autowired
    private DoctorService doctorService;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String command = message.getText().split(" ")[0];
            switch (command) {
                case "/listDoctors":
                    listDoctors(message);
                    break;
                case "/getDoctor":
                    getDoctor(message);
                    break;
                case "/addDoctor":
                    addDoctor(message);
                    break;
                case "/updateDoctor":
                    updateDoctor(message);
                    break;
                case "/deleteDoctor":
                    deleteDoctor(message);
                    break;
                default:
                    sendMessage(message.getChatId(), "Unknown command");
                    break;
            }
        }
    }

    private void listDoctors(Message message) {
        List<Doctor> doctors = doctorService.getAllDoctors();
        StringBuilder response = new StringBuilder("Doctors:\n");
        for (Doctor doctor : doctors) {
            response.append(doctor.getId()).append(": ").append(doctor.getName()).append(" - ").append(doctor.getSpecialty()).append("\n");
        }
        sendMessage(message.getChatId(), response.toString());
    }

    private void getDoctor(Message message) {
        String[] parts = message.getText().split(" ");
        if (parts.length < 2) {
            sendMessage(message.getChatId(), "Usage: /getDoctor <id>");
            return;
        }
        Long id = Long.parseLong(parts[1]);
        Optional<Doctor> doctor = doctorService.getDoctorById(id);
        if (doctor.isPresent()) {
            sendMessage(message.getChatId(), "Doctor: " + doctor.get().getName() + " - " + doctor.get().getSpecialty());
        } else {
            sendMessage(message.getChatId(), "Doctor not found");
        }
    }

    private void addDoctor(Message message) {
        String[] parts = message.getText().split(" ", 3);
        if (parts.length < 3) {
            sendMessage(message.getChatId(), "Usage: /addDoctor <name> <specialty>");
            return;
        }
        Doctor doctor = new Doctor();
        doctor.setName(parts[1]);
        doctor.setSpecialty(parts[2]);
        doctorService.saveDoctor(doctor);
        sendMessage(message.getChatId(), "Doctor added successfully");
    }

    private void updateDoctor(Message message) {
        String[] parts = message.getText().split(" ", 4);
        if (parts.length < 4) {
            sendMessage(message.getChatId(), "Usage: /updateDoctor <id> <name> <specialty>");
            return;
        }
        Long id = Long.parseLong(parts[1]);
        Optional<Doctor> optionalDoctor = doctorService.getDoctorById(id);
        if (optionalDoctor.isPresent()) {
            Doctor doctor = optionalDoctor.get();
            doctor.setName(parts[2]);
            doctor.setSpecialty(parts[3]);
            doctorService.saveDoctor(doctor);
            sendMessage(message.getChatId(), "Doctor updated successfully");
        } else {
            sendMessage(message.getChatId(), "Doctor not found");
        }
    }

    private void deleteDoctor(Message message) {
        String[] parts = message.getText().split(" ");
        if (parts.length < 2) {
            sendMessage(message.getChatId(), "Usage: /deleteDoctor <id>");
            return;
        }
        Long id = Long.parseLong(parts[1]);
        doctorService.deleteDoctor(id);
        sendMessage(message.getChatId(), "Doctor deleted successfully");
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        // Return bot username
        return "YourBotUsername";
    }

    @Override
    public String getBotToken() {
        // Return bot token
        return "YourBotToken";
    }
}
