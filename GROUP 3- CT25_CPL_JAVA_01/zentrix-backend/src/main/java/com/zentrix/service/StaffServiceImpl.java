package com.zentrix.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.zentrix.model.entity.Staff;
import com.zentrix.model.entity.User;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.exception.ValidationFailedException;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.utils.Role;
import com.zentrix.model.utils.Status;
import com.zentrix.repository.StaffRepository;
import com.zentrix.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/*
* @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
* @date February 13, 2025
*/
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StaffServiceImpl implements StaffService {

    StaffRepository staffRepository;
    UserRepository userRepository;

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = ActionFailedException.class)
    @Override
    public Staff createStaff(Staff staff) {
        staff.setStaffId(null);
        return staffRepository.save(staff);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = ActionFailedException.class)
    @Override
    public Staff updateStaff(Staff staff) {
        Staff existingStaff = findStaffByStaffId(staff.getStaffId());
        if (existingStaff == null) {
            throw new ValidationFailedException(AppCode.STAFF_NOT_FOUND);
        }
        existingStaff.setBrchId(staff.getBrchId());
        existingStaff.setUserId(staff.getUserId());
        return staffRepository.save(existingStaff);
    }

    @Override
    public Staff findStaffByStaffId(Long staffId) {
        Optional<Staff> staff = staffRepository.findById(staffId);
        if (staff.isPresent()) {
            if (staff.get().getUserId().getStatus() == Status.UNACTIVE.getValue()) {
                return null;
            }
        }
        return staff.get();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = ActionFailedException.class)
    @Override
    public void LockAndUnlockStaff(Long staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ValidationFailedException(AppCode.STAFF_NOT_FOUND));
        User user = staff.getUserId();
        if (user == null)
            throw new ValidationFailedException(AppCode.USER_NOT_FOUND);
        if (user.isStatus(Status.ACTIVE.getValue())) {
            user.setStatus(Status.LOCK.getValue());
        } else if (user.isStatus(Status.VERIFYING.getValue())) {
            user.setStatus(Status.LOCK_VERIFY.getValue());
        } else if (user.isStatus(Status.LOCK_VERIFY.getValue())) {
            user.setStatus(Status.VERIFYING.getValue());
        } else {
            user.setStatus(Status.ACTIVE.getValue());
        }

        staff.setUserId(user);
        staffRepository.save(staff);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = ActionFailedException.class)
    @Override
    public void deleteStaff(Long staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ValidationFailedException(AppCode.STAFF_NOT_FOUND));
        User user = staff.getUserId();
        if (user == null)
            throw new ValidationFailedException(AppCode.USER_NOT_FOUND);
        user.setUsername("customer-deleted-" + user.getUserId() + "-id-" + user.getUsername());
        user.setEmail(UUID.randomUUID().toString() + "-" + user.getEmail());
        user.setPhone(UUID.randomUUID().toString() + "-" + user.getPhone());
        user.setStatus(Status.UNACTIVE.getValue());
        staff.setUserId(user);
        staffRepository.save(staff);
    }

    @Override
    public PaginationWrapper<List<Staff>> searchStaffs(String keyword, Pageable pageable) {
        Page<Staff> staffPage = staffRepository.searchStaffs(keyword, pageable, Status.UNACTIVE.getValue(),
                Role.CUSTOMER.name());
        if (staffPage.getContent().isEmpty()) {
            throw new ValidationFailedException(AppCode.STAFF_NOT_FOUND);
        }
        return new PaginationWrapper.Builder<List<Staff>>()
                .setData(staffPage.getContent())
                .setPaginationInfo(staffPage)
                .build();
    }

    @Override
    public Staff findStaffByUserId(Long userId) {
        Staff staff = staffRepository.findStaffByUserId(userRepository.findById(userId)
                .orElseThrow(() -> new ValidationFailedException(AppCode.USER_NOT_FOUND)));

        if (staff.getUserId().getStatus() == Status.UNACTIVE.getValue()) {
            return null;
        }

        return staff;
    }

    @Override
    public Staff findStaffByUsername(String username) {
        User user = userRepository.findUserByUsername(username);
        if (user == null) {
            throw new ValidationFailedException(AppCode.USER_NOT_FOUND);
        }
        return staffRepository.findStaffByUserId(user);
    }

    @Override
    public PaginationWrapper<List<Staff>> getAllStaffs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Staff> staffPage = staffRepository.findAll(pageable, Status.UNACTIVE.getValue(),
                Role.CUSTOMER.name());
        return new PaginationWrapper.Builder<List<Staff>>()
                .setData(staffPage.getContent())
                .setPaginationInfo(staffPage)
                .build();
    }

}
