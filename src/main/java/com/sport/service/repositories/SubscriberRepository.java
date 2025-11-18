package com.sport.service.repositories;

import com.sport.service.entities.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

    long count();

    int countByGetEventsTrue();

    List<Subscriber> findAllByGetEventsTrue();
}