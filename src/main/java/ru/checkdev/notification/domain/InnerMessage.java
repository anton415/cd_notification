package ru.checkdev.notification.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.*;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder(builderMethodName = "of")
@Entity(name = "cd_message")
public class InnerMessage implements Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int userId;
    private String text;
    private Timestamp created;
    private boolean read;
    private int interviewId;

    public InnerMessage(int id, int userId, String text, Timestamp created, boolean read) {
        this.id = id;
        this.userId = userId;
        this.text = text;
        this.created = created;
        this.read = read;
    }
}
