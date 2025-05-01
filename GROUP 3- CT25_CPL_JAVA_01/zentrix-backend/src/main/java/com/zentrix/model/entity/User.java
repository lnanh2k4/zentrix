package com.zentrix.model.entity;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/*
* @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
* @date February 11, 2025
*/

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "\"user\"", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "phone"),
        @UniqueConstraint(columnNames = "username")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long userId;
    @OneToOne
    Membership mbsId;
    @Column(length = 255, nullable = false)
    String username;
    @Column(nullable = false)
    String password;
    @Column(nullable = false, unique = true)
    String email;
    @Column(nullable = false)
    LocalDate dob;
    @Column(nullable = false, unique = true)
    String phone;
    @Column(nullable = false)
    String firstName;
    @Column(nullable = false)
    String lastName;
    String address;
    @Column(nullable = false)
    Integer sex;
    @Column(nullable = false)
    Integer status;
    Integer userPoint;
    String companyName;
    String taxCode;
    @JsonIgnore
    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL)
    List<Order> orders;
    @JsonIgnore
    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL)
    List<Cart> carts;
    @ManyToOne
    Role roleId;

    public boolean isStatus(int status) {
        return this.status == status;
    }

    @Override
    public String toString() {
        return "User [userId=" + userId + ", mbsId=" + mbsId + ", username=" + username + ", password=" + password
                + ", email=" + email + ", dob=" + dob + ", phone=" + phone + ", firstName=" + firstName + ", lastName="
                + lastName + ", address=" + address + ", sex=" + sex + ", status=" + status + ","
                + ", userPoint=" + userPoint + ", companyName=" + companyName + ", taxCode=" + taxCode + ", roleId="
                + roleId + "]";
    }

}
