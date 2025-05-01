package com.zentrix.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.zentrix.model.entity.Staff;
import com.zentrix.model.entity.User;
import com.zentrix.model.entity.ProductType;
import com.zentrix.model.entity.Warranty;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.request.WarrantyRequest;
import com.zentrix.repository.UserRepository;
import com.zentrix.repository.ProductTypeRepository;
import com.zentrix.repository.WarrantyRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/*
 * @author Dang Cong Khanh - CE180117 - CT25_CPL_JAVA_01
 * @date February 11, 2025
 */

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class WarrantyServiceImpl implements WarrantyService {

    WarrantyRepository warrantyRepository;
    UserRepository userRepository;
    ProductTypeRepository productTypeRepository;
    StaffService staffService;

    @Override
    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    public Warranty addWarranty(WarrantyRequest request, Long createdById) {
        Warranty warranty = new Warranty();

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ActionFailedException(AppCode.USER_NOT_FOUND));
        warranty.setUserId(user);

        ProductType productType = productTypeRepository.findById(request.getProdTypeId())
                .orElseThrow(() -> new ActionFailedException(AppCode.PRODUCT_TYPE_NOT_FOUND));
        warranty.setProdTypeId(productType);

        Staff staff = null;
        if (createdById != null) {
            staff = staffService.findStaffByUserId(createdById);
            if (staff == null) {
                throw new ActionFailedException(AppCode.STAFF_NOT_FOUND);
            }
        } else {
            throw new ActionFailedException(AppCode.INVALID_INPUT);
        }
        warranty.setCreatedBy(staff);

        if (request.getWarnStartDate() == null || request.getWarnEndDate() == null) {
            throw new ActionFailedException(AppCode.INVALID_INPUT);
        }
        warranty.setWarnStartDate(request.getWarnStartDate());
        warranty.setWarnEndDate(request.getWarnEndDate());
        warranty.setDescription(request.getDescription());
        warranty.setReceive(request.getReceive());
        warranty.setStatus(1);
        return warrantyRepository.save(warranty);
    }

    @Override
    public Warranty findWarrantyById(Long warnId) {
        return warrantyRepository.findById(warnId).orElse(null);
    }

    @Override
    public Page<Warranty> findAllWarranty(Pageable pageable) {
        return warrantyRepository.findAll(pageable);
    }

    @Override
    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    public Warranty updateWarranty(WarrantyRequest request, Long warnId) {
        Warranty warranty = warrantyRepository.findById(warnId)
                .orElseThrow(() -> new ActionFailedException(AppCode.WARRANTY_NOT_FOUND));

        if (request.getWarnEndDate() != null) {
            warranty.setWarnEndDate(request.getWarnEndDate());
        }
        warranty.setStatus(request.getStatus());
        warranty.setDescription(request.getDescription());
        return warrantyRepository.save(warranty);
    }

    @Override
    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    public void deleteWarranty(Long warnId) {
        warrantyRepository.deleteById(warnId);
    }

    @Override
    public Page<Warranty> findWarrantyByUserPhone(String phone, Pageable pageable) {
        Page<Warranty> warranties = warrantyRepository.findByUserPhone(phone, pageable);
        return warranties;
    }
}