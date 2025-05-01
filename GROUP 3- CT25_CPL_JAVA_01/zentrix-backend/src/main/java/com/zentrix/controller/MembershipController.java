package com.zentrix.controller;

import com.zentrix.model.entity.Membership;
import com.zentrix.model.entity.Promotion;
import com.zentrix.model.request.MembershipRequest;

import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.MembershipService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
* @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
* @date February 11, 2025
*/
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping("/api/v1/memberships")

@Tag(name = "Membership Controller", description = "This class contains the membership CRUD methods.")

public class MembershipController {

        // Injects MembershipService to handle business logic
        MembershipService membershipService;

        /**
         * Retrieves all memberships.
         *
         * This method fetches and returns all memberships from the database.
         *
         * @return ResponseEntity containing a list of all memberships wrapped in
         *         ApiResponse.
         */
        @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Get memberships", description = "Get all memberships")
        @GetMapping

        public ResponseEntity<ResponseObject<List<Membership>>> getAllMemberships(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size
        // @RequestHeader("Authorization") String jwt
        ) {
                // Fetch the list of memberships from the service
                PaginationWrapper<List<Membership>> memberships = membershipService.getAllMemberships(page, size);
                ResponseObject<List<Membership>> response = new ResponseObject.Builder<List<Membership>>()
                                .unwrapPaginationWrapper(
                                                memberships)
                                .message("Get customers list successfully!")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();

                // Wrap the result in ApiResponse and return it as a response entity

                return ResponseEntity.ok(response);
        }

        /**
         * Retrieves a membership by its ID.
         *
         * This method fetches a membership based on the provided ID and returns
         * the details of the membership if found. If not found, it returns a 404
         * error.
         *
         * @param id The ID of the membership.
         * @return ResponseEntity containing the membership details or error if not
         *         found.
         */
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Get membership by ID", description = "Retrieve a membership by its ID.")
        @GetMapping("/{id}")
        public ResponseEntity<ResponseObject<Membership>> getMembershipById(
                        @PathVariable Long id
        // @RequestHeader("Authorization") String jwt
        ) {
                Membership membership = membershipService.getMembershipById(id);
                ResponseObject<Membership> response = new ResponseObject.Builder<Membership>()
                                .content(
                                                membership)
                                .message("Get user by Id successfully")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.status(HttpStatus.OK.value()).body(response);
        }

        /**
         * Searches for memberships by name.
         *
         * This method searches for memberships matching the provided name and
         * returns a list of matching memberships.
         *
         * @param mbsName Name of the membership to search for.
         * @return ResponseEntity containing the list of matching memberships.
         */
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Search membership by name", description = "Search membership by name")

        @GetMapping("/search")
        public ResponseEntity<ResponseObject<List<Membership>>> searchMembershipByName(
                        @RequestParam String mbsName, @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size
        // @RequestHeader("Authorization") String jwt
        ) {

                // Search for memberships by name using the service
                PaginationWrapper<List<Membership>> memberships = membershipService.findMembershipByName(mbsName, page,
                                size);
                ResponseObject<List<Membership>> response = new ResponseObject.Builder<List<Membership>>()
                                .unwrapPaginationWrapper(
                                                memberships)
                                .message("Get customers list successfully!")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.ok(response);
        }

        /**
         * Creates a new membership.
         *
         * This method accepts a MembershipRequest object containing the necessary
         * details for creating a new membership and then returns the newly created
         * membership in the response.
         *
         * @param request The membership details for creation.
         * @return ResponseEntity containing the created membership.
         */
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Create membership", description = "Add a newmembership")
        @PostMapping
        public ResponseEntity<ResponseObject<Membership>> createMembership(
                        @RequestBody MembershipRequest request
        // @RequestHeader("Authorization") String jwt
        ) {

                Membership membership = membershipService.createMembership(request);
                ResponseObject<Membership> response = new ResponseObject.Builder<Membership>()
                                .content(membership)
                                .message("Create user successfully")
                                .code(HttpStatus.CREATED.value())
                                .success(true)
                                .build();

                return ResponseEntity.status(HttpStatus.CREATED.value()).body(response);

        }

