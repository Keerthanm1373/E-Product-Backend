package com.web.website.models;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Products {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(length = 10000)
    private String name;
    @Column(length = 10000)
    private String description;
    private double price;
    private Boolean productAvailable;
    @Column(nullable = true)
    private double rating;
    @Column(nullable = true)
    private int reviewCount;
    @Column(length = 10000)
    private String url;
    @Column(length = 10000)
    private String imageName;
    private String category;
}
