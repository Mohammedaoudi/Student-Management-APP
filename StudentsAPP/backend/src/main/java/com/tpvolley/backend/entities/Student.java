package com.tpvolley.backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Student {
    @Id
    @GeneratedValue
    private Integer id;

    private String nom;

    private String prenom;

    private String ville;

    private String sexe;

    private String image;

    private String filiere;


}
