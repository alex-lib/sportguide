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
    @Column(name = "type")
    private Type type;

//    @Lob
    @Column(name = "photo", columnDefinition = "BYTEA")
    private byte[] photo;
}