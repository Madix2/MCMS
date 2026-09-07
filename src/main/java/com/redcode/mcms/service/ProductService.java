package com.redcode.mcms.service;

import com.redcode.mcms.dto.ProductDto;
import com.redcode.mcms.entity.*;
import com.redcode.mcms.exception.BusinessException;
import com.redcode.mcms.exception.NotFoundException;
import com.redcode.mcms.repository.*;
import com.redcode.mcms.security.AuthContext;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Inventory/business service for products.
 *
 * Handles product CRUD, stock adjustments and records every stock movement.
 * When a product's quantity drops to or below its minimum level, a low-stock
 * notification is generated automatically so Procurement is alerted.
 */
@Stateless
public class ProductService {

    @Inject
    private ProductRepository productRepository;

    @Inject
    private CategoryRepository categoryRepository;

    @Inject
    private SupplierRepository supplierRepository;

    @Inject
    private StockMovementRepository stockMovementRepository;

    @Inject
    private NotificationService notificationService;

    @Inject
    private AuditService auditService;

    @Inject
    private AuthContext authContext;

    public List<ProductDto> list(String search, Long categoryId, Long supplierId) {
        List<Product> products;
        if (supplierId != null) {
            products = productRepository.findBySupplier(supplierId);
        } else if (categoryId != null) {
            products = productRepository.findByCategory(categoryId);
        } else {
            products = productRepository.search(search);
        }
        return products.stream().map(this::toDto).collect(Collectors.toList());
    }

    public ProductDto find(Long id) {
        return toDto(getProduct(id));
    }

    public ProductDto create(ProductDto dto) {
        authContext.requireRole(AuthContext.RolePermission.INVENTORY, AuthContext.RolePermission.ADMIN);
        validate(dto);

        Product p = new Product();
        apply(dto, p);
        p.setQuantity(dto.getQuantity());
        p.setStatus(Status.ACTIVE);

        Product saved = productRepository.save(p);

        // Record the initial stock as a movement
        if (saved.getQuantity() > 0) {
            recordMovement(saved, MovementType.INITIAL, saved.getQuantity(),
                    "INIT", "Initial stock on product creation");
        }

        auditService.log("CREATE", "Product", "Product \"" + saved.getName() + "\" created");
        checkLowStock(saved);
        return toDto(saved);
    }

    public ProductDto update(Long id, ProductDto dto) {
        authContext.requireRole(AuthContext.RolePermission.INVENTORY, AuthContext.RolePermission.ADMIN);
        validate(dto);
        Product p = getProduct(id);
        apply(dto, p);
        Product updated = productRepository.update(p);
        auditService.log("UPDATE", "Product", "Product \"" + updated.getName() + "\" updated");
        return toDto(updated);
    }

    public void delete(Long id) {
        authContext.requireRole(AuthContext.RolePermission.INVENTORY, AuthContext.RolePermission.ADMIN);
        Product p = getProduct(id);
        p.setStatus(Status.INACTIVE);
        productRepository.update(p);
        auditService.log("DEACTIVATE", "Product", "Product \"" + p.getName() + "\" deactivated");
    }

