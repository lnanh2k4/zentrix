package com.zentrix.service;

import com.zentrix.model.entity.Branch;
import com.zentrix.model.entity.ProductTypeBranch;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.BranchRepository;
import com.zentrix.repository.ProductTypeBranchRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import java.util.List;

/*
 * @author Nguyen Thanh Binh - CE171099 - CT25_CPL_JAVA_01
 * @date April 06, 2025
 */

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class BranchServiceImpl implements BranchService {

    BranchRepository branchRepository;
    ProductTypeBranchRepository productTypeBranchRepository;


    

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public List<ProductTypeBranch> getProductTypesByBranchId(Long branchId) {
        return productTypeBranchRepository.findByBrchId_BrchId(branchId);
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public List<Branch> getAllBranchesNonPaged() {
        return branchRepository.findAll();
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public Branch findByBrchName(String brchName) {
        return branchRepository.findByBrchName(brchName).orElse(null);
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public PaginationWrapper<List<Branch>> getAllBranches(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Branch> branchPage = branchRepository.findAll(pageable);
        return new PaginationWrapper<>(
                branchPage.getContent(),
                branchPage.getNumber(),
                branchPage.getSize(),
                branchPage.getTotalPages(),
                (int) branchPage.getTotalElements()
        );
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public Branch getBranchById(Long id) {
        return branchRepository.findById(id).orElse(null);
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public Branch createBranch(Branch branch) {
        return branchRepository.save(branch);
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public Branch updateBranch(Long id, Branch updatedBranch) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ActionFailedException(AppCode.BRANCH_NOT_FOUND));

        branch.setBrchName(updatedBranch.getBrchName());
        branch.setAddress(updatedBranch.getAddress());
        branch.setPhone(updatedBranch.getPhone());
        branch.setStatus(updatedBranch.getStatus());

        return branchRepository.save(branch);
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public List<Branch> findBranchesByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ActionFailedException(AppCode.BRANCH_ALREADY_EXISTS);
        }
        return branchRepository.findByBrchNameContainingIgnoreCase(name);
    }

    @Transactional(rollbackFor = { ActionFailedException.class }, isolation = Isolation.REPEATABLE_READ)
    @Override
    public boolean deleteBranch(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found"));

        if (productTypeBranchRepository.existsByBrchId_BrchId(id)) {
            throw new IllegalStateException("Cannot delete branch because it contains product types");
        }

        branchRepository.deleteById(id);
        return true;
    }
}
