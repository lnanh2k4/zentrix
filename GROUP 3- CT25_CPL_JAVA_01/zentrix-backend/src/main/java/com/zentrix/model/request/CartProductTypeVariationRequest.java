package com.zentrix.model.request;

import com.zentrix.model.entity.Cart;
import com.zentrix.model.entity.ProductTypeVariation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class CartProductTypeVariationRequest {

    Long cartId;

    Long prodTypeVariId;

}
