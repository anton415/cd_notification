package ru.checkdev.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Calendar;

/**
 * DTO модель класса Person сервиса Auth.
 *
 * @author parsentev
 * @author Arcady555
 * @since 25.09.2016
 * <p>
 * переделал в Profile
 * @since 01.11.2023
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profile implements Base {
    private int id;
    private String username;
    private String email;
    private String password;
    private boolean privacy;
    private Calendar created;
}