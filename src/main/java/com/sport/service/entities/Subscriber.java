package com.sport.service.entities;

import com.sport.service.entities.enums.subscriber.RoleType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "subscribers")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = {"coach", "jointTrainings", "payments"})
public class Subscriber {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "get_events")
    private Boolean getEvents;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private RoleType role;

    @OneToOne(mappedBy = "subscriber")
    private Coach coach;

    @OneToMany(mappedBy = "subscriber", fetch = FetchType.LAZY)
    private List<JointTraining> jointTrainings;

    @OneToMany(mappedBy = "subscriber", fetch = FetchType.LAZY)
    private List<Payment> payments;
}