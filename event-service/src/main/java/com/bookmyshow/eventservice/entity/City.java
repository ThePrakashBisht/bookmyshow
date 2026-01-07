package com.bookmyshow.eventservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class City extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String state;

    private String country = "India";

    private boolean active = true;

    // One city has many venues
    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Venue> venues = new ArrayList<>();
}