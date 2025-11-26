package com.sport.service.entities;

import com.sport.service.entities.enums.coach.Sex;
import com.sport.service.entities.enums.common.SportType;
import com.sport.service.entities.training_program.TrainingProgram;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "coaches")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = {"photo", "trainingPrograms"})
public class Coach {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "subscriber_id")
    public Subscriber subscriber;

    @Column(name = "name")
    private String name;

    @Column(name = "sport_types")
    @Enumerated(EnumType.STRING)
    private List<SportType> sportTypes;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "age", columnDefinition = "SMALLINT", nullable = false)
    private Integer age;

    @Column(name = "sex", nullable = false)
    @Enumerated(EnumType.STRING)
    private Sex sex;

    @Column(name = "years_of_experience", columnDefinition = "SMALLINT")
    private Integer yearsOfExperience;

    @Column(name = "education")
    private String education;

    @Column(name = "phone_number")
    private String phoneNumber;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "coaches_work_places",
            joinColumns = @JoinColumn(name = "coach_id"),
            inverseJoinColumns = @JoinColumn(name = "place_id"))
    private List<Place> workPlaces;

    @Lob
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "photo")
    private byte[] photo;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "months_for_subscription_to_be_coach", columnDefinition = "SMALLINT")
    private Short monthsForSubscriptionToBeCoach;

    @Column(name = "expired_date_for_subscription_to_be_coach")
    private LocalDate expiredDateForSubscriptionToBeCoach;

    @Column(name = "show_in_web")
    private Boolean showInWeb;

    @ManyToMany(mappedBy = "creators", fetch = FetchType.LAZY)
    private List<TrainingProgram> trainingPrograms;
}