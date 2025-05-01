package com.zentrix.configuration;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zentrix.model.entity.Branch;
import com.zentrix.model.entity.Category;
import com.zentrix.model.entity.Order;
import com.zentrix.model.entity.OrderDetail;
import com.zentrix.model.entity.Staff;
import com.zentrix.model.entity.Supplier;
import com.zentrix.model.entity.User;
import com.zentrix.model.exception.BaseException;
import com.zentrix.model.request.AttributeRequest;
import com.zentrix.model.request.MembershipRequest;
import com.zentrix.model.request.NotificationRequest;
import com.zentrix.model.request.OrderDetailRequest;
import com.zentrix.model.request.OrderRequest;
import com.zentrix.model.request.PostRequest;
import com.zentrix.model.request.ProductRequest;
import com.zentrix.model.request.ProductTypeAttributeRequest;
import com.zentrix.model.request.ProductTypeBranchRequest;
import com.zentrix.model.request.ProductTypeRequest;
import com.zentrix.model.request.ProductTypeVariationRequest;
import com.zentrix.model.request.ReviewRequest;
import com.zentrix.model.request.VariationRequest;
import com.zentrix.model.utils.Role;
import com.zentrix.model.utils.Sex;
import com.zentrix.model.utils.Status;
import com.zentrix.repository.OrderRepository;
import com.zentrix.service.AttributeService;
import com.zentrix.service.BranchService;
import com.zentrix.service.CategoryService;
import com.zentrix.service.FileService;
import com.zentrix.service.ImageProductTypeService;
import com.zentrix.service.ImageService;
import com.zentrix.service.MembershipService;
import com.zentrix.service.NotificationService;
import com.zentrix.service.OrderDetailService;
import com.zentrix.service.OrderService;
import com.zentrix.service.PostService;
import com.zentrix.service.ProductService;
import com.zentrix.service.ProductTypeAttributeService;
import com.zentrix.service.ProductTypeBranchService;
import com.zentrix.service.ProductTypeService;
import com.zentrix.service.ProductTypeVariationService;
import com.zentrix.service.RoleService;
import com.zentrix.service.StaffService;
import com.zentrix.service.SupplierService;
import com.zentrix.service.UserService;
import com.zentrix.service.VariationService;
import com.zentrix.service.WarrantyService;
import com.zentrix.service.PromotionService;
import com.zentrix.service.ReviewService;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/*
 * @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
 * @date  April 01, 2025
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DatabaseInitialization {

        private final OrderRepository orderRepository;
        UserService userService;
        StaffService staffService;
        PasswordEncoder passwordEncoder;
        BranchService branchService;
        RoleService roleService;
        ProductService productService;
        ProductTypeService productTypeService;
        VariationService variationService;
        ProductTypeVariationService productTypeVariationService;
        AttributeService attributeService;
        ProductTypeAttributeService productTypeAttributeService;
        ProductTypeBranchService productTypeBranchService;
        CategoryService categoryService;
        SupplierService supplierService;
        ImageService imageService;
        ImageProductTypeService imageProductTypeService;
        PromotionService promotionService;
        WarrantyService warrantyService;
        PostService postService;
        ReviewService reviewService;
        MembershipService membershipService;
        OrderService orderService;
        OrderDetailService orderDetailService;
        NotificationService notificationService;
        FileService fileService;

        @PostConstruct
        @Transactional

        public void initialize() {
                Random random = new Random();
                if (roleService.findRoleByName(Role.GUEST.getRoleName()) == null) {
                        roleService.createRole(Role.GUEST.getRoleName());
                }
                if (roleService.findRoleByName(Role.CUSTOMER.getRoleName()) == null) {
                        roleService.createRole(Role.CUSTOMER.getRoleName());
                }
                if (roleService.findRoleByName(Role.ADMIN.getRoleName()) == null) {
                        roleService.createRole(Role.ADMIN.getRoleName());
                }
                if (roleService.findRoleByName(Role.SELLER_STAFF.getRoleName()) == null) {
                        roleService.createRole(Role.SELLER_STAFF.getRoleName());
                }
                if (roleService.findRoleByName(Role.WAREHOUSE_STAFF.getRoleName()) == null) {
                        roleService.createRole(Role.WAREHOUSE_STAFF.getRoleName());
                }
                if (roleService.findRoleByName(Role.SHIPPER.getRoleName()) == null) {
                        roleService.createRole(Role.SHIPPER.getRoleName());
                }
                String[] provinces = {
                                "Hà Nội", "TP. Hồ Chí Minh", "Hải Phòng", "Đà Nẵng", "Cần Thơ",
                                "An Giang", "Bà Rịa - Vũng Tàu", "Bắc Giang", "Bắc Kạn", "Bạc Liêu",
                                "Bắc Ninh", "Bến Tre", "Bình Định", "Bình Dương", "Bình Phước",
                                "Bình Thuận", "Cà Mau", "Cao Bằng", "Đắk Lắk", "Đắk Nông",
                                "Điện Biên", "Đồng Nai", "Đồng Tháp", "Gia Lai", "Hà Giang",
                                "Hà Nam", "Hà Tĩnh", "Hải Dương", "Hậu Giang", "Hòa Bình",
                                "Hưng Yên", "Khánh Hòa", "Kiên Giang", "Kon Tum", "Lai Châu",
                                "Lâm Đồng", "Lạng Sơn", "Lào Cai", "Long An", "Nam Định",
                                "Nghệ An", "Ninh Bình", "Ninh Thuận", "Phú Thọ", "Quảng Bình",
                                "Quảng Nam", "Quảng Ngãi", "Quảng Ninh", "Quảng Trị", "Sóc Trăng",
                                "Sơn La", "Tây Ninh", "Thái Bình", "Thái Nguyên", "Thanh Hóa",
                                "Thừa Thiên Huế", "Tiền Giang", "Trà Vinh", "Tuyên Quang", "Vĩnh Long",
                                "Vĩnh Phúc", "Yên Bái", "Phú Yên"
                };
                String[] provincesCode = {
                                "HNi", "HCM", "HPg", "DaN", "CTo",
                                "AGg", "BR_VT", "BGg", "BKn", "BLu",
                                "BNh", "BTr", "BDh", "BDg", "BPc",
                                "BTn", "CMu", "CBg", "DLk", "DNg",
                                "DBn", "DgN", "DTp", "GLi", "HaG",
                                "HNm", "HTh", "HDg", "HGg", "HBh",
                                "HYn", "KHa", "KGg", "KTm", "LiC",
                                "LDg", "LSn", "LCc", "LAn", "NDh",
                                "NAn", "NBh", "NTh", "PTo", "QBh",
                                "QNm", "QNi", "QNh", "QTi", "STg",
                                "SLa", "TNh", "TBh", "TiN", "THa",
                                "TT_H", "TnG", "TVh", "TQg", "VLg",
                                "VPc", "YBi", "PYn"
                };

                if (branchService.getBranchById(1L) == null) {
                        for (int i = 0; i < provinces.length; i++) {
                                String province = provinces[i];
                                String provinceCode = provincesCode[i];
                                String phoneNumber = "09" + String.format("%07d", i + 1);
                                Branch branch = Branch.builder()
                                                .brchName(province + " branch")
                                                .address(province + " city")
                                                .phone(phoneNumber)
                                                .status(Status.ACTIVE.getValue())
                                                .build();
                                branchService.createBranch(branch);

                                for (int j = 1; j <= 5; j++) {
                                        if (userService.findUserByUsername(
                                                        "admin-zentrix.system-" + provinceCode + "-" + j) == null) {
                                                System.out.println("Status: " + Status.ACTIVE.getValue());
                                                User user = User.builder()
                                                                .username("admin-zentrix.system-" + provinceCode + "-"
                                                                                + j)
                                                                .email(provinceCode + ".admin" + "-" + j
                                                                                + ".zentrix.system@gmail.com")
                                                                .firstName("Admin " + province + " " + j)
                                                                .lastName("ZS")
                                                                .address(branch.getAddress())
                                                                .companyName("Zentrix Company")
                                                                .taxCode("0123456789")
                                                                .phone(provinceCode + "-09"
                                                                                + String.format("%07d", j + 1))
                                                                .password(passwordEncoder
                                                                                .encode("@dm$n.Zentr!x.$y$tem-"
                                                                                                + provinceCode + "-"
                                                                                                + j))
                                                                .mbsId(null)
                                                                .sex(Sex.MALE.getValue())
                                                                .status(Status.ACTIVE.getValue())
                                                                .userPoint(0)
                                                                .roleId(roleService.findRoleByName(
                                                                                Role.ADMIN.getRoleName()))
                                                                .dob(LocalDate.now())
                                                                .build();
                                                userService.createUser(user);
                                                Staff staff = Staff.builder().brchId(branch).userId(user).build();
                                                staffService.createStaff(staff);
                                        }
                                }

                                for (int k = 1; k <= 5; k++) {
                                        if (userService.findUserByUsername(
                                                        "seller.staff-zentrix.system-" + provinceCode + "-"
                                                                        + k) == null) {
                                                User user = User.builder()
                                                                .username("seller.staff-zentrix.system-" + provinceCode
                                                                                + "-" + k)
                                                                .email(provinceCode + ".seller" + "-" + k
                                                                                + ".zentrix.system@gmail.com")
                                                                .firstName("Seller " + province + " " + k)
                                                                .lastName("ZS").address(branch.getAddress())
                                                                .companyName("Zentrix Company").taxCode("0123456789")
                                                                .phone(provinceCode + "-08"
                                                                                + String.format("%07d", k + 1))
                                                                .password(passwordEncoder.encode(
                                                                                "$e11er-$t@ff.Zentr!x.$y$tem-"
                                                                                                + provinceCode + "-"
                                                                                                + k))
                                                                .mbsId(null).sex(Sex.MALE.getValue())
                                                                .status(Status.ACTIVE.getValue())
                                                                .roleId(roleService.findRoleByName(
                                                                                Role.SELLER_STAFF.getRoleName()))
                                                                .dob(LocalDate.now())
                                                                .userPoint(0).build();
                                                userService.createUser(user);
                                                Staff staff = Staff.builder().brchId(branch).userId(user).build();
                                                staffService.createStaff(staff);
                                        }
                                }

                                for (int h = 0; h <= 5; h++) {
                                        if (userService.findUserByUsername(
                                                        "warehouse.staff-zentrix.system-" + provinceCode + "-"
                                                                        + h) == null) {
                                                User user = User.builder()
                                                                .username("warehouse.staff-zentrix.system-"
                                                                                + provinceCode
                                                                                + "-" + h)
                                                                .email(provinceCode + ".warehouse" + "-" + h
                                                                                + ".zentrix.system@gmail.com")
                                                                .firstName("Warehouse " + province + " " + h)
                                                                .lastName("ZS").address(branch.getAddress())
                                                                .companyName("Zentrix Company").taxCode("0123456789")
                                                                .phone(provinceCode + "-07"
                                                                                + String.format("%07d", h + 1))
                                                                .password(passwordEncoder.encode(
                                                                                "w@rehOu$e-$t@ff.Zentr!x.$y$tem-"
                                                                                                + provinceCode + "-"
                                                                                                + h))
                                                                .mbsId(null).sex(Sex.MALE.getValue())
                                                                .status(Status.ACTIVE.getValue())
                                                                .roleId(roleService.findRoleByName(
                                                                                Role.WAREHOUSE_STAFF.getRoleName()))
                                                                .dob(LocalDate.now())
                                                                .userPoint(0).build();
                                                userService.createUser(user);
                                                Staff staff = Staff.builder().brchId(branch).userId(user).build();
                                                staffService.createStaff(staff);
                                        }
                                }

                                for (int g = 1; g <= 5; g++) {
                                        if (userService.findUserByUsername(
                                                        "shipper.staff-zentrix.system-" + provinceCode + "-"
                                                                        + g) == null) {
                                                User user = User.builder()
                                                                .username("shipper.staff-zentrix.system-" + provinceCode
                                                                                + "-"
                                                                                + g)
                                                                .email(provinceCode + ".shipper" + "-" + g
                                                                                + ".zentrix.system@gmail.com")
                                                                .firstName("Shipper " + province + " " + g)
                                                                .lastName("ZS").address(branch.getAddress())
                                                                .companyName("Zentrix Company").taxCode("0123456789")
                                                                .phone(provinceCode + "-06"
                                                                                + String.format("%07d", g + 1))
                                                                .password(passwordEncoder.encode(
                                                                                "$h1pper-$t@ff.Zentr!x.$y$tem-"
                                                                                                + provinceCode + "-"
                                                                                                + g))
                                                                .mbsId(null).sex(Sex.MALE.getValue())
                                                                .status(Status.ACTIVE.getValue())
                                                                .dob(LocalDate.now())
                                                                .roleId(roleService.findRoleByName(
                                                                                Role.SHIPPER.getRoleName()))
                                                                .userPoint(0).build();
                                                userService.createUser(user);

                                                Staff staff = Staff.builder().brchId(branch).userId(user).build();
                                                staffService.createStaff(staff);
                                        }
                                }

                                for (int y = 1; y <= 5; y++) {
                                        if (userService.findUserByUsername(
                                                        "customer-zentrix.system-" + provinceCode + "-"
                                                                        + y) == null) {
                                                User user = User.builder()
                                                                .username("customer-zentrix.system-" + provinceCode
                                                                                + "-"
                                                                                + y)
                                                                .email(provinceCode + ".customer" + "-" + y
                                                                                + ".zentrix.system@gmail.com")
                                                                .firstName("Customer " + province + " " + y)
                                                                .lastName("ZS").address(branch.getAddress())
                                                                .companyName("Zentrix Company").taxCode("0123456789")
                                                                .phone(provinceCode + "-05"
                                                                                + String.format("%07d", y + 1))
                                                                .password(passwordEncoder
                                                                                .encode("Cu$t0mer.Zentr!x.$y$tem-"
                                                                                                + provinceCode + "-"
                                                                                                + y))
                                                                .mbsId(null)
                                                                .sex(Sex.MALE.getValue())
                                                                .status(Status.ACTIVE.getValue()).userPoint(0)
                                                                .dob(LocalDate.now())
                                                                .roleId(roleService.findRoleByName(
                                                                                Role.CUSTOMER.getRoleName()))
                                                                .build();

                                                userService.createUser(user);
                                        }
                                }

                        }
                }

                if (userService.findUserByUsername("admin-zentrix.system") == null) {
                        Branch branch = branchService.getBranchById(1L);
                        System.out.println("Status: " + Status.ACTIVE.getValue());
                        User user = User.builder()
                                        .username("admin-zentrix.system")
                                        .email("zentrix.system@gmail.com")
                                        .firstName("Administration")
                                        .lastName("Zentrix System")
                                        .address(branch.getAddress())
                                        .companyName("Zentrix Company")
                                        .taxCode("0123456789")
                                        .phone("0423456789")
                                        .password(passwordEncoder.encode("@dm$n.Zentr!x.$y$tem"))
                                        .mbsId(null)
                                        .sex(Sex.MALE.getValue())
                                        .status(Status.ACTIVE.getValue())
                                        .userPoint(0)
                                        .roleId(roleService.findRoleByName(Role.ADMIN.getRoleName()))
                                        .dob(LocalDate.now())
                                        .build();
                        userService.createUser(user);
                        Staff staff = Staff.builder().brchId(branch).userId(user).build();
                        staffService.createStaff(staff);
                }
                if (userService.findUserByUsername("shipper.staff-zentrix.system") == null) {
                        Branch branch = branchService.getBranchById(1L);
                        User user = User.builder().username("shipper.staff-zentrix.system")
                                        .email("shipper.zentrix.system@gmail.com").firstName("Shipper Staff")
                                        .lastName("Zentrix System").address(branch.getAddress())
                                        .companyName("Zentrix Company").taxCode("0123456789")
                                        .phone("0323456789")
                                        .password(passwordEncoder
                                                        .encode("$h1pper-$t@ff.Zentr!x.$y$tem"))
                                        .mbsId(null).sex(Sex.MALE.getValue())
                                        .status(Status.ACTIVE.getValue())
                                        .dob(LocalDate.now())
                                        .roleId(roleService.findRoleByName(Role.SHIPPER.getRoleName()))
                                        .userPoint(0).build();
                        userService.createUser(user);
                        Staff staff = Staff.builder().brchId(branch).userId(user).build();
                        staffService.createStaff(staff);
                }
                if (userService.findUserByUsername("warehouse.staff-zentrix.system") == null) {
                        Branch branch = branchService.getBranchById(1L);
                        User user = User.builder().username("warehouse.staff-zentrix.system")
                                        .email("warehouse.zentrix.system@gmail.com").firstName("Warehouse Staff")
                                        .lastName("Zentrix System").address("Cantho city")
                                        .companyName("Zentrix Company").taxCode("0123456789")
                                        .phone("0223456789")
                                        .password(passwordEncoder
                                                        .encode("w@rehOu$e-$t@ff.Zentr!x.$y$tem"))
                                        .mbsId(null).sex(Sex.MALE.getValue())
                                        .status(Status.ACTIVE.getValue())
                                        .roleId(roleService.findRoleByName(
                                                        Role.WAREHOUSE_STAFF.getRoleName()))
                                        .dob(LocalDate.now())
                                        .userPoint(0).build();
                        userService.createUser(user);
                        Staff staff = Staff.builder().brchId(branch).userId(user).build();
                        staffService.createStaff(staff);
                }
                if (userService.findUserByUsername("seller.staff-zentrix.system") == null) {
                        Branch branch = branchService.getBranchById(1L);
                        User user = User.builder().username("seller.staff-zentrix.system")
                                        .email("seller.zentrix.system@gmail.com").firstName("Seller Staff")
                                        .lastName("Zentrix System").address(branch.getAddress())
                                        .companyName("Zentrix Company").taxCode("0123456789")
                                        .phone("0023456789")
                                        .password(passwordEncoder.encode("$e11er-$t@ff.Zentr!x.$y$tem"))
                                        .mbsId(null).sex(Sex.MALE.getValue())
                                        .status(Status.ACTIVE.getValue())
                                        .roleId(roleService.findRoleByName(
                                                        Role.SELLER_STAFF.getRoleName()))
                                        .dob(LocalDate.now())
                                        .userPoint(0).build();
                        userService.createUser(user);
                        Staff staff = Staff.builder().brchId(branch).userId(user).build();
                        staffService.createStaff(staff);
                }
                if (userService.findUserByUsername("customer-zentrix.system") == null) {
                        Branch branch = branchService.getBranchById(1L);
                        User user = User.builder().username("customer-zentrix.system")
                                        .email("customer.zentrix.system@gmail.com").firstName("Customer")
                                        .lastName("Zentrix System").address(branch.getAddress())
                                        .companyName("Zentrix Company").taxCode("0123456789")
                                        .phone("1113456789")
                                        .password(passwordEncoder.encode("Cu$t0mer.Zentr!x.$y$tem"))
                                        .mbsId(null)
                                        .sex(Sex.MALE.getValue()).status(Status.ACTIVE.getValue())
                                        .userPoint(0)
                                        .dob(LocalDate.now())
                                        .roleId(roleService.findRoleByName(Role.CUSTOMER.getRoleName()))
                                        .build();

                        userService.createUser(user);
                }

                if (categoryService.getCategoryById(1) == null) {
                        Category smartphones = Category.builder()
                                        .cateName("Smartphones")
                                        .parentCateId(null)
                                        .build();
                        categoryService.addCategory(smartphones);

                        Category androidPhones = Category.builder()
                                        .cateName("Android Phones")
                                        .parentCateId(smartphones)
                                        .build();
                        categoryService.addCategory(androidPhones);

                        Category iosPhones = Category.builder()
                                        .cateName("iPhones")
                                        .parentCateId(smartphones)
                                        .build();
                        categoryService.addCategory(iosPhones);

                        Category gamingPhones = Category.builder()
                                        .cateName("Gaming Phones")
                                        .parentCateId(smartphones)
                                        .build();
                        categoryService.addCategory(gamingPhones);

                        Category budgetPhones = Category.builder()
                                        .cateName("Budget Phones")
                                        .parentCateId(smartphones)
                                        .build();
                        categoryService.addCategory(budgetPhones);

                        Category laptops = Category.builder()
                                        .cateName("Laptops")
                                        .parentCateId(null)
                                        .build();
                        categoryService.addCategory(laptops);

                        Category gamingLaptops = Category.builder()
                                        .cateName("Gaming Laptops")
                                        .parentCateId(laptops)
                                        .build();
                        categoryService.addCategory(gamingLaptops);

                        Category ultrabooks = Category.builder()
                                        .cateName("Ultrabooks")
                                        .parentCateId(laptops)
                                        .build();
                        categoryService.addCategory(ultrabooks);

                        Category laptopAccessories = Category.builder()
                                        .cateName("Laptop Accessories")
                                        .parentCateId(laptops)
                                        .build();
                        categoryService.addCategory(laptopAccessories);

                        Category laptopBags = Category.builder()
                                        .cateName("Laptop Bags")
                                        .parentCateId(laptops)
                                        .build();
                        categoryService.addCategory(laptopBags);
                }

                if (supplierService.getSupplierById(1) == null) {
                        Supplier supplier1 = Supplier.builder()
                                        .suppName("Samsung Store")
                                        .email("contact@samsung.com")
                                        .phone("0987654321")
                                        .address("Seoul, South Korea")
                                        .build();
                        supplierService.addSupplier(supplier1);

                        Supplier supplier2 = Supplier.builder()
                                        .suppName("Apple Store")
                                        .email("support@apple.com")
                                        .phone("0912345678")
                                        .address("Cupertino, USA")
                                        .build();
                        supplierService.addSupplier(supplier2);

                        Supplier supplier3 = Supplier.builder()
                                        .suppName("Xiaomi Distributor")
                                        .email("info@xiaomi.com")
                                        .phone("0932145678")
                                        .address("Beijing, China")
                                        .build();
                        supplierService.addSupplier(supplier3);

                        Supplier supplier4 = Supplier.builder()
                                        .suppName("OnePlus Official")
                                        .email("contact@oneplus.com")
                                        .phone("0965432187")
                                        .address("Shenzhen, China")
                                        .build();
                        supplierService.addSupplier(supplier4);

                        Supplier supplier5 = Supplier.builder()
                                        .suppName("Oppo Supplier")
                                        .email("support@oppo.com")
                                        .phone("0956783421")
                                        .address("Guangdong, China")
                                        .build();
                        supplierService.addSupplier(supplier5);
                }

                if (supplierService.getSupplierById(1) == null) {
                        Supplier supplier1 = Supplier.builder()
                                        .suppName("Samsung Store")
                                        .email("contact@samsung.com")
                                        .phone("0987654321")
                                        .address("Seoul, South Korea")
                                        .build();
                        supplierService.addSupplier(supplier1);

                        Supplier supplier2 = Supplier.builder()
                                        .suppName("Apple Store")
                                        .email("support@apple.com")
                                        .phone("0912345678")
                                        .address("Cupertino, USA")
                                        .build();
                        supplierService.addSupplier(supplier2);

                        Supplier supplier3 = Supplier.builder()
                                        .suppName("Xiaomi Distributor")
                                        .email("info@xiaomi.com")
                                        .phone("0932145678")
                                        .address("Beijing, China")
                                        .build();
                        supplierService.addSupplier(supplier3);

                        Supplier supplier4 = Supplier.builder()
                                        .suppName("OnePlus Official")
                                        .email("contact@oneplus.com")
                                        .phone("0965432187")
                                        .address("Shenzhen, China")
                                        .build();
                        supplierService.addSupplier(supplier4);

                        Supplier supplier5 = Supplier.builder()
                                        .suppName("Oppo Supplier")
                                        .email("support@oppo.com")
                                        .phone("0956783421")
                                        .address("Guangdong, China")
                                        .build();
                        supplierService.addSupplier(supplier5);
                }

                // Hạng Bronze
                if (membershipService.getMembershipById(1L) == null) {
                        MembershipRequest bronze = MembershipRequest.builder()
                                        .mbsName("Bronze")
                                        .mbsPoint(0L)
                                        .mbsDescription("Provides 1 voucher valid for 1 month for new account holders, offering a direct 5% discount on products price")
                                        .build();
                        membershipService.createMembership(bronze);
                }

                // Hạng Silver
                if (membershipService.getMembershipById(2L) == null) {
                        MembershipRequest silver = MembershipRequest.builder()
                                        .mbsName("Silver")
                                        .mbsPoint(5000L)
                                        .mbsDescription("Provides 1 voucher valid for 2 months, offering a direct discount on products priced 5% discount on order")
                                        .build();
                        membershipService.createMembership(silver);
                }

                // Hạng Gold
                if (membershipService.getMembershipById(3L) == null) {
                        MembershipRequest gold = MembershipRequest.builder()
                                        .mbsName("Gold")
                                        .mbsPoint(10000L)
                                        .mbsDescription("Provides 1 voucher valid for 2 months, offering a direct discount on products priced 5% discount on any product pricede")
                                        .build();
                        membershipService.createMembership(gold);
                }

                // Hạng Platinum
                if (membershipService.getMembershipById(4L) == null) {
                        MembershipRequest platinum = MembershipRequest.builder()
                                        .mbsName("Platinum")
                                        .mbsPoint(20000L)
                                        .mbsDescription("Provides 1 voucher valid for 2 months, offering a direct discount on products priced and above 5% discount on any order")
                                        .build();
                        membershipService.createMembership(platinum);
                }

                // Hạng Diamond
                if (membershipService.getMembershipById(5L) == null) {
                        MembershipRequest diamond = MembershipRequest.builder()
                                        .mbsName("Diamond")
                                        .mbsPoint(50000L)
                                        .mbsDescription("Provides 2 vouchers valid for 2 months, each offering a direct on products priced and above 7% discount on any order")
                                        .build();
                        membershipService.createMembership(diamond);
                }

                // ! Product Area

                // !Product AREA!!!

                for (

                                long id = 1L; id <= 5L; id++) {
                        if (variationService.getById(id) == null) {
                                String variName;
                                switch ((int) id) {
                                        case 1:
                                                variName = "RAM";
                                                break;
                                        case 2:
                                                variName = "Memory";
                                                break;
                                        case 3:
                                                variName = "color";
                                                break;
                                        default:
                                                variName = "Variation " + id;
                                                break;
                                }
                                VariationRequest variation = VariationRequest.builder()
                                                .variName(variName)
                                                .build();
                                variationService.create(variation);
                        }
                }

                if (attributeService.getById(1L) == null) {
                        AttributeRequest variation = AttributeRequest.builder()
                                        .atbName("Size").build();
                        attributeService.create(variation);
                }
                if (attributeService.getById(2L) == null) {
                        AttributeRequest variation = AttributeRequest.builder()
                                        .atbName("RAM").build();
                        attributeService.create(variation);
                }
                if (attributeService.getById(3L) == null) {
                        AttributeRequest variation = AttributeRequest.builder()
                                        .atbName("Screen Size").build();
                        attributeService.create(variation);
                }
                if (attributeService.getById(4L) == null) {
                        AttributeRequest variation = AttributeRequest.builder()
                                        .atbName("Feature").build();
                        attributeService.create(variation);
                }
                if (attributeService.getById(5L) == null) {
                        AttributeRequest variation = AttributeRequest.builder()
                                        .atbName("Version").build();
                        attributeService.create(variation);
                }
                // ! Product Area
                if (productService.findProductById(1L) == null) {
                        ProductRequest product = ProductRequest.builder()
                                        .cateId(3) // iPhones
                                        .suppId(2) // Apple Store
                                        .prodName("iPhone Series")
                                        .vat((float) 0.05)
                                        .description("Premium smartphones from Apple")
                                        .build();
                        productService.createProduct(product, 3, 2);
                }
                if (productService.findProductById(2L) == null) {
                        ProductRequest product = ProductRequest.builder()
                                        .cateId(2) // Android Phones
                                        .suppId(1) // Samsung Store
                                        .prodName("Samsung Galaxy Series")
                                        .vat((float) 0.05)
                                        .description("High-performance Android phones from Samsung")
                                        .build();
                        productService.createProduct(product, 2, 1);
                }
                if (productService.findProductById(3L) == null) {
                        ProductRequest product = ProductRequest.builder()
                                        .cateId(4) // Gaming Phones
                                        .suppId(4) // OnePlus Official
                                        .prodName("OnePlus Gaming Series")
                                        .vat((float) 0.05)
                                        .description("Optimized phones for gaming enthusiasts")
                                        .build();
                        productService.createProduct(product, 4, 4);
                }
                if (productService.findProductById(4L) == null) {
                        ProductRequest product = ProductRequest.builder()
                                        .cateId(7) // Gaming Laptops
                                        .suppId(3) // Xiaomi Distributor (giả sử Xiaomi cung cấp laptop gaming)
                                        .prodName("Xiaomi Gaming Laptops")
                                        .vat((float) 0.05)
                                        .description("Powerful laptops for gaming")
                                        .build();
                        productService.createProduct(product, 7, 3);
                }
                if (productService.findProductById(5L) == null) {
                        ProductRequest product = ProductRequest.builder()
                                        .cateId(8) // Ultrabooks
                                        .suppId(5) // Oppo Supplier (giả sử Oppo cung cấp ultrabook)
                                        .prodName("Oppo Ultrabook Series")
                                        .vat((float) 0.05)
                                        .description("Slim and lightweight laptops")
                                        .build();
                        productService.createProduct(product, 8, 5);
                }
                if (productService.findProductById(6L) == null) {
                        ProductRequest product = ProductRequest.builder()
                                        .cateId(9) // Laptop Accessories
                                        .suppId(1) // Samsung Store
                                        .prodName("Samsung Laptop Accessories")
                                        .vat((float) 0.05)
                                        .description("Accessories for laptops from Samsung")
                                        .build();
                        productService.createProduct(product, 9, 1);
                }
                if (productService.findProductById(7L) == null) {
                        ProductRequest product = ProductRequest.builder()
                                        .cateId(10) // Laptop Bags
                                        .suppId(2) // Apple Store
                                        .prodName("Apple Laptop Bags")
                                        .vat((float) 0.05)
                                        .description("Stylish bags for laptops")
                                        .build();
                        productService.createProduct(product, 10, 2);
                }
                // ! ProductType Area
                // ! ProductType Area
                String[] productTypeNames = {
                                "iPhone 14 Pro Max", "iPhone 15 Plus", "iPhone 16", // iPhones (prodId = 1, cateId = 3)
                                "Samsung Galaxy S23 Ultra", "Samsung Galaxy A54 5G", "Samsung Galaxy Z Fold 5", // Android
                                                                                                                // Phones
                                                                                                                // (prodId
                                                                                                                // = 2,
                                                                                                                // cateId
                                                                                                                // = 2)
                                "OnePlus 11", "OnePlus Nord 3", "OnePlus Ace 2", // Gaming Phones (prodId = 3, cateId =
                                                                                 // 4)
                                "Xiaomi Book Pro 16", "Xiaomi Predator 15", "Xiaomi ROG Strix", // Gaming Laptops
                                                                                                // (prodId = 4, cateId =
                                                                                                // 7)
                                "Oppo AirBook 14", "Oppo SlimBook 13", "Oppo Ultra 15", // Ultrabooks (prodId = 5,
                                                                                        // cateId = 8)
                                "Samsung Charger 65W", "Samsung Cooling Pad", "Samsung Keyboard", // Laptop Accessories
                                                                                                  // (prodId = 6, cateId
                                                                                                  // = 9)
                                "Apple Leather Bag 15", "Apple Backpack 14", "Apple Sleeve 13" // Laptop Bags (prodId =
                                                                                               // 7, cateId = 10)
                };

                for (long id = 1L; id <= 21L; id++) {
                        if (productTypeService.findProductTypeById(id) == null) {
                                long prodId;
                                if (id <= 3)
                                        prodId = 1L; // iPhones
                                else if (id <= 6)
                                        prodId = 2L; // Android Phones
                                else if (id <= 9)
                                        prodId = 3L; // Gaming Phones
                                else if (id <= 12)
                                        prodId = 4L; // Gaming Laptops
                                else if (id <= 15)
                                        prodId = 5L; // Ultrabooks
                                else if (id <= 18)
                                        prodId = 6L; // Laptop Accessories
                                else
                                        prodId = 7L; // Laptop Bags

                                double prodTypePrice = 2_000_000 + (random.nextDouble() * 3_000_000);
                                double unitPrice = 500_000 + (random.nextDouble() * 1_500_000);

                                String[] units = { "Sản phẩm", "Cái", "Bộ", "Hộp", "Chiếc" };
                                String randomUnit = units[random.nextInt(units.length)];

                                ProductTypeRequest productType = ProductTypeRequest.builder()
                                                .prodId(prodId)
                                                .prodTypeName(productTypeNames[(int) (id - 1)])
                                                .prodTypePrice(Math.round(prodTypePrice * 100.0) / 100.0)
                                                .unit(randomUnit)
                                                .unitPrice(Math.round(unitPrice * 100.0) / 100.0)
                                                .build();
                                productTypeService.saveProductType(productType);
                        }
                }

                // ! ProductTypeVariation Area

                if (productTypeVariationService.getById(1L) == null) {
                        imageService.ExampleImage("Example_Image_1.png");
                        imageService.ExampleImage("Example_Image_2.jpg");
                        imageService.ExampleImage("Example_Image_3.png");
                        imageService.ExampleImage("Example_Image_4.png");
                        imageService.ExampleImage("Example_Image_5.png");
                        imageService.ExampleImage("Example_Image_6.jpg");
                        imageService.ExampleImage("Example_Image_7.jpg");
                        imageService.ExampleImage("Example_Image_8.png");
                        imageService.ExampleImage("Example_Image_9.jpg");
                        imageService.ExampleImage("Example_Image_10.jpg");
                        imageService.ExampleImage("Example_Image_11.jpg");
                        imageService.ExampleImage("Example_Image_12.jpg");
                        imageService.ExampleImage("Example_Image_13.jpg");
                        imageService.ExampleImage("Example_Image_14.png");
                        imageService.ExampleImage("Example_Image_15.png");
                        imageService.ExampleImage("Example_Image_16.jpg");
                        imageService.ExampleImage("Example_Image_17.png");
                        imageService.ExampleImage("Example_Image_18.jpg");
                        imageService.ExampleImage("Example_Image_19.jpg");
                        imageService.ExampleImage("Example_Image_20.png");
                        imageService.ExampleImage("Example_Image_21.jpg");
                        imageService.ExampleImage("Example_Image_22.jpg");
                        imageService.ExampleImage("Example_Image_23.png");
                        imageService.ExampleImage("Example_Image_24.png");
                        imageService.ExampleImage("Example_Image_25.jpg");
                        for (long imageId = 1L; imageId <= 25L; imageId++) {
                                imageProductTypeService.ExampleImageProductType(imageId, imageId);
                        }
                        for (long prodTypeId = 1L; prodTypeId <= 25L; prodTypeId++) {
                                // RAM
                                String[] rams = { "4GB", "6GB", "8GB", "10GB", "12GB" };
                                for (String ram : rams) {
                                        ProductTypeVariationRequest productTypeVariation = ProductTypeVariationRequest
                                                        .builder()
                                                        .variId(1L)
                                                        .prodTypeId(prodTypeId)
                                                        .prodTypeValue(ram)
                                                        .defaultVari(1)
                                                        .build();
                                        productTypeVariationService.create(productTypeVariation);
                                }

                                // Memory
                                String[] memories = { "128GB", "256GB", "512GB", "1TB", "2TB" };
                                for (String memory : memories) {
                                        ProductTypeVariationRequest productTypeVariation = ProductTypeVariationRequest
                                                        .builder()
                                                        .variId(2L)
                                                        .prodTypeId(prodTypeId)
                                                        .prodTypeValue(memory)
                                                        .defaultVari(1)
                                                        .build();
                                        productTypeVariationService.create(productTypeVariation);
                                }
                                // Color
                                String[] colors = { "Black", "Silver", "Gold", "Blue", "Red" };
                                for (String color : colors) {
                                        ProductTypeVariationRequest productTypeVariation = ProductTypeVariationRequest
                                                        .builder()
                                                        .variId(3L) // Color
                                                        .prodTypeId(prodTypeId)
                                                        .prodTypeValue(color)
                                                        .defaultVari(3)
                                                        .build();
                                        productTypeVariationService.create(productTypeVariation);
                                }

                        }

                }

                if (productTypeAttributeService.getById(1L) == null) {
                        for (long prodTypeId = 1L; prodTypeId <= 25L; prodTypeId++) {
                                for (long attrId = 1L; attrId <= 5L; attrId++) {
                                        String prodAtbValue;
                                        switch ((int) attrId) {
                                                case 1: // Size
                                                        String[] sizes = { "S", "M", "L", "XL", "XXL" };
                                                        prodAtbValue = sizes[random.nextInt(sizes.length)];
                                                        break;
                                                case 2: // RAM
                                                        String[] rams = { "4GB", "8GB", "16GB", "32GB", "64GB" };
                                                        prodAtbValue = rams[random.nextInt(rams.length)];
                                                        break;
                                                case 3: // Screen Size
                                                        String[] screenSizes = { "13 inch", "14 inch", "15.6 inch",
                                                                        "17 inch" };
                                                        prodAtbValue = screenSizes[random.nextInt(screenSizes.length)];
                                                        break;
                                                case 4: // Feature
                                                        String[] features = { "Waterproof", "Bluetooth", "Touchscreen",
                                                                        "4K" };
                                                        prodAtbValue = features[random.nextInt(features.length)];
                                                        break;
                                                case 5: // Version
                                                        String[] versions = { "v1.0", "v2.0", "v3.0", "Pro", "Lite" };
                                                        prodAtbValue = versions[random.nextInt(versions.length)];
                                                        break;
                                                default:
                                                        prodAtbValue = "Value " + attrId;
                                                        break;
                                        }

                                        ProductTypeAttributeRequest productTypeAttribute = ProductTypeAttributeRequest
                                                        .builder()
                                                        .atbId(attrId)
                                                        .prodTypeId(prodTypeId)
                                                        .prodAtbValue(prodAtbValue)
                                                        .build();
                                        productTypeAttributeService.create(productTypeAttribute);
                                }
                        }
                }

                if (productTypeBranchService.findProductTypeBranchById(1L) == null) {
                        // Duyệt qua 25 prodTypeId
                        for (long prodTypeId = 1L; prodTypeId <= 25L; prodTypeId++) {
                                // Với mỗi prodTypeId, tạo ngẫu nhiên một số brchId (tối đa 63)
                                for (long brchId = 1L; brchId <= 63L; brchId++) {
                                        // Random quantity từ 100 đến 2000 (bạn có thể điều chỉnh phạm vi)
                                        int quantity = 100 + random.nextInt(1901); // 1901 = 2000 - 100 + 1

                                        ProductTypeBranchRequest productTypeBranchRequest = ProductTypeBranchRequest
                                                        .builder()
                                                        .brchId(brchId)
                                                        .prodTypeId(prodTypeId)
                                                        .quantity(quantity)
                                                        .build();
                                        productTypeBranchService.saveProductTypeBranch(productTypeBranchRequest);
                                }
                        }
                }

                // Tạo 5 Order mẫu, mỗi Order có 1 OrderDetail

                for (long i = 1L; i <= 5L; i++) {
                        // Kiểm tra nếu Order với ID i chưa tồn tại, thì tạo mới
                        if (orderService.findOrderById(i) == null) {
                                // Tạo một đối tượng OrderRequest để truyền vào addOrder
                                OrderRequest orderRequest = OrderRequest.builder()
                                                .userId(i) // Giả định userId từ 1 đến 5 đã tồn tại
                                                .promId(i % 2 == 0 ? 1L : 2L) // Giả định promId luân phiên giữa 1 và 2
                                                .brchId(i) // Giả định branchId từ 1 đến 5 đã tồn tại
                                                .address("Địa chỉ mẫu " + i + ", Hà Nội")
                                                .status(1) // 1: Đang xử lý
                                                .paymentMethod(i % 2 == 0 ? "Credit Card" : "Cash On Delivery") // Luân
                                                // phiên
                                                // phương
                                                // thức
                                                // thanh
                                                // toán
                                                .build();

                                // Tạo và lưu Order
                                Order sampleOrder = orderService.addOrder(orderRequest);

                                // Tạo một OrderDetailRequest cho Order này
                                OrderDetailRequest orderDetailRequest = OrderDetailRequest.builder()
                                                .orderId(sampleOrder.getOrderId()) // Liên kết với Order vừa tạo
                                                .prodTypeBranchId(i) // Giả định prodTypeBranchId từ 1 đến 5 đã tồn tại
                                                .quantity((int) (i + 1)) // Số lượng tăng dần từ 2 đến 6
                                                .unitPrice((int) (500000 * i)) // Đơn giá tăng dần: 500k, 1M, 1.5M, 2M,
                                                // 2.5M
                                                .amountNotVat((float) (500000 * i * (i + 1))) // Tổng trước VAT =
                                                // unitPrice * quantity
                                                .vatRate(0.1f) // VAT 10%
                                                .variation("Màu " + (i % 2 == 0 ? "Đen" : "Trắng") + ", Size "
                                                                + (i <= 3 ? "M" : "L"))
                                                .build();

                                // Lưu OrderDetail
                                OrderDetail savedDetail = orderDetailService.saveOrderDetail(orderDetailRequest);

                                // Gán OrderDetail vào Order (nếu cần thiết cho mối quan hệ 1-n)
                                List<OrderDetail> orderDetails = new ArrayList<>();
                                orderDetails.add(savedDetail);
                                sampleOrder.setOrderDetails(orderDetails);

                                // Cập nhật Order trong database nếu cần (tùy thuộc cách triển khai addOrder)
                                orderRepository.save(sampleOrder);
                        }
                }

                if (postService.getPostById(1L) == null) {
                        PostRequest post1 = PostRequest.builder()
                                        .title("iPhone 16 Series Launch - 20% Off")
                                        .description("Discover the groundbreaking iPhone 16 Series and enjoy a fantastic 20% discount! Featuring cutting-edge technology and sleek design, stocks are limited. This exclusive offer is available only from March 20 to March 31, 2025, so hurry up!")
                                        .createdBy(1L)
                                        .approvedBy(2L)
                                        .createdAt(java.sql.Date.valueOf(LocalDate.of(2025, 3, 20)))
                                        .imageFiles(null)
                                        .build();
                        postService.createPost(post1);
                        postService.approvePost(1L, 2L);
                        fileService.updateUrlImageForDatabaseInit(1, "localhost:6789/uploads/Iphone-16.jpg");
                }
                if (postService.getPostById(2L) == null) {

                        PostRequest post2 = PostRequest.builder()
                                        .title("Gaming Laptop Deals - Up to $200 Off")
                                        .description("Level up your gaming experience with incredible deals on laptops from ASUS, MSI, and Acer. Save up to $200 on high-performance machines perfect for all gamers. This amazing offer lasts until April 15, 2025—don’t wait too long!")
                                        .createdBy(3L)
                                        .approvedBy(1L)
                                        .createdAt(java.sql.Date.valueOf(LocalDate.of(2025, 3, 20)))
                                        .imageFiles(null)
                                        .build();
                        postService.createPost(post2);
                        postService.approvePost(2L, 1L);
                        fileService.updateUrlImageForDatabaseInit(2, "localhost:6789/uploads/Laptop-gaming.jpg");

                }

                if (postService.getPostById(3L) == null) {

                        PostRequest post3 = PostRequest.builder()
                                        .title("Bluetooth Earbuds - Buy 1 Get 1 Free")
                                        .description("Grab a pair of premium Bluetooth earbuds and get another one absolutely free! Enjoy superior sound quality and wireless freedom. This unbeatable deal runs from March 25 to April 5, 2025—perfect chance to share with a friend!")
                                        .createdBy(5L)
                                        .approvedBy(2L)
                                        .createdAt(java.sql.Date.valueOf(LocalDate.of(2025, 3, 20)))
                                        .imageFiles(null)
                                        .build();
                        postService.createPost(post3);
                        postService.approvePost(3L, 2L);
                        fileService.updateUrlImageForDatabaseInit(3, "localhost:6789/uploads/Bluetooth.jpg");
                }

                if (postService.getPostById(4L) == null) {

                        PostRequest post4 = PostRequest.builder()
                                        .title("Samsung Galaxy S25 Pre-Order Bonus")
                                        .description("Pre-order the stunning Samsung Galaxy S25 today and unlock an exclusive gift bundle worth $120, including accessories and more. This limited-time offer is valid until April 10, 2025—secure your spot and enjoy the latest tech now!")
                                        .createdBy(7L)
                                        .approvedBy(3L)
                                        .createdAt(java.sql.Date.valueOf(LocalDate.of(2025, 3, 20)))
                                        .imageFiles(null)
                                        .build();
                        postService.createPost(post4);
                        postService.approvePost(4L, 3L);
                        fileService.updateUrlImageForDatabaseInit(4, "localhost:6789/uploads/SamsungGalaxy25.jpg");
                }

                if (postService.getPostById(5L) == null) {

                        PostRequest post5 = PostRequest.builder()
                                        .title("Weekend Flash Sale - 15% Off Accessories")
                                        .description("Boost your device with our weekend flash sale—15% off phone cases, chargers, and more! Stock up on essentials, but hurry, quantities are limited. This deal is only available from March 21 to March 23, 2025—shop now before it’s gone!")
                                        .createdBy(4L)
                                        .approvedBy(1L)
                                        .createdAt(java.sql.Date.valueOf(LocalDate.of(2025, 3, 20)))
                                        .imageFiles(null)
                                        .build();
                        postService.createPost(post5);
                        postService.approvePost(5L, 1L);
                        fileService.updateUrlImageForDatabaseInit(5, "localhost:6789/uploads/Weekend-flashsale.jpg");
                }

                try {
                        if (reviewService.getReviewById(1L) == null) {
                                ReviewRequest review1 = ReviewRequest.builder()
                                                .productId(1L)
                                                .userId(1L)
                                                .comment("The machine runs smoothly, the battery is strong. Fast delivery, very satisfied!")
                                                .rating(5)
                                                .createdAt(java.sql.Date.valueOf(LocalDate.of(2025, 3, 20)))
                                                .imageFile(null)
                                                .build();
                                reviewService.createReview(review1, true);
                                fileService.updateUrlImageReviewForDatabaseInit(1,
                                                "localhost:6789/uploads/review1.jpg");
                        }
                } catch (BaseException e) {

                }
                try {

                        if (reviewService.getReviewById(2L) == null) {
                                ReviewRequest review2 = ReviewRequest.builder()
                                                .productId(1L)
                                                .userId(1L)
                                                .comment("Nice machine, but gets a bit hot when playing heavy games.")
                                                .rating(4)
                                                .createdAt(java.sql.Date.valueOf(LocalDate.of(2025, 3, 20)))
                                                .imageFile(null)
                                                .build();
                                reviewService.createReview(review2, true);
                                fileService.updateUrlImageReviewForDatabaseInit(2,
                                                "localhost:6789/uploads/review2.jpg");
                        }

                } catch (BaseException e) {
                }
                try {
                        if (reviewService.getReviewById(3L) == null) {
                                ReviewRequest review = ReviewRequest.builder()
                                                .productId(1L)
                                                .userId(1L)
                                                .comment("Excellent performance and battery life. Delivery was quick and packaging was secure.")
                                                .rating(5)
                                                .createdAt(java.sql.Date.valueOf(LocalDate.of(2025, 3, 22)))
                                                .imageFile(null)
                                                .build();

                                reviewService.createReview(review, true);
                                fileService.updateUrlImageReviewForDatabaseInit(3,
                                                "localhost:6789/uploads/review3.jpg");
                        }
                } catch (BaseException e) {
                }

                try {

                        if (reviewService.getReviewById(4L) == null) {
                                ReviewRequest review4 = ReviewRequest.builder()
                                                .productId(2L)
                                                .userId(6L)
                                                .comment("Fast laptop, suitable for programmers. However, the battery is a bit weak.")
                                                .rating(4)
                                                .createdAt(java.sql.Date.valueOf(LocalDate.of(2025, 3, 20)))
                                                .imageFile(null)
                                                .build();
                                reviewService.createReview(review4, true);
                                fileService.updateUrlImageReviewForDatabaseInit(4,
                                                "localhost:6789/uploads/review4.jpg");
                        }
                } catch (BaseException e) {

                }
                try {

                        if (reviewService.getReviewById(5L) == null) {
                                ReviewRequest review5 = ReviewRequest.builder()
                                                .productId(5L)
                                                .userId(9L)
                                                .comment("Good price but camera is not very good. Good customer service.")
                                                .rating(3)
                                                .createdAt(java.sql.Date.valueOf(LocalDate.of(2025, 3, 20)))
                                                .imageFile(null)
                                                .build();
                                reviewService.createReview(review5, true);
                                fileService.updateUrlImageReviewForDatabaseInit(5,
                                                "localhost:6789/uploads/review5.jpg");
                        }
                } catch (BaseException e) {
                }

                if (notificationService.getNotificationById(1L) == null) {
                        NotificationRequest notification1 = NotificationRequest.builder()
                                        .createdById(1L)
                                        .title("New laptop models available!")
                                        .description("Check out our latest collection of laptops with upgraded specifications and better pricing. Limited stock available!")
                                        .createdAt(java.sql.Date.valueOf(LocalDate.of(2025, 3, 20)))
                                        .status(1)
                                        .build();

                        notificationService.createNotification(notification1);
                }

                if (notificationService.getNotificationById(2L) == null) {
                        NotificationRequest notification2 = NotificationRequest.builder()
                                        .createdById(2L)
                                        .title("Spring sale on smartphones!")
                                        .description("Enjoy discounts up to 30% on selected smartphones during our spring sale. Hurry up before the stock runs out!")
                                        .createdAt(java.sql.Date.valueOf(LocalDate.of(2025, 3, 22)))
                                        .status(1)
                                        .build();

                        notificationService.createNotification(notification2);
                }

                if (notificationService.getNotificationById(3L) == null) {
                        NotificationRequest notification3 = NotificationRequest.builder()
                                        .createdById(3L)
                                        .title("Laptop repair services now available")
                                        .description("We now offer affordable and reliable laptop repair services. Visit our store for more details.")
                                        .createdAt(java.sql.Date.valueOf(LocalDate.of(2025, 3, 23)))
                                        .status(1)
                                        .build();

                        notificationService.createNotification(notification3);
                }

                if (notificationService.getNotificationById(4L) == null) {
                        NotificationRequest notification4 = NotificationRequest.builder()
                                        .createdById(4L)
                                        .title("Exclusive deals on accessories")
                                        .description("Get up to 50% off on phone and laptop accessories. Shop now while the offer lasts!")
                                        .createdAt(java.sql.Date.valueOf(LocalDate.of(2025, 3, 24)))
                                        .status(1)
                                        .build();

                        notificationService.createNotification(notification4);
                }

                if (notificationService.getNotificationById(5L) == null) {
                        NotificationRequest notification5 = NotificationRequest.builder()
                                        .createdById(5L)
                                        .title("Customer appreciation event")
                                        .description("Join us for a special event where we show our gratitude for your support. Free refreshments and giveaways!")
                                        .createdAt(java.sql.Date.valueOf(LocalDate.of(2025, 3, 25)))
                                        .status(1)
                                        .build();

                        notificationService.createNotification(notification5);
                }

        }
}
