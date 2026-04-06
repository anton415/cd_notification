package ru.checkdev.notification.telegram.config;

import org.junit.jupiter.api.Test;
import ru.checkdev.notification.domain.Profile;

import java.util.Calendar;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testing TgConfig;
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 06.10.2023
 */
class TgConfigTest {
    private final int passSize = 10;
    private final TgConfig tgConfig = new TgConfig(passSize);

    @Test
    void whenIsEmailThenReturnTrue() {
        String email = "mail@mail.ru";
        assertThat(tgConfig.isEmail(email)).isTrue();
    }

    @Test
    void whenIsEmailThenReturnFalse() {
        String email = "mail.ru";
        assertThat(tgConfig.isEmail(email)).isFalse();
    }

    @Test
    void whenGetPasswordThenLengthPassSize() {
        String pass = tgConfig.getPassword();
        assertThat(pass.length()).isEqualTo(passSize);
    }

    @Test
    void whenGetPasswordThenNotStartWishPrefix() {
        String pass = tgConfig.getPassword();
        assertThat(pass.startsWith("tg/")).isFalse();
    }

    @Test
    void whenGetObjectToMapThenReturnObjectMap() {
        Profile profile = new Profile(
                0, "username", "mail", "pass", true, Calendar.getInstance());
        Map<String, String> map = tgConfig.getObjectToMap(profile);

        assertThat(map.get("username")).isEqualTo(profile.getUsername());
        assertThat(map.get("email")).isEqualTo(profile.getEmail());
        assertThat(map.get("password")).isEqualTo(profile.getPassword());
        assertThat(String.valueOf(map.get("privacy"))).isEqualTo(String.valueOf(true));
    }

    @Test
    void whenGetNameFromEmailGetPrefixEmail() {
        String email = "emailPrefix@emailDomen.ru";
        String name = "emailPrefix";
        String actual = tgConfig.getNameFromEmail(email);
        assertThat(actual).isEqualTo(name);
    }
}