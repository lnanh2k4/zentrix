package com.zentrix.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zentrix.model.entity.Variation;

/*
 * @author Huynh Hoang Ty CE180191 - CT25_CPL_JAVA_01
 * @date February 20, 2025
 */
public interface VariationRepository extends JpaRepository<Variation, Long> {
}
