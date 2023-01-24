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
        return "5545958714:AAFzeHx-LA2OFejBM3gwWRIW5RYbPhM9Gmo";
    }

    @Override
    public String getBotUsername() {
        return "https://t.me/abdumominbot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            User user = UserService.getUserService().getUserWithChatId(message.getChatId().toString());
            if (message.getText().equals("/start")) {
                sendBot(UserService.getUserService().openUserMenu(message));
            } else if (user.getBotState().equals(BotState.OPEN_QR_GENERATE)) {
                sendBot(UserService.getUserService().generatingQrPhotos(message));
            }
            else if (user.getBotState().equals(BotState.OPEN_READ_QR)) {

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


                            BufferedImage bfrdImgobj = ImageIO.read(file);
                            LuminanceSource source = new BufferedImageLuminanceSource(bfrdImgobj);
                            BinaryBitmap binarybitmapobj = new BinaryBitmap(new HybridBinarizer(source));
                            Result resultobj = new MultiFormatReader().decode(binarybitmapobj);
                            sendMessage.setText(" Result: " + resultobj.getText());
//                System.out.println("Data Stored In our QR Code" +"  " + resultobj.getText());

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

    public SendMessage readingPhotos15(Message message) {
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
                    java.io.File file = new java.io.File("src/main/resources/downloadPhotos/photo_" + uuid + "." + file1.getFilePath().split("\\.")[1]);
                    downloadFile(file1, file);

                    BufferedImage bfrdImgobj = ImageIO.read(file);
                    LuminanceSource source = new BufferedImageLuminanceSource(bfrdImgobj);
                    BinaryBitmap binarybitmapobj = new BinaryBitmap(new HybridBinarizer(source));
                    Result resultobj = new MultiFormatReader().decode(binarybitmapobj);
                    sendMessage.setText(" Result: " + resultobj.getText());
//                System.out.println("Data Stored In our QR Code" +"  " + resultobj.getText());

                } catch (TelegramApiException | NotFoundException | IOException e) {
                    e.printStackTrace();
                }

            } else {
                sendMessage.setText("1515");
            }
        }
        sendMessage.setReplyMarkup(UserService.getUserService().getUserMenuButton());
        UserService.getUserService().updateUserState(message.getChatId().toString(), BotState.OPEN_MENU);
        return sendMessage;
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