        /**
         * Updates the information of an existing membership.
         *
         * This method takes a membership ID and a request object containing the
         * updated
         * membership details, and it updates the corresponding membership in the
         * database.
         *
         * @param id  The ID of the membership to update.
         * @param req The request object containing the updated membership details.
         * @return ResponseEntity containing the updated membership wrapped in an
         *         ApiResponse.
         * @throws AppException if there is an error during the update process.
         */
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Update membership", description = "Update an existing membership")
        @PutMapping("/{id}")
        public ResponseEntity<ResponseObject<Membership>> updateMembership(
                        @PathVariable Long id,
                        @RequestBody MembershipRequest req) {

                Membership updatedMembership = membershipService.updateMembership(id, req);
                ResponseObject<Membership> response = new ResponseObject.Builder<Membership>()
                                .content(updatedMembership)
                                .message("Update user successfully")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.status(HttpStatus.OK.value()).body(response);

        }

        /**
         * Deletes a membership by its ID.
         *
         * This method deletes a membership based on the provided ID and returns
         * a success response.
         *
         * @param id The ID of the membership to delete.
         * @return ResponseEntity indicating success of deletion.
         */
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Delete membership", description = "Delete membership")
        @DeleteMapping("/{id}")
        public ResponseEntity<ResponseObject<Void>> deleteMembership(
                        @PathVariable Long id
        // @RequestHeader("Authorization") String jwt
        ) {

                membershipService.deleteMembership(id);
                ResponseObject<Void> response = new ResponseObject.Builder<Void>()
                                .message("Update status of customer successfully")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.ok().body(response);
        }

        /**
         * Assigns a promotion to a user upon rank-up.
         * 
         * @param userId   The ID of the user.
         * @param mbsId    The ID of the membership.
         * @param username The username of the user.
         * @return ResponseEntity with success message.
         */
        @PostMapping("/assign-promotion")
        @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Assign Promotion on Rank Up", description = "Assign a promotion to a user based on membership rank")
        public ResponseEntity<ResponseObject<Void>> assignPromotionOnRankUp(
                        @RequestParam Long userId,
                        @RequestParam Long mbsId,
                        @RequestParam String username) {
                membershipService.assignPromotionOnRankUp(userId, mbsId, username);
                ResponseObject<Void> response = new ResponseObject.Builder<Void>()
                                .message("Promotion assigned successfully")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.ok(response);
        }

        /**
         * Retrieves promotions assigned to a user.
         * 
         * @param userId The ID of the user.
         * @return ResponseEntity with the list of promotions.
         */
        @GetMapping("/my-promotions")
        @PreAuthorize("hasRole('CUSTOMER')")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Get My Promotions", description = "Retrieve promotions assigned to a specific user")
        public ResponseEntity<ResponseObject<List<Promotion>>> getMyPromotions(@RequestParam Long userId) {
                List<Promotion> promotions = membershipService.getUserPromotions(userId);
                ResponseObject<List<Promotion>> response = new ResponseObject.Builder<List<Promotion>>()
                                .content(promotions)
                                .message("User promotions retrieved successfully")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.ok(response);
        }

        /**
         * Applies a membership to a user.
         * 
         * @param userId The ID of the user.
         * @param mbsId  The ID of the membership.
         * @return ResponseEntity with success message.
         */
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Apply membership to user", description = "Apply a membership to a user's points")
        @PostMapping("/apply-to-user")
        public ResponseEntity<ResponseObject<Void>> applyMembershipToUser(
                        @RequestParam Long userId,
                        @RequestParam Long mbsId) {
                membershipService.applyMembershipToUser(userId, mbsId);
                ResponseObject<Void> response = new ResponseObject.Builder<Void>()
                                .message("Membership applied to user successfully")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.ok(response);
        }

        /**
         * Automatically updates a user's membership based on points.
         * 
         * @param userId            The ID of the user.
         * @param accumulatedPoints The user's accumulated points.
         * @return ResponseEntity with success message.
         */
        @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Auto update membership", description = "Automatically update user's membership based on accumulated points")
        @PostMapping("/auto-update")
        public ResponseEntity<ResponseObject<Void>> autoUpdateMembership(
                        @RequestParam Long userId,
                        @RequestParam Long accumulatedPoints) {
                membershipService.autoUpdateMembership(userId, accumulatedPoints);
                ResponseObject<Void> response = new ResponseObject.Builder<Void>()
                                .message("Membership updated automatically")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.ok(response);
        }
}
