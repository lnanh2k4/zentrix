package com.zentrix.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zentrix.model.entity.Staff;
import com.zentrix.model.entity.User;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.exception.ValidationFailedException;
import com.zentrix.model.request.StaffCreationRequest;
import com.zentrix.model.request.StaffRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.model.utils.Status;
import com.zentrix.service.BranchService;
import com.zentrix.service.RoleService;
import com.zentrix.service.StaffService;
import com.zentrix.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*
* @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
* @date February 12, 2025
*/
@RestController
@RequestMapping("/api/v1/staffs")
@Tag(name = "Staff Controller", description = "This class supports StaffAPI")
public class StaffController {

        @Autowired
        private StaffService staffService;
        @Autowired
        private UserService userService;
        @Autowired
        private BranchService branchService;
        @Autowired
        private RoleService roleService;
        @Autowired
        private PasswordEncoder passwordEncoder;
        @Value("${pagination.page:0}")
        private int defaultPage;

        @Value("${pagination.size:10}")
        private int defaultSize;

        /**
         * This API allows to get staffs list
         * 
         * @param page page of list (default value: 0)
         * @param size size of list (default value: 10)
         * @return information of staff
         */
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Get All Staffs", description = "This API allows to get staffs list", operationId = "getAllStaffs", method = "GET")
        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping
        public ResponseEntity<ResponseObject<List<Staff>>> getAllStaffs(
                        @RequestParam(value = "page", required = false) Integer page,
                        @RequestParam(value = "size", required = false) Integer size) {
                int currentPage = (page != null) ? page : defaultPage;
                int currentSize = (size != null) ? size : defaultSize;
                PaginationWrapper<List<Staff>> wrapper = staffService.getAllStaffs(currentPage, currentSize);
                ResponseObject<List<Staff>> response = new ResponseObject.Builder<List<Staff>>()
                                .unwrapPaginationWrapper(wrapper)
                                .message("Get staffs list successfully!")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.ok(response);
        }

        /**
         * This API allows to get information of staff
         *
         * @param staffId identity of staff
         * @return information of staff
         */
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Get Staff", description = "This API allows to get information of staff", operationId = "getStaff", method = "GET")
        @PreAuthorize("hasAnyRole('ADMIN')")
        @GetMapping("/{staffId}")
        public ResponseEntity<ResponseObject<Staff>> getStaff(@PathVariable Long staffId) {
                Staff staff = staffService.findStaffByStaffId(staffId);
                ResponseObject<Staff> response = new ResponseObject.Builder<Staff>()
                                .content(staff)
                                .message("Get staff successfully!")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();

                return ResponseEntity.ok().body(response);
        }

        /**
         * This API allows to get information of staff
         *
         * @param username username of account
         * @return information of staff
         */
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Get Staff by username", description = "This API allows to get information of staff", operationId = "getStaff", method = "GET")
        @PreAuthorize("hasAnyRole('ADMIN','SHIPPER STAFF','WAREHOUSE STAFF', 'SELLER STAFF')")
        @GetMapping("/username/{username}")
        public ResponseEntity<ResponseObject<Staff>> getStaffByUsername(@PathVariable String username) {
                Staff staff = staffService.findStaffByUsername(username);
                ResponseObject<Staff> response = new ResponseObject.Builder<Staff>()
                                .content(staff)
                                .message("Get staff successfully!")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();

                return ResponseEntity.ok().body(response);
        }

        /**
         * This API allows to get staffs list by key word
         *
         * @param keyword name of staff
         * @param page    page of list (default value: 0)
         * @param size    size of list (default value: 10)
         * @return staffs list
         */
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Search Staffs", description = "This API allows to get staffs list by key word", operationId = "searchStaffs", method = "GET")
        @GetMapping("/search")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ResponseObject<List<Staff>>> searchStaffs(
                        @RequestParam(value = "keyword", required = true) String keyword,
                        @RequestParam(value = "page", required = false) Integer page,
                        @RequestParam(value = "size", required = false) Integer size) {
                int currentPage = (page != null) ? page : defaultPage;
                int currentSize = (size != null) ? size : defaultSize;
                Pageable pageable = PageRequest.of(currentPage, currentSize);
                PaginationWrapper<List<Staff>> wrapper = staffService.searchStaffs(keyword,
                                pageable);
                ResponseObject<List<Staff>> response = new ResponseObject.Builder<List<Staff>>()
                                .unwrapPaginationWrapper(wrapper)
                                .message("Search staffs list successfully!")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.ok(response);
        }

