package com.zentrix.model.entity;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/*
* @author Nguyen Thanh Binh - CE171099 - CT25_CPL_JAVA_01
* @date February 11, 2025
*/

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class Supplier {
    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer suppId;

    // Attribute
    @Column(length = 255, nullable = false)
    private String suppName;

    @Column(length = 255)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String address;

    @JsonIgnore
    @OneToMany(mappedBy = "prodId", cascade = CascadeType.ALL)
    private List<Product> products;

    @OneToMany(mappedBy = "supplierId", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Stock> stocks;

}