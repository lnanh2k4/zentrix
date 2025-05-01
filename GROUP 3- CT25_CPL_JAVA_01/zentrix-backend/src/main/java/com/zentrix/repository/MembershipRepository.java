package com.zentrix.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.zentrix.model.entity.Membership;

/*
* @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
* @date February 11, 2025
*/
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    /**
     * This method allows to find Membership records by their name.
     * It retrieves a list of memberships whose name matches the provided name.
     *
     * @param mbsName the name of the membership to search for.
     * @return a list of Membership objects that match the provided name.
     */
    Page<Membership> findByMbsName(String mbsName, Pageable pageable);

    /**
     * 
     * @param mbsName
     * @return
     */
    Optional<Membership> findByMbsNameIgnoreCase(String mbsName);

}
