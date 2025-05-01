package com.zentrix.service;

import java.util.List;

import com.zentrix.model.entity.Membership;
import com.zentrix.model.entity.Promotion;
import com.zentrix.model.request.MembershipRequest;
import com.zentrix.model.response.PaginationWrapper;

/*
* @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
* @date February 11, 2025
*/
public interface MembershipService {
    /**
     * Retrieve all memberships from the database.
     *
     * @return List of all memberships.
     * @throws AppException if there is an issue retrieving the data.
     */
    PaginationWrapper<List<Membership>> getAllMemberships(int page, int size);

    /**
     * Retrieve a membership by its unique ID.
     *
     * @param id Membership ID.
     * @return An Optional containing the membership if found, otherwise empty.
     * @throws AppException if there is an issue retrieving the data.
     */
    Membership getMembershipById(Long id);

    /**
     * Save or update a membership in the database.
     *
     * @param membership Membership object to be saved.
     * @return The saved or updated Membership object.
     * @throws AppException if there is an issue saving the data.
     */
    Membership saveMembership(Membership membership);

    /**
     * Delete a membership by its ID.
     *
     * @param id Membership ID to delete.
     * @throws AppException if there is an issue deleting the data.
     */
    void deleteMembership(Long id);

    /**
     * Create a new membership entry in the system.
     *
     * @param req Request object containing membership details.
     * @return The created Membership object.
     * @throws AppException if there is an issue creating the membership.
     */
    Membership createMembership(MembershipRequest req);

    /**
     * Search for memberships by name.
     *
     * @param mbsName Name of the membership to search for.
     * @return List of memberships that match the given name.
     * @throws AppException if there is an issue retrieving the data.
     */
    PaginationWrapper<List<Membership>> findMembershipByName(String mbsName, int page, int size);

    /**
     * Update the membership information.
     *
     * @param id  The ID of the membership to update.
     * @param req The request object containing the data to update.
     * @return The updated Membership object.
     * @throws AppException if there is an error during the update process.
     */
    Membership updateMembership(Long id, MembershipRequest req);

    /**
     * Assigns a promotion to a user upon ranking up.
     * 
     * @param userId   The ID of the user.
     * @param mbsId    The ID of the membership.
     * @param username The username of the user.
     */
    void assignPromotionOnRankUp(Long userId, Long mbsId, String username);

    /**
     * Retrieves all promotions for a specific user.
     * 
     * @param userId The ID of the user.
     * @return List of promotions associated with the user.
     */
    List<Promotion> getUserPromotions(Long userId);

    /**
     * Applies a membership to a user.
     * 
     * @param userId The ID of the user.
     * @param mbsId  The ID of the membership.
     */
    void applyMembershipToUser(Long userId, Long mbsId);

    /**
     * Automatically updates a user's membership based on accumulated points.
     * 
     * @param userId            The ID of the user.
     * @param accumulatedPoints The total points accumulated by the user.
     */
    void autoUpdateMembership(Long userId, Long accumulatedPoints);
}
