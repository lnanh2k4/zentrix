package com.zentrix.model.entity;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/*
 * @author Vo Lam Thuy Vi - CE170398 - CT25_CPL_JAVA_01
 * @date February 11, 2025
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "notification") 
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Primary key
    Long notiId;
    // Foreign Key
    @ManyToOne
    Staff createdBy;
    // Normal key
    @Column(nullable = false)
    String title;
    @Column(nullable = false)
    String description;
    @Column(nullable = false)
    Date createdAt;
    @Column(nullable = false)
    Integer status;
    @Column(nullable = true)
    Boolean isPublic;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = true)
    User user;
}
