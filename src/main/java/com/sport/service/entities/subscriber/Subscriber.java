package com.sport.service.entities.subscriber;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subscribers")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
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
}