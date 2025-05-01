package com.zentrix.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.zentrix.model.entity.User;
import com.zentrix.model.entity.UserToken;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.exception.ValidationFailedException;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.utils.Role;
import com.zentrix.model.utils.Status;
import com.zentrix.repository.RoleRepository;
import com.zentrix.repository.UserRepository;
import com.zentrix.repository.UserTokenRepository;

/*
* @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
* @date February 11, 2025
*/

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserTokenRepository userTokenRepository;
    @Autowired
    private EmailService emailService;

    public void validateCustomerUniqueContract(String username, String email, String phone) {
        validateCustomerUniqueUsername(username);
        validateCustomerUniqueEmail(email);
        validateCustomerUniquePhone(phone);
    }

    @Override
    public void verifyRequest(String identifier, String method, String type) {
        UserToken resetToken = new UserToken();
        if ("email".equalsIgnoreCase(method)) {
            Optional<User> existUser = userRepository.findByEmail(identifier);
            if (!existUser.isPresent()) {
                throw new ValidationFailedException(AppCode.USER_NOT_FOUND);
            }
            User user = existUser.get();
            Optional<UserToken> userToken = userTokenRepository.findByUserId(user);
            if (userToken.isPresent()) {
                userTokenRepository.delete(userToken.get());
            }
            resetToken.setUserId(user);
            String token = UUID.randomUUID().toString();
            resetToken.setToken(token);
            resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));
            userTokenRepository.save(resetToken);
            if ("verify".equalsIgnoreCase(type)) {
                emailService.sendEmailWithTokenForVerifyAccount(user.getEmail(), token);
            } else {
                emailService.sendEmailWithTokenForResetPassword(user.getEmail(), token);
            }
        }

    }

    @Override
    public void resetPasswordWithOtp(String otp, String newPassword) {
        UserToken resetToken = userTokenRepository.findByOtp(otp)
                .orElseThrow(() -> new ValidationFailedException(AppCode.OTP_INVALID));
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ValidationFailedException(AppCode.OTP_EXPIRED);
        }
        User user = resetToken.getUserId();
        user.setPassword(newPassword);
        userRepository.save(user);
        userTokenRepository.delete(resetToken);
    }

    @Override
    public boolean verifyWithToken(String token) {
        UserToken resetToken = userTokenRepository.findByToken(token)
                .orElseThrow(() -> new ValidationFailedException(AppCode.TOKEN_INVALID));
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ValidationFailedException(AppCode.TOKEN_EXPIRED);
        }
        Optional<User> existUser = userRepository.findById(resetToken.getUserId().getUserId());
        if (existUser.isPresent()) {
            User user = existUser.get();
            user.setStatus(Status.ACTIVE.getValue());
            userRepository.save(user);
        }
        userTokenRepository.delete(resetToken);
        return true;
    }

    @Override
    public void resetPasswordWithToken(String token, String newPassword) {
        UserToken resetToken = userTokenRepository.findByToken(token)
                .orElseThrow(() -> new ValidationFailedException(AppCode.TOKEN_INVALID));
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ValidationFailedException(AppCode.TOKEN_EXPIRED);
        }
        User user = resetToken.getUserId();
        user.setPassword(newPassword);
        userRepository.save(user);
        userTokenRepository.delete(resetToken);

    }

    public void validateCustomerUniqueUsername(String username) {
        if (userRepository.findUserByUsername(username) != null) {
            throw new ValidationFailedException(AppCode.USERNAME_EXISTED);
        }
    }

    public void validateCustomerUniqueEmail(String email) {
        if (!userRepository.findByEmail(email.toLowerCase()).isEmpty()) {
            throw new ValidationFailedException(AppCode.EMAIL_EXISTED);
        }
    }

    public void validateCustomerUniquePhone(String phone) {
        if (userRepository.findByPhone(phone) != null) {
            throw new ValidationFailedException(AppCode.PHONE_EXISTED);
        }
    }

    @Override
    public PaginationWrapper<List<User>> searchUser(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.searchUser(keyword, pageable, Status.UNACTIVE.getValue(),
                Role.CUSTOMER.getRoleName());
        if (userPage.isEmpty()) {
            throw new ValidationFailedException(AppCode.USER_NOT_FOUND);
        }
        ;
        return new PaginationWrapper.Builder<List<User>>()
                .setData(userPage.getContent())
                .setPaginationInfo(userPage)
                .build();
    }

    @Override
    public PaginationWrapper<List<User>> getAllUsers(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> userPage = userRepository.findAll(pageable, Status.UNACTIVE.getValue(),
                    Role.CUSTOMER.getRoleName());
            return new PaginationWrapper.Builder<List<User>>()
                    .setData(userPage.getContent())
                    .setPaginationInfo(userPage)
                    .build();
        } catch (Exception e) {
            throw new ActionFailedException(AppCode.USER_GET_LIST_FAILED, e);
        }
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public User updateUser(User userDetails) {
        User user = findUserByUserId(userDetails.getUserId());
        if (!userDetails.getEmail().equalsIgnoreCase(user.getEmail())
                && findUserByEmail(userDetails.getEmail()) != null) {
            validateCustomerUniqueEmail(user.getEmail());
        }
        if (!userDetails.getPhone().equals(user.getPhone()) && findUsersByPhone(userDetails.getPhone()) != null) {
            validateCustomerUniquePhone(user.getPhone());
        }
        if (!userDetails.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (user.getStatus() == Status.LOCK.getValue()) {
                user.setStatus(Status.LOCK_VERIFY.getValue());
            } else {
                user.setStatus(Status.VERIFYING.getValue());
            }
        } else {
            user.setStatus(user.getStatus());
        }
        user.setEmail(userDetails.getEmail());
        user.setAddress(userDetails.getAddress());
        user.setCompanyName(userDetails.getCompanyName());
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setMbsId(userDetails.getMbsId());
        user.setPhone(userDetails.getPhone());
        user.setSex(userDetails.getSex());
        user.setTaxCode(userDetails.getTaxCode());
        if (userDetails.getRoleId() == null) {
            userDetails.setRoleId(roleRepository.findByRoleName(Role.CUSTOMER.getRoleName()));
        }
        user.setRoleId(userDetails.getRoleId());
        user.setPassword(user.getPassword());
        user.setRoleId(userDetails.getRoleId());
        user.setDob(userDetails.getDob());
        user.setMbsId(user.getMbsId());
        user.setUserId(user.getUserId());
        user.setUserPoint(user.getUserPoint());
        user.setUsername(userDetails.getUsername());
        try {
            return userRepository.save(user);
        } catch (Exception e) {
            throw new ActionFailedException(AppCode.USER_UPDATED_FAILED, e);
        }
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public User createUser(User user) {
        validateCustomerUniqueContract(user.getUsername(), user.getEmail(), user.getPhone());
        user.setUserId(null);
        user.setMbsId(null);
        user.setUserPoint(0);
        return userRepository.save(user);
    }

    @Override
    public User findUserByUserId(Long userId) {
        User user = userRepository.findById(userId).get();
        if (user != null) {
            if (user.getStatus() == Status.UNACTIVE.getValue()
                    && user.getRoleId().getRoleName().equalsIgnoreCase(Role.CUSTOMER.getRoleName())) {
                return null;
            }
        }
        return user;
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public void deleteUser(Long userId) {
        User user = findUserByUserId(userId);
        if (user == null)
            throw new ValidationFailedException(AppCode.USER_NOT_FOUND);
        user.setUsername("customer-deleted-" + user.getUserId() + "-id-" + user.getUsername());
        user.setEmail(UUID.randomUUID().toString() + "-" + user.getEmail());
        user.setPhone(UUID.randomUUID().toString() + "-" + user.getPhone());
        user.setStatus(Status.UNACTIVE.getValue());
        userRepository.save(user);
    }

    @Override
    public User findUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email.toLowerCase());
        if (user.isPresent()) {
            return user.get();
        }
        return null;
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public void LockAndUnlockUser(Long userId) {
        User user = findUserByUserId(userId);
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
        userRepository.save(user);
    }

    @Override
    public User findUsersByPhone(String phone) {
        User user = userRepository.findByPhone(phone);
        if (user == null || (user.getStatus() == Status.UNACTIVE.getValue()
                && user.getRoleId().getRoleName().equalsIgnoreCase(Role.CUSTOMER.getRoleName()))) {
            return null;
        }
        return user;
    }

    @Override
    public User findUserByUsername(String username) {
        User user = userRepository.findUserByUsername(username);
        if (user != null) {
            if (user.getStatus() == Status.UNACTIVE.getValue()
                    && user.getRoleId().getRoleName().equalsIgnoreCase(Role.CUSTOMER.getRoleName())) {
                return null;
            }
        }
        return user;
    }

}
