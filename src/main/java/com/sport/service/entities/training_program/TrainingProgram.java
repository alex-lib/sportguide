package com.sport.service.entities.training_program;

import com.sport.service.entities.Coach;
import com.sport.service.entities.Payment;
import com.sport.service.entities.enums.common.SportType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Column;
import jakarta.persistence.GenerationType;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "training_programs")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = "payments")
public class TrainingProgram {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "coaches_training_programs",
            joinColumns = @JoinColumn(name = "training_program_id"),
            inverseJoinColumns = @JoinColumn(name = "coach_id"))
    private List<Coach> creators;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "payments_count")
    private Long paymentsCount;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "sport_types")
    @Enumerated(EnumType.STRING)
    private List<SportType> sportTypes;

    @Column(name = "program_id_in_mongodb", nullable = false)
    public String programIdInMongoDB;

    @Column(name = "show_in_web")
    private Boolean showInWeb;

    @OneToMany(mappedBy = "trainingProgram", fetch = FetchType.LAZY)
    private List<Payment> payments;
}