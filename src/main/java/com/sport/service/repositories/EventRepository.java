package com.sport.service.repositories;

import com.sport.service.entities.Event;
import com.sport.service.entities.enums.common.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    boolean existsByName(String name);

    List<Event> findAllByName(String name);

    @Modifying
    void deleteByDateBefore(LocalDate currentDate);

    @Query("""
    SELECT e FROM Event e
    WHERE (:district IS NULL OR e.district = :district)
      AND (:date IS NULL OR e.date = :date)
    ORDER BY e.date, e.time
    """)
    List<Event> findWithFilters(@Param("district") District district, @Param("date") LocalDate date);
}
