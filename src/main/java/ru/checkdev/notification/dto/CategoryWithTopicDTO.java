package ru.checkdev.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryWithTopicDTO {

    private int categoryId;
    private String categoryName;
    private int topicId;
    private String topicName;
    private int interviewId;
    private int submitterId;
}
