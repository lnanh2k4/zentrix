package com.zentrix.model.entity;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cateId;

    // Attribute
    @Column(length = 255, nullable = false)
    private String cateName;

    @ManyToOne
    private Category parentCateId;

    @JsonIgnore
    @OneToMany(mappedBy = "orderId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;

    @JsonIgnore
    @OneToMany(mappedBy = "cateId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> categories;

}