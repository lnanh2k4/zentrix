package com.zentrix.model.entity;

import java.sql.Date;
import java.util.List;

import com.zentrix.model.utils.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
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
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Primary Key
    Long postId;
    // Foreign Key
    @ManyToOne
    @JoinColumn(name = "created_by", referencedColumnName = "staffId")
    Staff createdBy;

    @ManyToOne
    @JoinColumn(name = "approved_by", referencedColumnName = "staffId")
    Staff approvedBy;

    // Normal key
    @Column(nullable = false)
    String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    String description;

    @Column(nullable = false)
    Date createdAt;

    @Enumerated(EnumType.STRING)
    Status status;

    @Transient
    List<String> images;

}
