package com.sport.service.entities;
import com.sport.service.entities.place.Place;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

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

    @Column(name = "district")
    private String description;

    @Column(name = "place_name")
    private String placeName;

//    @ManyToOne
//    @JoinColumn(name = "place_id")
//    @ToString.Exclude
//    private Place place;

    @Column(name = "address")
    private String address;

    @Column(name = "web_site")
    private String webSite;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "time")
    private String time;
}