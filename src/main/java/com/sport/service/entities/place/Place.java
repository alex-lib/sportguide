package com.sport.service.entities.place;
import com.sport.service.entities.Event;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "places", schema = "app_schema")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "district")
    private PlaceDistrict district;

    @Column(name = "address")
    private String address;

    @Column(name = "description")
    private String description;

    @Column(name = "web_site")
    private String webSite;

    @Column(name = "outdoor")
    private Boolean outdoor;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "type")
    private PlaceType type;

    @Lob
    @Column(name = "photo")
    private byte[] photo;

//    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL)
//    @ToString.Exclude
//    @Builder.Default
//    private List<Event> events = new ArrayList<>();
}