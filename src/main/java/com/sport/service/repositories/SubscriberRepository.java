package com.sport.service.repositories;
import com.sport.service.entities.subscriber.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

    @Query("SELECT COUNT(s) FROM Subscriber s")
    int getUsersCount();

    @Query("SELECT COUNT(s) FROM Subscriber s WHERE s.getEvents = true")
    int getSubscriptionsCount();
}