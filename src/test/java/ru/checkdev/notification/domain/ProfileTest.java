package ru.checkdev.notification.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Calendar;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileTest {

    private static final Calendar CREATED = new Calendar.Builder()
            .setDate(2023, 10, 23)
            .setTimeOfDay(20, 20, 20)
            .build();

    private Profile profile;

    @BeforeEach
    public void setUp() {
        profile = new Profile(0, "username", "email", "password", true, CREATED);
    }

    @Test
    public void testGetUsername() {
        assertThat(profile.getUsername()).isEqualTo("username");
    }

    @Test
    public void testGetEmail() {
        assertThat(profile.getEmail()).isEqualTo("email");
    }

    @Test
    public void testGetPassword() {
        assertThat(profile.getPassword()).isEqualTo("password");
    }

    @Test
    public void testGetPrivacy() {
        assertThat(profile.isPrivacy()).isTrue();
    }

    @Test
    public void testGetCreated() {
        assertThat(profile.getCreated()).isEqualTo(CREATED);
    }
}