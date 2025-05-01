package com.zentrix.model.entity;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
/*
 * @author Nguyen Thanh Binh - CE171099 - CT25_CPL_JAVA_01
 * @date February 11, 2025
 */

@Entity
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class Branch {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long brchId;

    // Attribute
    @Column(length = 255, nullable = false)
    private String brchName;

    @Column(length = 255)
    private String address;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private Integer status;

    @JsonIgnore
    @OneToMany(mappedBy = "stockId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Stock> stocks;

    @JsonIgnore
    @OneToMany(mappedBy = "brchId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductTypeBranch> productTypeBranches;

    @JsonIgnore
    @OneToMany(mappedBy = "orderId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;

    @Override
    public String toString() {
        return "Branch [brchId=" + brchId + ", brchName=" + brchName + ", address=" + address + ", phone=" + phone
                + ", status=" + status;
    }

}