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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
                    registerUser(update);
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

    private void registerUser(Update update) {
        long telegramUserId = getUserTelegramId(update);
        long chatId = getChatId(update);

        Optional<User> existingUser = userService.getUserByTelegramId(telegramUserId);

        if (existingUser.isEmpty()) {
            String name = update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName();

            User newUser = new User(
                    telegramUserId,
                    chatId,
                    update.getMessage().getFrom().getUserName(),
                    name
            );

            userService.registerUser(newUser);

            sendMessage(chatId, "Welcome to the Hospital Bot. You have been registered.");
        } else {
            sendMessage(chatId, "Welcome back!");
            showMainMenu(chatId);
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
        keyboard.add(createInlineKeyboardRow("Doctors Appointments", "doctors_with_appointments"));
        keyboard.add(createInlineKeyboardRow("Book Appointment", "create_appointment"));
        keyboard.add(createInlineKeyboardRow("Show My Appointments", "show_my_appointments"));
        keyboard.add(createInlineKeyboardRow("Move Appointment", "move_appointment"));
        keyboard.add(createInlineKeyboardRow("Cancel Appointment", "cancel_appointment"));
        return new InlineKeyboardMarkup(keyboard);
    }

    private List<InlineKeyboardButton> createInlineKeyboardRow(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        return row;
    }

    private InlineKeyboardButton createInlineKeyboard(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    private void handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        long telegramUserId = getUserTelegramId(update);
        long chatId = getChatId(update);

        Optional<User> user = userService.getUserByTelegramId(telegramUserId);

        if (!user.isEmpty()) {
            sendMessage(chatId, "You are not logged in.");
            return;
        }

        switch (callbackData) {
            case "show_doctors":
                showDoctors(chatId);
                break;

            case "show_doctor_working_days":
                showDoctorWorkingDays(update);
                break;

            case "show_doctor_free_hours":
                showDoctorFreeHours(update);
                break;

            case "create_appointment":
                createAppointmentForUser(update);
                break;

            case "show_my_appointments":
                showUserAppointments(update);
                break;

            case "show_appointment_actions":
                showUserAppointmentActions(update);
                break;

            case "display_move_dates":
                displayAvailableAppointMoveDates(update);
                break;

            case "display_move_hours":
                displayAvailableAppointMoveHours(update);
                break;

            case "move_appointment":
                moveUserAppointment(update);
                break;

            case "cancel_appointment":
                cancelUserAppointment(update);
                break;
        }
    }
    private void handleDoctorSelection(Update update) {
        long doctorId = extractDoctorId(update.getCallbackQuery().getData());
        long chatId = getChatId(update);

        Optional<Doctor> doctorOpt = doctorService.getDoctorById(doctorId);
        if (doctorOpt.isEmpty()) {
            sendMessage(chatId, "Doctor not found.");
            return;
        }

        Doctor doctor = doctorOpt.get();
        List<List<InlineKeyboardButton>> workingDays = doctor.getWorkingDays();

        List<List<InlineKeyboardButton>> keyboard = workingDays.stream()
                .map(date -> createInlineKeyboardRow(date.toString(), "select_date:doctorId=" + doctorId + ":date=" + date))
                .collect(Collectors.toList());

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Select a date for the appointment:");
        message.setReplyMarkup(new InlineKeyboardMarkup(keyboard));

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void handleDateSelection(Update update) {
        long doctorId = extractDoctorId(update.getCallbackQuery().getData());
        String dateStr = extractDateFromCallbackData(update.getCallbackQuery().getData());
        LocalDate date = LocalDate.parse(dateStr);
        long chatId = getChatId(update);

        Optional<Doctor> doctorOpt = doctorService.getDoctorById(doctorId);
        if (doctorOpt.isEmpty()) {
            sendMessage(chatId, "Doctor not found.");
            return;
        }

        Doctor doctor = doctorOpt.get();
        List<List<InlineKeyboardButton>> freeHours = doctor.getFreeHours(dateStr);

        List<List<InlineKeyboardButton>> keyboard = freeHours.stream()
                .map(time -> createInlineKeyboardRow(time.toString(), "select_time:doctorId=" + doctorId + ":date=" + date + ":time=" + time))
                .collect(Collectors.toList());

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Select a time for the appointment:");
        message.setReplyMarkup(new InlineKeyboardMarkup(keyboard));

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }








    private void showDoctors(long chatId) {
        List<Doctor> doctors = this.doctorService.getAllDoctors();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (Doctor doctor : doctors) {
            keyboard.add(createInlineKeyboardRow(doctor.getName(), "show_doctor_working_days" + ":doctorId=" + doctor.getId()));
        }

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Select Doctor to see Working Days");
        message.setReplyMarkup(new InlineKeyboardMarkup(keyboard));
    }

    private void showDoctorWorkingDays(Update update) {
        long doctorId = extractDoctorId(update.getCallbackQuery().getData());
        long chatId = getChatId(update);
        Optional<Doctor> doctor = this.doctorService.getDoctorById(doctorId);

        if (doctor.isEmpty()) {
            sendMessage(update.getMessage().getChatId(), "Doctor not found.");
            return;
        }

        List<List<InlineKeyboardButton>> keyboard = doctor.get().getWorkingDays();

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Select Day to see Free Hours");
        message.setReplyMarkup(new InlineKeyboardMarkup(keyboard));
    }


    private void showDoctorFreeHours(Update update) {
        String dateStr = extractDateFromCallbackData(update.getCallbackQuery().getData());
        Long chatId = getChatId(update);
        Long doctorId = extractDoctorId(update.getCallbackQuery().getData());
        LocalDate date = LocalDate.parse(dateStr);

        Optional<Doctor> doctorOpt = this.doctorService.getDoctorById(doctorId);

        if (doctorOpt.isEmpty()) {
            sendMessage(chatId, "Doctor not found.");
            return;
        }

        Doctor doctor = doctorOpt.get();
        List<Appointment> appointments = this.appointmentService.getAllAppointmentsByDoctorIdAndDate(doctorId, date);
        doctor.setAppointments(appointments);

        List<List<InlineKeyboardButton>> keyboard = doctor.getFreeHours(dateStr);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Select Hour to Book an appointment");
        message.setReplyMarkup(new InlineKeyboardMarkup(keyboard));
    }


    private void showUserAppointments(Update update) {
        long telegramUserId = getUserTelegramId(update);
        long chatId = getChatId(update);

        Optional<User> user = userService.getUserByTelegramId(telegramUserId);

        if (user.isEmpty()) {
            sendMessage(chatId, "You are not registered.");
            return;
        }

        List<Appointment> appointments = appointmentService.getUserAppointments(user.get().getId());

        if (appointments.isEmpty()) {
            sendMessage(chatId, "You have no appointments.");
            return;
        }

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (Appointment appointment : appointments) {
            String callbackData = String.format("show_appointment_actions:appointmentId=%d", appointment.getId());
            keyboard.add(createInlineKeyboardRow(String.format("Doctor: %s | Date: %s | Time: %s",
                    appointment.getDoctor().getName(),
                    appointment.getDate(),
                    appointment.getHour()), callbackData));
        }

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Your Appointments:");
        message.setReplyMarkup(new InlineKeyboardMarkup(keyboard));

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }



    private void showUserAppointmentActions(Update update) {
        long appointmentId = extractAppointmentId(update.getCallbackQuery().getData());
        long chatId = getChatId(update);

        Optional<Appointment> appointmentOpt = appointmentService.getAppointmentById(appointmentId);

        if (appointmentOpt.isEmpty()) {
            sendMessage(chatId, "Appointment not found.");
            return;
        }

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(createInlineKeyboardRow("Move", "display_move_dates:appointmentId=" + appointmentId));
        keyboard.add(createInlineKeyboardRow("Cancel", "cancel_appointment:appointmentId=" + appointmentId));

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Select an action:");
        message.setReplyMarkup(new InlineKeyboardMarkup(keyboard));

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }



    private void displayAvailableAppointMoveDates(Update update) {
    }

    private void displayAvailableAppointMoveHours(Update update) {
    }

    private void moveUserAppointment(Update update) {
    }

    private void cancelUserAppointment(Update update) {
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

    private long getUserTelegramId(Update update) {
        return update.getMessage().getFrom().getId();
    }

    private long getChatId(Update update) {
        return update.getMessage().getChatId();
    }

    private String extractHoursFromCallbackData(String callbackData) {
        // Example callbackData: "show_doctor_free_hours:doctorId=1:date=2024-07-22"
        String[] parts = callbackData.split(":");
        for (String part : parts) {
            if (part.startsWith("hours=")) {
                return part.split("=")[1];
            }
        }
        throw new IllegalArgumentException("Invalid callback data format for hours extraction");
    }

    private String extractDateFromCallbackData(String callbackData) {
        // Example callbackData: "show_doctor_free_hours:doctorId=1:date=2024-07-22"
        String[] parts = callbackData.split(":");
        for (String part : parts) {
            if (part.startsWith("date=")) {
                return part.split("=")[1];
            }
        }
        throw new IllegalArgumentException("Invalid callback data format for date extraction");
    }

    private long extractDoctorId(String callbackData) {
        // Example callbackData: "show_doctor_working_days:doctorId=1"
        String[] parts = callbackData.split(":");
        if (parts.length > 1) {
            String[] idParts = parts[1].split("=");
            if (idParts.length > 1) {
                return Long.parseLong(idParts[1]);
            }
        }
        throw new IllegalArgumentException("Invalid callback data format");
    }
    private long extractAppointmentId(String callbackData) {
        String[] parts = callbackData.split(":");
        for (String part : parts) {
            if (part.startsWith("appointmentId=")) {
                return Long.parseLong(part.split("=")[1]);
            }
        }
        throw new IllegalArgumentException("Invalid callback data format for appointment ID extraction");
    }


}
