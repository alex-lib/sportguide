package com.sport.service.repositories;
import com.sport.service.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT p FROM Event p WHERE p.name = :name")
    Event findByName(String name);
}