        /**
         * This API allows to create a new staff
         *
         * @param request staff request
         * @return information of staff
         */
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Create Staff", operationId = "createStaff", description = "This API allows to create a new staff", method = "POST")
        @PostMapping("/")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ResponseObject<Staff>> createStaff(@RequestBody @Valid StaffCreationRequest request) {
                userService.validateCustomerUniqueContract(request.getUsername(), request.getEmail(),
                                request.getPhone());
                User existUser = User.builder()
                                .username(request.getUsername())
                                .password(passwordEncoder.encode("DEF@ULT_P@$$W0RD!" + request.getUsername()))
                                .email(request.getEmail().toLowerCase())
                                .dob(request.getDob())
                                .phone(request.getPhone())
                                .firstName(request.getFirstName())
                                .lastName(request.getLastName())
                                .address(request.getAddress())
                                .sex(request.getSex())
                                .companyName(request.getCompanyName())
                                .taxCode(request.getTaxCode())
                                .roleId(roleService.findRoleById(request.getRoleId()))
                                .status(Status.VERIFYING.getValue())
                                .build();
                userService.createUser(existUser);
                Staff existStaff = Staff.builder()
                                .brchId(branchService.getBranchById(request.getBrchId()))
                                .userId(existUser)
                                .build();
                Staff staff = staffService.createStaff(existStaff);
                ResponseObject<Staff> response = new ResponseObject.Builder<Staff>()
                                .content(staff)
                                .message("Create staff successfully")
                                .code(HttpStatus.CREATED.value())
                                .success(true)
                                .build();
                return ResponseEntity.status(HttpStatus.CREATED.value()).body(response);

        }

        /**
         * This API allows to update information of staff
         *
         * @param request staff request
         * @return information of staff
         */
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Update Staff", description = "This API allows to update information of staff", operationId = "updateStaff", method = "PUT")
        @PreAuthorize("hasRole('ADMIN')")
        @PutMapping
        public ResponseEntity<ResponseObject<Staff>> updateStaff(@RequestBody @Valid StaffRequest request) {
                User existUser = userService.findUserByUserId(request.getUserId());
                if (existUser == null) {
                        throw new ValidationFailedException(AppCode.STAFF_NOT_FOUND);
                }
                if (!request.getUsername().equals(existUser.getUsername())) {
                        userService.validateCustomerUniqueUsername(request.getUsername());
                }
                if (!request.getEmail().equalsIgnoreCase(existUser.getEmail())) {
                        userService.validateCustomerUniqueEmail(request.getEmail());
                }
                if (!request.getPhone().equals(existUser.getPhone())) {
                        userService.validateCustomerUniquePhone(request.getPhone());
                }
                existUser.setUserId(request.getUserId());
                existUser.setUsername(request.getUsername());
                if (!existUser.getEmail().equalsIgnoreCase(request.getEmail())) {
                        if (existUser.getStatus() == Status.LOCK.getValue()) {
                                existUser.setStatus(Status.LOCK_VERIFY.getValue());
                        } else {
                                existUser.setStatus(Status.VERIFYING.getValue());
                        }
                } else {
                        existUser.setStatus(existUser.getStatus());
                }
                existUser.setEmail(request.getEmail().toLowerCase());
                existUser.setDob(request.getDob());
                existUser.setPhone(request.getPhone());
                existUser.setPassword(existUser.getPassword());
                existUser.setFirstName(request.getFirstName());
                existUser.setLastName(request.getLastName());
                existUser.setAddress(request.getAddress());
                existUser.setCompanyName(request.getCompanyName());
                existUser.setRoleId(roleService.findRoleById(request.getRoleId()));

                Staff existStaff = Staff.builder()
                                .userId(existUser)
                                .brchId(branchService.getBranchById(request.getBrchId()))
                                .staffId(request.getStaffId())
                                .build();
                Staff staff = staffService.updateStaff(existStaff);
                ResponseObject<Staff> response = new ResponseObject.Builder<Staff>()
                                .content(staff)
                                .message("Update user successfully")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.status(HttpStatus.OK.value()).body(response);
        }

        /**
         * This API allows to lock or unlock staff
         *
         * @param staffId identity of staff
         * @return information of staff
         */
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Lock Staff", description = "This API allows to lock or unlock staff", operationId = "lockStaff", method = "PATCH")
        @PreAuthorize("hasRole('ADMIN')")
        @PatchMapping("/{staffId}")
        public ResponseEntity<ResponseObject<Staff>> lockStaff(@PathVariable Long staffId) {
                staffService.LockAndUnlockStaff(staffId);
                ResponseObject<Staff> response = new ResponseObject.Builder<Staff>()
                                .message("Lock or Unlock staff successfully")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.ok().body(response);
        }

        /**
         * This method allows to delete staff
         *
         * @param staffId identity of staff
         * @return information of staff
         */
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Delete Staff", operationId = "deleteStaff", description = "This method allows to delete staff")
        @PreAuthorize("hasRole('ADMIN')")
        @DeleteMapping("/{staffId}")
        public ResponseEntity<ResponseObject<Staff>> deleteStaff(@PathVariable Long staffId) {
                staffService.deleteStaff(staffId);
                ResponseObject<Staff> response = new ResponseObject.Builder<Staff>()
                                .message("Update status of staff successfully")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.ok().body(response);
        }

}
