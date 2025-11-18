package com.sport.service.entities;

import com.sport.service.entities.enums.common.District;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "events")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "place_name")
    private String placeName;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "district")
    private District district;

    @Column(name = "address")
    private String address;

    @Column(name = "link")
    private String link;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "time", columnDefinition = "TIME")
    private LocalTime time;

    @Column(name = "coordinates")
    private String coordinates;
}