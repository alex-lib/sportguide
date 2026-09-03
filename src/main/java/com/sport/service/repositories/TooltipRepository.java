package com.sport.service.repositories;

import com.sport.service.entities.Tooltip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TooltipRepository extends JpaRepository<Tooltip, Long> {

    @Query("SELECT t FROM Tooltip t WHERE t.tour.id = :tourId ORDER BY t.position ASC")
    List<Tooltip> findByTourIdOrderByPositionAsc(@Param("tourId") Long tourId);
}