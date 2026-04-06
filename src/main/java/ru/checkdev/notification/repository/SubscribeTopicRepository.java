package ru.checkdev.notification.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.checkdev.notification.domain.SubscribeTopic;

import java.util.List;

public interface SubscribeTopicRepository extends CrudRepository<SubscribeTopic, Integer> {
    @Override
    List<SubscribeTopic> findAll();

    List<SubscribeTopic> findByUserId(int id);

    SubscribeTopic findByUserIdAndTopicId(int userId, int topicId);

    @Query("""
            SELECT st.userId FROM cd_subscribe_topic st 
            WHERE st.topicId = :topicId 
            AND st.userId != :excludedUserId""")
    List<Integer> findUserIdByTopicIdExcludeCurrent(int topicId, int excludedUserId);
}