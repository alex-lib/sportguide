package com.sport.service.repositories;
import com.sport.service.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE e.name = :name")
    Event findByName(@Param("name") String name);

    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.name = :name")
    boolean existsByName(@Param("name") String name);

    @Query("SELECT e FROM Event e WHERE e.name = :name")
    List<Event> findAllByName(@Param("name") String name);
}