package uz.pdp.online.exam;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter

public class User {

    private String chatId;
    private BotState botState;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(chatId, user.chatId) && botState == user.botState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId, botState);
    }
}
