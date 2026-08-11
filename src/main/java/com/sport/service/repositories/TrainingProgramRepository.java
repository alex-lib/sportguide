package com.sport.service.repositories;

import com.sport.service.entities.TrainingProgram;
import com.sport.service.entities.enums.common.SportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingProgramRepository extends JpaRepository<TrainingProgram, Long> {

    Optional<TrainingProgram> findByTitle(String title);

    @Query(nativeQuery = true, value = """
            SELECT DISTINCT tp.* FROM training_programs tp
            WHERE (:sportTypes::text[] IS NULL OR tp.sport_types && :sportTypes::text[])
            """)
    List<TrainingProgram> findWithFilters(
            @Param("sportTypes") List<SportType> sportTypes
    );
}