    public List<ProductDto> lowStock() {
        return productRepository.findLowStock().stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<StockMovement> movements(Long productId) {
        return stockMovementRepository.findByProduct(productId);
    }

    /** Manual stock adjustment used by the inventory module. */
    public ProductDto adjustStock(Long productId, int delta, String reason) {
        authContext.requireRole(AuthContext.RolePermission.INVENTORY, AuthContext.RolePermission.ADMIN);
        Product p = getProduct(productId);
        int newQty = p.getQuantity() + delta;
        if (newQty < 0) {
            throw new BusinessException("Adjustment would make stock negative for " + p.getName() + ".");
        }
        p.setQuantity(newQty);
        productRepository.update(p);
        recordMovement(p, MovementType.ADJUSTMENT, delta, "ADJ", reason);
        auditService.log("ADJUST_STOCK", "Product", "Stock of \"" + p.getName() + "\" adjusted by " + delta);
        checkLowStock(p);
        return toDto(p);
    }

    /**
     * Used by sales: deducts the sold quantity from a product's stock and
     * returns true if the product has become low stock. Boundary of the sale
     * transaction.
     */
    public boolean deductStock(Product product, int quantity, String reference) {
        if (product.getQuantity() < quantity) {
            throw new BusinessException("Insufficient stock for " + product.getName() + ".");
        }
        product.setQuantity(product.getQuantity() - quantity);
        productRepository.update(product);
        recordMovement(product, MovementType.SALE, -quantity, reference, "Sale deduction");
        return product.isLowStock();
    }

    /** Used by purchase-order receiving: adds received quantity to stock. */
    public boolean addStock(Product product, int quantity, String reference) {
        product.setQuantity(product.getQuantity() + quantity);
        productRepository.update(product);
        recordMovement(product, MovementType.PURCHASE, quantity, reference, "Purchase order received");
        return product.isLowStock();
    }

    /**
     * Re-inspects a product and, if it is at or below its minimum stock level,
     * creates a low-stock notification for the Procurement role.
     */
    public void checkLowStock(Product product) {
        if (product.isLowStock()) {
            notificationService.notify("LOW_STOCK", "LOW STOCK",
                    product.getName() + " has reached its minimum stock level. Current: "
                            + product.getQuantity() + ", Minimum: " + product.getMinStockLevel() + ".",
                    "PROCUREMENT");
        }
    }

    private void recordMovement(Product product, MovementType type, int delta, String reference, String notes) {
        StockMovement m = new StockMovement();
        m.setProduct(product);
        m.setType(type);
        m.setQuantityChanged(delta);
        m.setBalanceAfter(product.getQuantity());
        m.setReference(reference);
        m.setNotes(notes);
        stockMovementRepository.save(m);
    }

    private void validate(ProductDto dto) {
        if (dto.getSellingPrice().signum() < 0) {
            throw new BusinessException("Selling price cannot be negative.");
        }
        if (dto.getCostPrice().signum() < 0) {
            throw new BusinessException("Cost price cannot be negative.");
        }
    }

    private void apply(ProductDto dto, Product p) {
        p.setName(dto.getName());
        p.setBarcode(dto.getBarcode());
        p.setSellingPrice(dto.getSellingPrice());
        p.setCostPrice(dto.getCostPrice());
        p.setDescription(dto.getDescription());
        p.setMinStockLevel(dto.getMinStockLevel());
        p.setMaxStockLevel(dto.getMaxStockLevel());

        if (dto.getCategoryId() != null) {
            p.setCategory(categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found.")));
        }
        if (dto.getSupplierId() != null) {
            p.setSupplier(supplierRepository.findById(dto.getSupplierId())
                    .orElseThrow(() -> new NotFoundException("Supplier not found.")));
        }
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found."));
    }

    public ProductDto toDto(Product p) {
        ProductDto dto = new ProductDto();
        dto.setId(p.getId());
        dto.setBarcode(p.getBarcode());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setSellingPrice(p.getSellingPrice());
        dto.setCostPrice(p.getCostPrice());
        dto.setQuantity(p.getQuantity());
        dto.setMinStockLevel(p.getMinStockLevel());
        dto.setMaxStockLevel(p.getMaxStockLevel());
        dto.setStatus(p.getStatus().name());
        dto.setLowStock(p.isLowStock());
        if (p.getCategory() != null) {
            dto.setCategoryId(p.getCategory().getId());
            dto.setCategoryName(p.getCategory().getName());
        }
        if (p.getSupplier() != null) {
            dto.setSupplierId(p.getSupplier().getId());
            dto.setSupplierName(p.getSupplier().getName());
        }
        return dto;
    }
}
