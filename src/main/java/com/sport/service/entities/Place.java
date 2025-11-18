package com.sport.service.entities;

import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.place.PlaceType;
import com.sport.service.entities.enums.place.Subdistrict;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "places", schema = "app_schema")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = "photo")
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "district", nullable = false)
    private District district;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "subdistrict")
    private Subdistrict subdistrict;

    @Column(name = "address")
    private String address;

    @Column(name = "description")
    private String description;

    @Column(name = "web_site")
    private String webSite;

    @Column(name = "outdoor")
    private Boolean outdoor;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "place_type", nullable = false)
    private PlaceType placeType;

    @Lob
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "photo")
    private byte[] photo;

    @Column(name = "coordinates")
    private String coordinates;

    @ManyToMany(mappedBy = "workPlaces", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<Coach> coaches;
}