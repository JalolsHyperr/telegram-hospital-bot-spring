package com.example.telegram_hospital_bot.service;

import com.example.telegram_hospital_bot.model.User;
import com.example.telegram_hospital_bot.model.Doctor;
import com.example.telegram_hospital_bot.model.Appointment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TelegramBotService extends TelegramLongPollingBot {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String username = update.getMessage().getFrom().getUserName();

            switch (messageText) {
                case "/start":
                    registerUser(chatId, username);
                    showMainMenu(chatId);
                    break;
                case "/menu":
                    showMainMenu(chatId);
                    break;
                default:
                    sendMessage(chatId, "Unknown command. Use /menu to see available commands.");
                    break;
            }
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
        }
    }

    private void registerUser(long chatId, String username) {
        Optional<User> existingUser = userService.findByUsername(username);
        if (existingUser.isEmpty()) {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setChatId(chatId); // Assuming chatId is stored in User
            userService.registerUser(newUser);
            sendMessage(chatId, "Welcome to the Hospital Bot. You have been registered.");
        } else {
            sendMessage(chatId, "Welcome back!");
        }
    }

    private void showMainMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Select an option:");
        message.setReplyMarkup(createMainMenuKeyboard());

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private InlineKeyboardMarkup createMainMenuKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(createInlineKeyboardButton("Doctors", "doctors"));
        keyboard.add(createInlineKeyboardButton("Appointments", "appointments"));
        keyboard.add(createInlineKeyboardButton("Show My Appointments", "show_my_appointments"));
        keyboard.add(createInlineKeyboardButton("Cancel Appointment", "cancel_appointment"));
        return new InlineKeyboardMarkup(keyboard);
    }

    private List<InlineKeyboardButton> createInlineKeyboardButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        return row;
    }

    private void handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        switch (callbackData) {
            case "doctors_with_appointments":
                showDoctors(chatId);
                break;
            case "create_appointment":

            case "move_appointment":
                showAvailableAppointments(chatId);
                break;
            case "show_my_appointments":
                showUserAppointments(chatId);
                break;
            case "cancel_appointment":
                showAppointmentsToCancel(chatId);
                break;
            default:
                sendMessage(chatId, "Unknown action.");
                break;
        }
    }

    private void showDoctors(long chatId) {
        List<Doctor> doctors = doctorService.getAllDoctors();
        StringBuilder responseText = new StringBuilder("Available Doctors:\n");
        for (Doctor doctor : doctors) {
            responseText.append(doctor.getId()).append(". ").append(doctor.getName()).append(" (").append(doctor.getSpecialty()).append(")\n");
        }
        sendMessage(chatId, responseText.toString());
    }

    private void showUserAppointments(long chatId) {
        Optional<User> user = userService.findByChatId(chatId);
        if (user.isPresent()) {
            List<Appointment> userAppointments = appointmentService.getUserAppointments(user.get().getId());
            StringBuilder responseText = new StringBuilder("Your Appointments:\n");
            for (Appointment appointment : userAppointments) {
                responseText.append("Appointment ID: ").append(appointment.getId()).append(" - Doctor ID: ").append(appointment.getDoctorId()).append(" - Time: ").append(appointment.getAppointmentTime()).append("\n");
            }
            sendMessage(chatId, responseText.toString());
        } else {
            sendMessage(chatId, "No user found.");
        }
    }

    private void showAppointmentsToCancel(long chatId) {
        Optional<User> user = userService.findByChatId(chatId);
        if (user.isPresent()) {
            List<Appointment> userAppointments = appointmentService.getUserAppointments(user.get().getId());
            StringBuilder responseText = new StringBuilder("Your Appointments:\n");
            for (Appointment appointment : userAppointments) {
                responseText.append("Appointment ID: ").append(appointment.getId()).append(" - Doctor Name: ").append(appointment.getDoctorId()).append(" - Time: ").append(appointment.getAppointmentTime()).append("\n");
            }
            responseText.append("Reply with the Appointment ID to cancel.");
            sendMessage(chatId, responseText.toString());
        } else {
            sendMessage(chatId, "No user found.");
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
