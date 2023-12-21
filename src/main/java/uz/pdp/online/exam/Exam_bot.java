package uz.pdp.online.exam;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class Exam_bot extends TelegramLongPollingBot {

    @Override
    public String getBotToken() {
        return "YOUR_BOT_TOKEN";
    }

    @Override
    public String getBotUsername() {
        return "YOUR_BOT_USERNAME";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            User user = UserService.getUserService().getUserWithChatId(message.getChatId().toString());
            if (message.getText() != null && message.getText().equals("/delete")) {
                ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
                replyKeyboardRemove.setRemoveKeyboard(true);
                SendMessage sendMessage = new SendMessage();
                sendMessage.setText(" ReplyKeyboard olib tashlandi!!! Qaytadan /start bosing.");
                sendMessage.setChatId(message.getChatId());
                sendMessage.setReplyMarkup(replyKeyboardRemove);
                sendBot(sendMessage);
            } else if (message.getText() != null && message.getText().equals("/start")) {
                sendBot(UserService.getUserService().openUserMenu(message));
            } else if (user.getBotState().equals(BotState.OPEN_QR_GENERATE)) {
                sendBot(UserService.getUserService().generatingQrPhotos(message));
            } else if (user.getBotState().equals(BotState.OPEN_READ_QR)) {

                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId());
                if (message.hasPhoto()) {

                    List<PhotoSize> photo = message.getPhoto();
                    if (photo != null && !photo.isEmpty()) {
                        photo.sort(Comparator.comparing(PhotoSize::getFileSize).reversed());
                        PhotoSize photoSize = photo.get(0);
                        GetFile getFile = new GetFile(photoSize.getFileId());
                        try {
                            File file1 = execute(getFile);
                            UUID uuid = UUID.randomUUID();
                            java.io.File file = new java.io.File("src/main/resources/downloadPhotos/photo_" + uuid + ".jpg");
                            downloadFile(file1, file);

                            BufferedImage bfrdImgobj = ImageIO.read(new java.io.File(file.getPath()));
                            LuminanceSource source = new BufferedImageLuminanceSource(bfrdImgobj);
                            BinaryBitmap binarybitmapobj = new BinaryBitmap(new HybridBinarizer(source));
                            Result resultobj = new MultiFormatReader().decode(binarybitmapobj);
                            sendMessage.setText(" Result: " + resultobj.getText());

                        } catch (TelegramApiException | NotFoundException | IOException e) {
                            e.printStackTrace();
                        }

                    } else {
                        sendMessage.setText("PhotoSize not found");
                    }
                } else {
                    sendMessage.setText("Photo not found");
                }
                sendMessage.setReplyMarkup(UserService.getUserService().getUserMenuButton());
                UserService.getUserService().updateUserState(message.getChatId().toString(), BotState.OPEN_MENU);
                sendBot(sendMessage);
            } else {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId());
                sendMessage.setText("Admin ga murojaat uchun Rahmat!\uD83E\uDD1D \n Admin bundan mamnun bo`ladi!\uD83D\uDC4D✊ \n Admin username\uD83D\uDE0E: https://t.me/Mavlonovich_java");
                UserService.getUserService().updateUserState(message.getChatId().toString(), BotState.OPEN_MENU);
                sendMessage.setReplyMarkup(UserService.getUserService().getUserMenuButton());
                sendBot(sendMessage);
            }

        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            User user = UserService.getUserService().getUserWithChatId(callbackQuery.getMessage().getChatId().toString());
            String data = callbackQuery.getData();
            if (data.equals("generate_qr_code") && user.getBotState().equals(BotState.OPEN_MENU)) {
                sendBot(UserService.getUserService().generateQRForUser(callbackQuery));
            } else if (data.equals("read_qr_code") && user.getBotState().equals(BotState.OPEN_MENU)) {
                sendBot(UserService.getUserService().readBeginQR(callbackQuery));
            }
        }
    }

    public void sendBot(Object obj) {
        try {
            if (obj instanceof SendMessage sendMessage) {
                execute(sendMessage);
            } else if (obj instanceof SendPhoto sendPhoto) {
                execute(sendPhoto);
            } else if (obj instanceof SendDocument sendDocument) {
                execute(sendDocument);
            }

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
