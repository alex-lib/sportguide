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

    @Query(nativeQuery = true, value = """
        SELECT e.*
        FROM events e
        WHERE (CAST(:district AS varchar) IS NULL OR e.district = :district)
          AND (CAST(:date AS date) IS NULL OR e.date = :date)
          AND (CAST(:search AS varchar) IS NULL
               OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(e.place_name) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(e.address) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY e.date, e.time
        """)
    List<Event> findWithFilters(
            @Param("district") District district,
            @Param("date") LocalDate date,
            @Param("search") String search
    );
}
