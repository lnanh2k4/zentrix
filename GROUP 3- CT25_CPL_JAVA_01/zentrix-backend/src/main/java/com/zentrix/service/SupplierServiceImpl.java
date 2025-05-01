package com.zentrix.service;
import com.zentrix.repository.ProductRepository; // Thêm import này
import com.zentrix.repository.StockRepository;
import com.zentrix.model.entity.Supplier;
import com.zentrix.model.request.SupplierRequest;
import com.zentrix.repository.SupplierRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


/*
 * @author Nguyen Thanh Binh - CE171099 - CT25_CPL_JAVA_01
 * @date April 06, 2025
 */

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class SupplierServiceImpl implements SupplierService {

    SupplierRepository supplierRepository;
    ProductRepository productRepository; // add ProductRepository
    StockRepository stockRepository; // add StockRepository


    @Override
    public Page<Supplier> getAllSuppliers(Pageable pageable) {
        return supplierRepository.findAll(pageable);
    }

    @Override
    public Supplier getSupplierById(int id) {
        return supplierRepository.findById(id).orElse(null);
    }

    @Transactional
    public Supplier addSupplier(Supplier supplier) {
        if (supplierRepository.existsByEmail(supplier.getEmail())) {
            return null;
        }
        return supplierRepository.save(supplier);
    }

    @Override
    public List<Supplier> findSuppliersByName(String name) {
        return supplierRepository.findBySuppNameContainingIgnoreCase(name);
    }

    @Override
    @Transactional
    public Supplier updateSupplier(int id, SupplierRequest supplierRequest) {
        Optional<Supplier> optionalSupplier = supplierRepository.findById(id);
        if (optionalSupplier.isEmpty()) {
            return null;
        }

        Supplier existingSupplier = optionalSupplier.get();
        existingSupplier.setSuppName(supplierRequest.getSuppName());
        existingSupplier.setEmail(supplierRequest.getEmail());
        existingSupplier.setPhone(supplierRequest.getPhone());
        existingSupplier.setAddress(supplierRequest.getAddress());

        return supplierRepository.save(existingSupplier);
    }

    @Transactional
    @Override
    public boolean deleteSupplier(int id) {
        Optional<Supplier> optionalSupplier = supplierRepository.findById(id);
        if (optionalSupplier.isEmpty()) {
            return false;
        }

        Supplier supplier = optionalSupplier.get();

        // Product
        long productCount = productRepository.countBySuppId(supplier);
        if (productCount > 0) {
            throw new RuntimeException("Cannot delete supplier because it is associated with " + productCount + " products");
        }

        // Stock 
        long stockCount = stockRepository.countBySupplierId(supplier);
        if (stockCount > 0) {
            throw new RuntimeException("Cannot delete supplier because it is associated with " + stockCount + " stocks");
        }

        supplierRepository.delete(supplier);
        return !supplierRepository.existsById(id);
    }
}
