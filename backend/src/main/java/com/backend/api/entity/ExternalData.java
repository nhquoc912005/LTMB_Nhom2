package com.backend.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "phong")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @jakarta.persistence.Column(name = "id_phong")
    private Long id;
    
    @jakarta.persistence.Column(name = "ten_phong")
    private String name;
    
    @jakarta.persistence.Column(name = "loai_phong")
    private String description;
}
