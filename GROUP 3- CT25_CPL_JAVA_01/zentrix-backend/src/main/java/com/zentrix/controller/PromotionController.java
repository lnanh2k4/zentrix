package com.zentrix.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.zentrix.model.entity.Promotion;
import com.zentrix.model.entity.User;
import com.zentrix.model.entity.UserPromotion;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.request.PromotionRequest;
import com.zentrix.model.request.UserPromotionRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.PromotionService;
import com.zentrix.service.StaffService;
import com.zentrix.service.UserPromotionService;
import com.zentrix.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Controller class responsible for handling HTTP requests related to
 * promotions.
 * Provides endpoints for creating, updating, retrieving, searching, filtering,
 * claiming, and deleting promotions.
 * Secured with Spring Security using role-based access control.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping("/api/v1/promotions")
@Tag(name = "Promotion Controller", description = "APIs for managing promotions.")
public class PromotionController {

        PromotionService promotionService;
        UserPromotionService userPromotionService;
        StaffService staffService;
        UserRepository userRepository;

        /**
         * Retrieves the currently authenticated user from the security context.
         * 
         * @return the authenticated User object
         * @throws ActionFailedException if the user is not authenticated or not found
         *                               in the database
         */
        private User getCurrentUser() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated()) {
                        throw new ActionFailedException(AppCode.USER_NOT_AUTHORIZED);
                }
                User user = userRepository.findUserByUsername(authentication.getName());
                if (user == null) {
                        throw new ActionFailedException(AppCode.USER_NOT_FOUND);
                }
                return user;
        }

        /**
         * Checks if the given user has the 'Admin' role.
         * 
         * @param user the User object to check
         * @return true if the user is an Admin, false otherwise
         */
        private boolean isAdmin(User user) {
                return user.getRoleId() != null && "Admin".equalsIgnoreCase(user.getRoleId().getRoleName());
        }

        /**
         * Creates a new promotion with the provided details.
         * Accessible only to users with 'SELLER STAFF' or 'ADMIN' roles.
         * If the user is an admin, their ID is automatically set as createdBy.
         * 
         * @param promotionRequest the request object containing promotion details
         * @return ResponseEntity with the created Promotion and HTTP status 201
         *         (Created)
         * @throws ActionFailedException if staff validation fails or creation process
         *                               encounters an error
         */
        @PreAuthorize("hasAnyRole('SELLER STAFF', 'ADMIN')")
        @PostMapping
        @Operation(summary = "Create promotion", description = "Creates a new promotion with the provided details.", security = @SecurityRequirement(name = "Authorization"))
        public ResponseEntity<ResponseObject<Promotion>> createPromotion(
                        @RequestBody PromotionRequest promotionRequest) {
                User currentUser = getCurrentUser();
                if (isAdmin(currentUser)) {
                        promotionRequest.setCreatedBy(currentUser.getUserId());
                } else if (promotionRequest.getCreatedBy() != null) {
                        staffService.findStaffByUserId(promotionRequest.getCreatedBy());
                }
                Promotion promotion = promotionService.createPromotion(promotionRequest);
                ResponseObject<Promotion> response = new ResponseObject.Builder<Promotion>()
                                .content(promotion)
                                .message(AppCode.PROMOTION_CREATION_SUCCESSFUL.getMessage())
                                .code(HttpStatus.CREATED.value())
                                .success(true)
                                .build();
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        /**
         * Updates an existing promotion identified by the given ID.
         * Accessible only to users with 'SELLER STAFF' or 'ADMIN' roles.
         * If the user is an admin, their ID is set as approvedBy.
         * 
         * @param id               the ID of the promotion to update
         * @param promotionRequest the request object containing updated promotion
         *                         details
         * @return ResponseEntity with the updated Promotion and HTTP status 200 (OK)
         * @throws ActionFailedException if the promotion ID is null or update fails
         */
        @PreAuthorize("hasAnyRole('SELLER STAFF', 'ADMIN')")
        @PutMapping("/{id}")
        @Operation(summary = "Update promotion", description = "Updates an existing promotion based on the given ID.", security = @SecurityRequirement(name = "Authorization"))
        public ResponseEntity<ResponseObject<Promotion>> updatePromotion(
                        @PathVariable Long id,
                        @RequestBody PromotionRequest promotionRequest) {
                if (id == null) {
                        throw new ActionFailedException(AppCode.PROMOTION_NOT_FOUND);
                }
                User currentUser = getCurrentUser();
                if (isAdmin(currentUser)) {
                        promotionRequest.setApprovedBy(currentUser.getUserId());
                }
                Promotion updatedPromotion = promotionService.updatePromotion(id, promotionRequest);
                ResponseObject<Promotion> response = new ResponseObject.Builder<Promotion>()
                                .content(updatedPromotion)
                                .message("Promotion updated successfully")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.ok(response);
        }

        /**
         * Allows a user to claim a promotion by providing promotion ID and user ID.
         * Accessible to users with 'CUSTOMER' or 'ADMIN' roles.
         * 
         * @param promId the ID of the promotion to claim
         * @param userId the ID of the user claiming the promotion
         * @return ResponseEntity with the claimed UserPromotion and HTTP status 200
         *         (OK), or 400 (Bad Request) if claim fails
         */
        @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
        @PostMapping("/claim")
        @Operation(summary = "Claim a promotion", security = @SecurityRequirement(name = "Authorization"))
        public ResponseEntity<ResponseObject<UserPromotion>> claimPromotion(
                        @RequestParam Long promId,
                        @RequestParam Long userId) {
                UserPromotionRequest request = new UserPromotionRequest();
                request.setPromId(promId);
                request.setUserId(userId);
                request.setStatus(1);
                try {
                        UserPromotion userPromotion = userPromotionService.claimPromotion(request, promId, userId);
                        return ResponseEntity.ok(new ResponseObject.Builder<UserPromotion>()
                                        .content(userPromotion)
                                        .message("Promotion claimed successfully")
                                        .code(HttpStatus.OK.value())
                                        .success(true)
                                        .build());
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(new ResponseObject.Builder<UserPromotion>()
                                                        .message("Failed to claim promotion: " + e.getMessage())
                                                        .code(HttpStatus.BAD_REQUEST.value())
                                                        .success(false)
                                                        .build());
                }
        }

        /**
         * Retrieves a promotion by its ID.
         * Accessible to users with 'CUSTOMER', 'SELLER STAFF', or 'ADMIN' roles.
         * 
         * @param id the ID of the promotion to retrieve
         * @return ResponseEntity with the Promotion if found (HTTP 200), or not found
         *         response (HTTP 404)
         */
        @PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER STAFF', 'ADMIN')")
        @GetMapping("/{id}")
        @Operation(summary = "Get promotion by ID", description = "Retrieves a specific promotion by its ID.", security = @SecurityRequirement(name = "Authorization"))
        public ResponseEntity<ResponseObject<Promotion>> getPromotionById(
                        @PathVariable Long id) {
                Promotion promotion = promotionService.findPromotionById(id);
                if (promotion != null) {
                        ResponseObject<Promotion> response = new ResponseObject.Builder<Promotion>()
                                        .content(promotion)
                                        .message("Promotion found")
                                        .code(HttpStatus.OK.value())
                                        .success(true)
                                        .build();
                        return ResponseEntity.ok(response);
                }
                ResponseObject<Promotion> response = new ResponseObject.Builder<Promotion>()
                                .content(null)
                                .message("Promotion not found")
                                .code(HttpStatus.NOT_FOUND.value())
                                .success(false)
                                .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        /**
         * Retrieves a paginated list of all promotions.
         * Accessible only to users with 'SELLER STAFF' or 'ADMIN' roles.
         * 
         * @param page the page number (default is 0)
         * @param size the number of items per page (default is 10)
         * @param sort the sorting criteria (default is "promId,desc")
         * @return ResponseEntity with a paginated list of Promotions and HTTP status
         *         200 (OK)
         */
        @PreAuthorize("hasAnyRole('SELLER STAFF', 'ADMIN')")
        @GetMapping
        @Operation(summary = "Get all promotions", description = "Fetches a paginated list of all available promotions.", security = @SecurityRequirement(name = "Authorization"))
        public ResponseEntity<ResponseObject<List<Promotion>>> getAllPromotions(
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "10") int size,
                        @RequestParam(value = "sort", defaultValue = "promId,desc") String sort) {
                PaginationWrapper<List<Promotion>> promotions = promotionService.getPromotions(page, size, sort);
                ResponseObject<List<Promotion>> response = new ResponseObject.Builder<List<Promotion>>()
                                .unwrapPaginationWrapper(promotions)
                                .message("List of all promotions retrieved successfully")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.ok(response);
        }

        /**
         * Searches for promotions based on a keyword with pagination.
         * Accessible only to users with 'SELLER STAFF' or 'ADMIN' roles.
         * 
         * @param keyword the search keyword
         * @param page    the page number (default is 0)
         * @param size    the number of items per page (default is 10)
         * @param sort    the sorting criteria (default is "promId,desc")
         * @return ResponseEntity with a paginated list of matching Promotions and HTTP
         *         status 200 (OK)
         */
        @PreAuthorize("hasAnyRole('SELLER STAFF', 'ADMIN')")
        @GetMapping("/search")
        @Operation(summary = "Search promotion", description = "Searches for promotions based on provided keywords with pagination.", security = @SecurityRequirement(name = "Authorization"))
        public ResponseEntity<ResponseObject<List<Promotion>>> searchPromotions(
                        @RequestParam("keyword") String keyword,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "10") int size,
                        @RequestParam(value = "sort", defaultValue = "promId,desc") String sort) {
                PaginationWrapper<List<Promotion>> result = promotionService.searchPromotion(keyword, page, size, sort);
                ResponseObject<List<Promotion>> response = new ResponseObject.Builder<List<Promotion>>()
                                .unwrapPaginationWrapper(result)
                                .message("Search results retrieved successfully")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.ok(response);
        }

        /**
         * Filters promotions by status or date with pagination.
         * Accessible to users with 'CUSTOMER', 'SELLER STAFF', or 'ADMIN' roles.
         * 
         * @param status the promotion status (optional: active/inactive)
         * @param date   the specific date to filter by (optional)
         * @param page   the page number (default is 0)
         * @param size   the number of items per page (default is 10)
         * @param sort   the sorting criteria (default is "promId,desc")
         * @return ResponseEntity with a paginated list of filtered Promotions and HTTP
         *         status 200 (OK)
         * @throws ActionFailedException if both status and date are provided
         */
        @PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER STAFF', 'ADMIN')")
        @GetMapping("/filter")
        @Operation(summary = "Filter promotions", description = "Filters promotions by status (active/inactive) or by a specific date.", security = @SecurityRequirement(name = "Authorization"))
        public ResponseEntity<ResponseObject<List<Promotion>>> filterPromotions(
                        @RequestParam(value = "status", required = false) String status,
                        @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "10") int size,
                        @RequestParam(value = "sort", defaultValue = "promId,desc") String sort) {
                if (status != null && date != null) {
                        throw new ActionFailedException(AppCode.INVALID_FILTER_COMBINATION);
                }
                PaginationWrapper<List<Promotion>> result = promotionService.filterPromotions(status, date, page, size,
                                sort);
                ResponseObject<List<Promotion>> response = new ResponseObject.Builder<List<Promotion>>()
                                .unwrapPaginationWrapper(result)
                                .message("Filtered promotions retrieved successfully")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.ok(response);
        }

        /**
         * Retrieves all promotions claimed by a specific user.
         * Accessible to users with 'CUSTOMER', 'SELLER STAFF', or 'ADMIN' roles.
         * 
         * @param userId the ID of the user whose promotions are to be retrieved
         * @return ResponseEntity with a list of UserPromotions and HTTP status 200
         *         (OK), or 400 (Bad Request) if fetch fails
         */
        @PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER STAFF', 'ADMIN')")
        @GetMapping("/my-promotions")
        @Operation(summary = "Get user's promotions", security = @SecurityRequirement(name = "Authorization"))
        public ResponseEntity<ResponseObject<List<UserPromotion>>> getUserPromotions(
                        @RequestParam Long userId) {
                try {
                        List<UserPromotion> userPromotions = userPromotionService.findAllUserPromotionByUserId(userId);
                        return ResponseEntity.ok(new ResponseObject.Builder<List<UserPromotion>>()
                                        .content(userPromotions)
                                        .message("User promotions fetched successfully")
                                        .code(HttpStatus.OK.value())
                                        .success(true)
                                        .build());
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(new ResponseObject.Builder<List<UserPromotion>>()
                                                        .message("Failed to fetch user promotions: " + e.getMessage())
                                                        .code(HttpStatus.BAD_REQUEST.value())
                                                        .success(false)
                                                        .build());
                }
        }

        /**
         * Checks if a promotion code already exists.
         * No role-based restriction, but requires authentication due to security
         * requirement.
         * 
         * @param promCode the promotion code to check
         * @return ResponseEntity with a map indicating existence status and HTTP status
         *         200 (OK)
         */
        @GetMapping("/check-promcode")
        @Operation(summary = "Check promotion code existence", security = @SecurityRequirement(name = "Authorization"))
        public ResponseEntity<Map<String, Boolean>> checkPromCodeExists(@RequestParam("promCode") String promCode) {
                boolean exists = promotionService.existsByPromCode(promCode);
                Map<String, Boolean> response = new HashMap<>();
                response.put("success", true);
                response.put("exists", exists);
                return ResponseEntity.ok(response);
        }

        /**
         * Deletes a promotion by its ID.
         * Accessible only to users with 'SELLER STAFF' or 'ADMIN' roles.
         * 
         * @param id the ID of the promotion to delete
         * @return ResponseEntity with the deleted Promotion and HTTP status 200 (OK)
         */
        @PreAuthorize("hasAnyRole('SELLER STAFF', 'ADMIN')")
        @DeleteMapping("/{id}")
        @Operation(summary = "Delete promotion", description = "Deletes a promotion by its ID.", security = @SecurityRequirement(name = "Authorization"))
        public ResponseEntity<ResponseObject<Promotion>> deletePromotion(@PathVariable Long id) {
                Promotion deletedPromotion = promotionService.deletePromotion(id);
                ResponseObject<Promotion> response = new ResponseObject.Builder<Promotion>()
                                .content(deletedPromotion)
                                .message("Promotion deleted successfully")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.ok(response);
        }
}