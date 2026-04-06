package ru.checkdev.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "of")
public class WisherApprovedDTO {

    private int interviewId;
    private int wisherId;
    private int wisherUserId;
    private String interviewTitle;
    private String interviewLink;
    private String contactBy;
}
