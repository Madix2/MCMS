package com.redcode.mcms.service;

import com.redcode.mcms.dto.PurchaseOrderDto;
import com.redcode.mcms.dto.PurchaseOrderRequest;
import com.redcode.mcms.entity.*;
import com.redcode.mcms.exception.BusinessException;
import com.redcode.mcms.exception.NotFoundException;
import com.redcode.mcms.repository.ProductRepository;
import com.redcode.mcms.repository.PurchaseOrderRepository;
import com.redcode.mcms.repository.SupplierRepository;
import com.redcode.mcms.security.AuthContext;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Procurement business service delivering the purchase-order workflow:
 *
 *   DRAFT -> PENDING_APPROVAL -> APPROVED -> RECEIVED
 *                    |               |
 *                 REJECTED        ORDERED/PARTIALLY_RECEIVED
 *
 *  - Procurement creates and submits an order.
 *  - Only a Manager/Admin can approve or reject it ({@link #approve}/{@link #reject}).
 *  - When the order is received, the inventory and stock-movement history are
 *    updated inside a single transaction ({@link #receiveOrder}).
 */
@Stateless
public class PurchaseOrderService {

    @Inject
    private PurchaseOrderRepository purchaseOrderRepository;

    @Inject
    private ProductRepository productRepository;

    @Inject
    private SupplierRepository supplierRepository;

    @Inject
    private ProductService productService;

    @Inject
    private NotificationService notificationService;

    @Inject
    private AuditService auditService;

    @Inject
    private AuthContext authContext;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public PurchaseOrderDto create(PurchaseOrderRequest request) {
        authContext.requireRole(AuthContext.RolePermission.PROCUREMENT, AuthContext.RolePermission.ADMIN);

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("A purchase order must contain at least one product.");
        }

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new NotFoundException("Supplier not found."));

        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber(generatePoNumber());
        po.setSupplier(supplier);
        po.setStatus(PurchaseOrderStatus.DRAFT);
        po.setNotes(request.getNotes());

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderRequest.PoItem poItem : request.getItems()) {
            Product product = productRepository.findById(poItem.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found."));

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(po);
            item.setProduct(product);
            item.setQuantity(poItem.getQuantity());
            item.setUnitCost(product.getCostPrice());
            item.setLineTotal(product.getCostPrice().multiply(BigDecimal.valueOf(poItem.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP));
            total = total.add(item.getLineTotal());
            po.getItems().add(item);
        }
        po.setTotal(total.setScale(2, RoundingMode.HALF_UP));

        PurchaseOrder saved = purchaseOrderRepository.save(po);
        auditService.log("CREATE_PO", "PurchaseOrder", saved.getId(),
                "Purchase Order " + saved.getPoNumber() + " created");
        return toDto(saved);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public PurchaseOrderDto submitForApproval(Long id) {
        authContext.requireRole(AuthContext.RolePermission.PROCUREMENT, AuthContext.RolePermission.ADMIN);
        PurchaseOrder po = getPurchaseOrder(id);
        if (po.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new BusinessException("Only draft purchase orders can be submitted for approval.");
        }
        po.setStatus(PurchaseOrderStatus.PENDING_APPROVAL);
        PurchaseOrder updated = purchaseOrderRepository.update(po);

        notificationService.notify("PO_APPROVAL", "PURCHASE ORDER",
                "Purchase Order " + updated.getPoNumber() + " requires approval.", "MANAGER");
        auditService.log("SUBMIT_PO", "PurchaseOrder", updated.getId(),
                "Purchase Order " + updated.getPoNumber() + " submitted for approval");
        return toDto(updated);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public PurchaseOrderDto approve(Long id) {
        authContext.requireRole(AuthContext.RolePermission.MANAGER, AuthContext.RolePermission.ADMIN);
        PurchaseOrder po = getPurchaseOrder(id);
        if (po.getStatus() != PurchaseOrderStatus.PENDING_APPROVAL) {
            throw new BusinessException("Only purchase orders pending approval can be approved.");
        }
        po.setStatus(PurchaseOrderStatus.APPROVED);
        po.setApprovedBy(username());
        po.setApprovedAt(LocalDateTime.now());
        PurchaseOrder updated = purchaseOrderRepository.update(po);

        notificationService.notify("PO_APPROVED", "PURCHASE ORDER",
                "Purchase Order " + updated.getPoNumber() + " has been approved.", "PROCUREMENT");
        auditService.log("APPROVE_PO", "PurchaseOrder", updated.getId(),
                "Manager approved Purchase Order " + updated.getPoNumber());
        return toDto(updated);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public PurchaseOrderDto reject(Long id, String reason) {
        authContext.requireRole(AuthContext.RolePermission.MANAGER, AuthContext.RolePermission.ADMIN);
        PurchaseOrder po = getPurchaseOrder(id);
        if (po.getStatus() != PurchaseOrderStatus.PENDING_APPROVAL) {
            throw new BusinessException("Only purchase orders pending approval can be rejected.");
        }
        po.setStatus(PurchaseOrderStatus.REJECTED);
        if (reason != null && !reason.isBlank()) {
            po.setNotes((po.getNotes() == null ? "" : po.getNotes()) + " [Rejected: " + reason + "]");
        }
        PurchaseOrder updated = purchaseOrderRepository.update(po);

        notificationService.notify("PO_REJECTED", "PURCHASE ORDER",
                "Purchase Order " + updated.getPoNumber() + " has been rejected.", "PROCUREMENT");
        auditService.log("REJECT_PO", "PurchaseOrder", updated.getId(),
                "Manager rejected Purchase Order " + updated.getPoNumber());
        return toDto(updated);
    }

    /**
     * Marks the order as ORDERED (simulating dispatch) before receipt.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public PurchaseOrderDto markOrdered(Long id) {
        authContext.requireRole(AuthContext.RolePermission.PROCUREMENT, AuthContext.RolePermission.ADMIN);
        PurchaseOrder po = getPurchaseOrder(id);
        if (po.getStatus() != PurchaseOrderStatus.APPROVED) {
            throw new BusinessException("Only approved purchase orders can be ordered.");
        }
        po.setStatus(PurchaseOrderStatus.ORDERED);
        PurchaseOrder updated = purchaseOrderRepository.update(po);
        auditService.log("ORDER_PO", "PurchaseOrder", updated.getId(),
                "Purchase Order " + updated.getPoNumber() + " marked as ordered");
        return toDto(updated);
    }

    /**
     * Receives a fully ordered purchase order, updating inventory and stock
     * movements in one transaction. After this the inventory reflects the new
     * stock; if a product is no longer low stock the notification stays (the
     * demo reads history), and low-stock checks are refreshed.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public PurchaseOrderDto receive(Long id) {
        authContext.requireRole(AuthContext.RolePermission.PROCUREMENT,
                AuthContext.RolePermission.INVENTORY, AuthContext.RolePermission.ADMIN);
        PurchaseOrder po = getPurchaseOrder(id);
        if (po.getStatus() == PurchaseOrderStatus.RECEIVED || po.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new BusinessException("This purchase order cannot be received.");
        }
        if (po.getStatus() != PurchaseOrderStatus.ORDERED && po.getStatus() != PurchaseOrderStatus.APPROVED) {
            throw new BusinessException("Only ordered purchase orders can be received.");
        }

        for (PurchaseOrderItem item : po.getItems()) {
            int remaining = item.getQuantity() - item.getReceivedQty();
            if (remaining > 0) {
                Product product = item.getProduct();
                // update stock + record stock movement inside this same transaction
                productService.addStock(product, remaining, "PO " + po.getPoNumber());
                item.setReceivedQty(item.getReceivedQty() + remaining);
            }
        }

        boolean allReceived = po.getItems().stream()
                .allMatch(i -> i.getReceivedQty() >= i.getQuantity());
        po.setStatus(allReceived ? PurchaseOrderStatus.RECEIVED : PurchaseOrderStatus.PARTIALLY_RECEIVED);
        po.setReceivedBy(username());
        po.setReceivedAt(LocalDateTime.now());

        PurchaseOrder updated = purchaseOrderRepository.update(po);

        notificationService.notify("PO_RECEIVED", "PURCHASE ORDER",
                "Purchase Order " + updated.getPoNumber() + " received. Inventory updated.", "INVENTORY");
        auditService.log("RECEIVE_PO", "PurchaseOrder", updated.getId(),
                "Purchase Order " + updated.getPoNumber() + " received and inventory updated");
        return toDto(updated);
    }

    public PurchaseOrderDto find(Long id) {
        return toDto(getPurchaseOrder(id));
    }

    public List<PurchaseOrderDto> list(String status) {
        List<PurchaseOrder> list;
        if (status != null && !status.isBlank()) {
            list = new ArrayList<>(purchaseOrderRepository
                    .findByStatus(PurchaseOrderStatus.valueOf(status)));
        } else {
            list = purchaseOrderRepository.findAllOrderedDesc();
        }
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<PurchaseOrderDto> listPendingApproval() {
        return purchaseOrderRepository.findPendingApproval().stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    private PurchaseOrder getPurchaseOrder(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Purchase order not found."));
    }

    private String generatePoNumber() {
        return "PO-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String username() {
        return authContext.isAuthenticated() ? authContext.getUser().getUsername() : "SYSTEM";
    }

    public PurchaseOrderDto toDto(PurchaseOrder po) {
        PurchaseOrderDto dto = new PurchaseOrderDto();
        dto.setId(po.getId());
        dto.setPoNumber(po.getPoNumber());
        dto.setOrderDate(po.getOrderDate());
        dto.setStatus(po.getStatus().name());
        dto.setTotal(po.getTotal());
        dto.setApprovedBy(po.getApprovedBy());
        dto.setApprovedAt(po.getApprovedAt());
        dto.setReceivedBy(po.getReceivedBy());
        dto.setReceivedAt(po.getReceivedAt());
        dto.setNotes(po.getNotes());
        if (po.getSupplier() != null) {
            dto.setSupplierId(po.getSupplier().getId());
            dto.setSupplierName(po.getSupplier().getName());
        }
        for (PurchaseOrderItem item : po.getItems()) {
            PurchaseOrderDto.PoItemDto idto = new PurchaseOrderDto.PoItemDto();
            idto.setProductId(item.getProduct().getId());
            idto.setProductName(item.getProduct().getName());
            idto.setQuantity(item.getQuantity());
            idto.setReceivedQty(item.getReceivedQty());
            idto.setUnitCost(item.getUnitCost());
            idto.setLineTotal(item.getLineTotal());
            dto.getItems().add(idto);
        }
        return dto;
    }
}
