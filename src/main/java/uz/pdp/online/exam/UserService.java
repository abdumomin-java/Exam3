package uz.pdp.online.exam;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTZoom;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import com.google.zxing.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class UserService {

    public SendPhoto generatingQrPhotos(Message message){
        String data = message.getText();
        UUID uuid = UUID.randomUUID();
        String path = "src\\main\\resources\\photos\\" + uuid + ".jpg";
        String charset = "UTF-8";
        Map<EncodeHintType, ErrorCorrectionLevel> hashMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
        hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        try {
            createQR(data, path, charset, hashMap, 200, 200);
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(message.getChatId());
        sendPhoto.setPhoto(new InputFile(new File(path)));
        sendPhoto.setCaption("Successfully created Qr photo");
        sendPhoto.setReplyMarkup(getUserMenuButton());
        updateUserState(message.getChatId().toString(), BotState.OPEN_MENU);
        return sendPhoto;
    }
    public SendMessage readBeginQR(CallbackQuery callbackQuery){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(callbackQuery.getMessage().getChatId());
        sendMessage.setText(" Send me QR photo ");
        updateUserState(callbackQuery.getMessage().getChatId().toString(), BotState.OPEN_READ_QR);
        return sendMessage;
    }

    public void createQR (String data, String path, String charset, Map hashMap, int height, int width)throws WriterException, IOException {
        BitMatrix matrix = new MultiFormatWriter().encode(new String(data.getBytes(charset), charset), BarcodeFormat.QR_CODE, width, height);
        MatrixToImageWriter.writeToFile(matrix, path.substring(path.lastIndexOf('.') + 1), new File(path));
    }

    public SendMessage generateQRForUser(CallbackQuery callbackQuery){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(callbackQuery.getMessage().getChatId());
        sendMessage.setText(" Enter text for Qr code");
        updateUserState(callbackQuery.getMessage().getChatId().toString(), BotState.OPEN_QR_GENERATE);
        return sendMessage;
    }

    public SendMessage openUserMenu(Message message){
        User user1 = getUserWithChatId(message.getChatId().toString());
        if (user1.getChatId() == null){
            User user = new User();
            user.setChatId(message.getChatId().toString());
            user.setBotState(BotState.OPEN_MENU);
            writeUser(user);
        }
        updateUserState(message.getChatId().toString(), BotState.OPEN_MENU);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyMarkup(getUserMenuButton());
        sendMessage.setText(" Choose Button \uD83D\uDC47\uD83C\uDFFB");
        return sendMessage;
    }
    public void writeUsers(List<User> users) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String s = gson.toJson(users);
        try {
            Files.write(Path.of("src/main/resources/json/user.json"), s.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeUser(User user){
        List<User> users = getUsers();
        users.add(user);
        writeUsers(users);
    }

    public List<User> getUsers() {
        Type type = new TypeToken<List<User>>() {
        }.getType();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (BufferedReader reader = Files.newBufferedReader(Path.of("src/main/resources/json/user.json"))) {
            List<User> users = gson.fromJson(reader, type);
            return users == null ? new ArrayList<>() : users;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public User getUserWithChatId(String chatId){
        return getUsers().stream().filter(user -> user.getChatId().equals(chatId)).findFirst().orElse(new User());
    }

    public void updateUserState(String chatId, BotState botState) {
        List<User> users = getUsers();
        User user = users.stream().filter(u -> u.getChatId().equals(chatId)).findFirst().orElse(new User());
        user.setBotState(botState);
        writeUsers(users);
    }

    public InlineKeyboardMarkup getUserMenuButton(){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Generate QR code");
        button1.setCallbackData("generate_qr_code");
        row1.add(button1);
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Read QR code");
        button2.setCallbackData("read_qr_code");
        row1.add(button2);
        rows.add(row1);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    private static final UserService userService = new UserService();
    public static UserService getUserService() {
        return userService;
    }
}
