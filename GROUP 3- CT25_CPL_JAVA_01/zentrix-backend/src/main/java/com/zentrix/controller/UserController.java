package com.zentrix.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zentrix.model.entity.User;
import com.zentrix.model.request.UserRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.model.utils.Role;
import com.zentrix.model.utils.Status;
import com.zentrix.service.RoleService;
import com.zentrix.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;

/*
* @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
* @date February 11, 2025
*/
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Controller", description = "This class supports CustomerAPI")
public class UserController {
        @Value("${pagination.page:0}")
        private int defaultPage;

        @Value("${pagination.size:10}")
        private int defaultSize;

        @Autowired
        private UserService userService;

        @Autowired
        private RoleService roleService;

        @Autowired
        private PasswordEncoder passwordEncoder;

        /**
         * This API allows to get all customers
         * 
         * @param page page of list (default value: 0)
         * @param size size of list (default value: 10)
         * @return list of users
         */
        @GetMapping
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Get All Customer", description = "This API allows to get all customers", method = "GET", operationId = "3")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ResponseObject<List<User>>> getAllCustomers(
                        @RequestParam(value = "page", required = false) Integer page,
                        @RequestParam(value = "size", required = false) Integer size) {
                int currentPage = (page != null) ? page : defaultPage;
                int currentSize = (size != null) ? size : defaultSize;
                PaginationWrapper<List<User>> wrapper = userService.getAllUsers(currentPage, currentSize);
                ResponseObject<List<User>> response = new ResponseObject.Builder<List<User>>()
                                .unwrapPaginationWrapper(wrapper)
                                .message("Get customers list successfully!")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.ok(response);
        }

        /**
         * This API allows to search users by full name
         * 
         * @param keyword name of customers
         * @param page    page of list (default value: 0)
         * @param size    size of list (default value: 10)
         * @return list of users
         */
        @GetMapping("/search")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "search Customers", description = "This API allows to search users by full name", method = "GET", operationId = "4")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ResponseObject<List<User>>> searchUsers(
                        @RequestParam(value = "keyword", required = true) String keyword,
                        @RequestParam(value = "page", required = false) Integer page,
                        @RequestParam(value = "size", required = false) Integer size) {
                int currentPage = (page != null) ? page : defaultPage;
                int currentSize = (size != null) ? size : defaultSize;
                PaginationWrapper<List<User>> wrapper = userService.searchUser(keyword,
                                currentPage, currentSize);
                ResponseObject<List<User>> response = new ResponseObject.Builder<List<User>>()
                                .unwrapPaginationWrapper(wrapper)
                                .message("Get customers list successfully!")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.ok(response);
        }

        /**
         * This API allows to get information of customer by user ID
         *
         * @param userId identity of user
         * @return information of customer
         */
        @GetMapping("/{id}")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Get Customer By Id", description = "This API allows to get information of customer by user ID", method = "GET", operationId = "5")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ResponseObject<User>> getCustomer(@PathVariable("id") Long id) {
                User user = userService.findUserByUserId(id);
                ResponseObject<User> response = new ResponseObject.Builder<User>()
                                .content(user)
                                .message("Get user by Id successfully")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.status(HttpStatus.OK.value()).body(response);
        }

        /**
         * This API allows to get All roles of users
         * 
         * @return list of roles
         */
        @GetMapping("/roles")
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Get All Roles", description = "This API allows to get All roles of users", method = "GET", operationId = "6")
        @PreAuthorize("permitAll()")
        public ResponseEntity<ResponseObject<List<com.zentrix.model.entity.Role>>> getAllRoles() {
                List<com.zentrix.model.entity.Role> roles = roleService.getAllRoles();
                System.out.println(roles);
                ResponseObject<List<com.zentrix.model.entity.Role>> response = new ResponseObject.Builder<List<com.zentrix.model.entity.Role>>()
                                .content(roles)
                                .message("Get all role successfully")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.status(HttpStatus.OK.value()).body(response);
        }

        /**
         * This API allows to search Customer by phone
         * 
         * @param phoneNumber phone number of customer
         * @return list of customers
         */
        @GetMapping("/phone/{phoneNumber}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(security = @SecurityRequirement(name = "Authorization"), operationId = "7", summary = "Search User By Phone", description = "This API allows to search Customer by phone")
        public ResponseEntity<ResponseObject<User>> searchUserByPhone(
                        @PathVariable("phoneNumber") String phoneNumber) {
                User user = userService.findUsersByPhone(phoneNumber);
                if (user == null) {
                        ResponseObject<User> response = new ResponseObject.Builder<User>()
                                        .message("No users found with this phone number")
                                        .code(HttpStatus.NOT_FOUND.value())
                                        .success(false)
                                        .content(null)
                                        .build();
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
                ResponseObject<User> response = new ResponseObject.Builder<User>()
                                .message("Search user by phone successfully!")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .content(user)
                                .build();
                return ResponseEntity.ok().body(response);
        }

        /**
         * This API allows to create a new user with CUSTOMER role
         *
         * @param UserRequest information of user
         * @return information of customer
         */
        @PostMapping
        @Operation(security = @SecurityRequirement(name = "Authorization"), summary = "Create Customer", method = "POST", operationId = "8", description = "This API allows to create a new user with CUSTOMER role")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ResponseObject<User>> createCustomer(@RequestBody @Valid UserRequest request) {
                User existUser = User.builder()
                                .username(request.getUsername())
                                .password(passwordEncoder.encode("DEF@ULT_P@$$W0RD!"))
                                .email(request.getEmail().toLowerCase())
                                .dob(request.getDob())
                                .phone(request.getPhone())
                                .firstName(request.getFirstName())
                                .lastName(request.getLastName())
                                .address(request.getAddress())
                                .sex(request.getSex())
                                .companyName(request.getCompanyName())
                                .taxCode(request.getTaxCode())
                                .roleId(roleService.findRoleByName(Role.CUSTOMER.getRoleName()))
                                .status(Status.VERIFYING.getValue())
                                .build();
                User user = userService.createUser(existUser);
                ResponseObject<User> response = new ResponseObject.Builder<User>()
                                .content(user)
                                .message("Create user successfully")
                                .code(HttpStatus.CREATED.value())
                                .success(true)
                                .build();
                return ResponseEntity.status(HttpStatus.CREATED.value()).body(response);
        }

        /**
         * This API allows to create a new user with CUSTOMER role
         *
         * @param request information of user
         * @return information of customer
         */
        @PutMapping
        @Operation(security = @SecurityRequirement(name = "Authorization"), operationId = "9", summary = "Update Customer", description = "This API allows to create a new user with CUSTOMER role")
        @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
        public ResponseEntity<ResponseObject<User>> updateCustomer(@RequestBody @Valid UserRequest request) {
                System.out.println("=============");
                System.out.println("Da vao day ne");
                System.out.println("=============");
                User existUser = User.builder()
                                .userId(request.getUserId())
                                .username(request.getUsername())
                                .email(request.getEmail().toLowerCase())
                                .dob(request.getDob())
                                .phone(request.getPhone())
                                .firstName(request.getFirstName())
                                .lastName(request.getLastName())
                                .address(request.getAddress())
                                .sex(request.getSex())
                                .companyName(request.getCompanyName())
                                .taxCode(request.getTaxCode())
                                .build();
                User user = userService.updateUser(existUser);
                ResponseObject<User> response = new ResponseObject.Builder<User>()
                                .content(user)
                                .message("Update user successfully")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.status(HttpStatus.OK.value()).body(response);
        }

        /**
         * This API allows to change status in order to lock or unlock customer
         * account
         *
         * @param userId identity of user
         * @return customer's information
         */
        @PatchMapping("/{userId}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(security = @SecurityRequirement(name = "Authorization"), operationId = "10", summary = "Lock Customer", description = "This API allows to change status in order to lock or unlock customer account")
        public ResponseEntity<ResponseObject<User>> lockCustomer(@PathVariable Long userId) {
                userService.LockAndUnlockUser(userId);
                ResponseObject<User> response = new ResponseObject.Builder<User>()
                                .message("Lock or Unlock customer successfully")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.ok().body(response);
        }

        /**
         * This API allows to change status of user into UNACTIVE
         *
         * @param userId identity of user
         * @return information of customer
         */
        @DeleteMapping("/{userId}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(security = @SecurityRequirement(name = "Authorization"), operationId = "11", summary = "Delete User", description = "This API allows to change status of user into UNACTIVE")
        public ResponseEntity<ResponseObject<User>> deleteUser(@PathVariable Long userId) {
                userService.deleteUser(userId);
                ResponseObject<User> response = new ResponseObject.Builder<User>()
                                .message("Update status of customer successfully")
                                .code(HttpStatus.OK.value())
                                .success(true)
                                .build();
                return ResponseEntity.ok().body(response);
        }

}
