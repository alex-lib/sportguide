package com.sport.service.entities.place;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "address")
    private String address;

    @Column(name = "description")
    private String description;

    @Column(name = "web_site")
    private String webSite;

    @Column(name = "outdoor")
    private Boolean outdoor;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "type", nullable = false)
    private Type type;

    @Column(name = "photo", columnDefinition = "bytea")
    private byte[] photo;
}