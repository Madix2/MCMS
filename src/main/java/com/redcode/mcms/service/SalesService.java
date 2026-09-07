package com.redcode.mcms.service;

import com.redcode.mcms.dto.SaleDto;
import com.redcode.mcms.dto.SaleRequest;
import com.redcode.mcms.entity.*;
import com.redcode.mcms.exception.BusinessException;
import com.redcode.mcms.exception.NotFoundException;
import com.redcode.mcms.repository.CustomerRepository;
import com.redcode.mcms.repository.ProductRepository;
import com.redcode.mcms.repository.SaleRepository;
import com.redcode.mcms.security.AuthContext;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sales business service.
 *
 * The {@link #createSale} method is wrapped in a single container-managed
 * transaction (default REQUIRED for {@code @Stateless}). Every database change
 * required to complete a sale happens inside one transaction boundary:
 *
 *   1. Create and save the Sale.
 *   2. Create and save each SaleItem.
 *   3. Deduct the purchased quantities from inventory (records stock movements).
 *   4. Calculate the totals.
 *   5. Record the payment.
 *   6. Award loyalty points to the customer.
 *
 * If any critical step fails (e.g. insufficient stock), a RuntimeException is
 * thrown and the entire transaction rolls back, so no partial sale is saved.
 */
@Stateless
public class SalesService {

    @Inject
    private SaleRepository saleRepository;

    @Inject
    private ProductRepository productRepository;

    @Inject
    private CustomerRepository customerRepository;

    @Inject
    private ProductService productService;

    @Inject
    private NotificationService notificationService;

    @Inject
    private AuditService auditService;

    @Inject
    private AuthContext authContext;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public SaleDto createSale(SaleRequest request) {
        authContext.requireRole(AuthContext.RolePermission.SALES, AuthContext.RolePermission.ADMIN);

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("A sale must contain at least one product.");
        }

        PaymentMethod method = parsePaymentMethod(request.getPaymentMethod());
        if (request.getAmountTendered() == null || request.getAmountTendered().signum() < 0) {
            throw new BusinessException("A valid amount tendered is required.");
        }

        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new NotFoundException("Customer not found."));
        }

        // Build the sale
        Sale sale = new Sale();
        sale.setSaleNumber(generateSaleNumber());
        sale.setCustomer(customer);
        sale.setSaleDate(LocalDateTime.now());

        BigDecimal subtotal = BigDecimal.ZERO;
        boolean anyLowStock = false;

        // Step 2 & 3: create items + deduct inventory inside the same transaction
        for (SaleRequest.CartItem cartItem : request.getItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found."));

            SaleItem item = new SaleItem();
            item.setSale(sale);
            item.setProduct(product);
            item.setQuantity(cartItem.getQuantity());
            item.setUnitPrice(product.getSellingPrice());

            BigDecimal lineTotal = product.getSellingPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            item.setLineTotal(lineTotal);
            subtotal = subtotal.add(lineTotal);
            sale.getItems().add(item);

            // Deduct stock; throws BusinessException if insufficient -> rollback
            boolean becameLow = productService.deductStock(product, cartItem.getQuantity(),
                    "SALE " + sale.getSaleNumber());
            if (becameLow) {
                anyLowStock = true;
            }
        }

        // Step 4: totals (15% VAT for the demo)
        BigDecimal discount = BigDecimal.ZERO;
        boolean redeemedPoints = false;
        if (customer != null && request.isUsePoints() && customer.getLoyaltyPoints() > 0) {
            // Redeem loyalty points at R0.10 per point, capped at the payable subtotal.
            BigDecimal maxDiscount = subtotal;
            BigDecimal desired = new BigDecimal(customer.getLoyaltyPoints())
                    .multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.DOWN);
            discount = desired.min(maxDiscount);
            if (discount.signum() > 0) {
                redeemedPoints = true;
            }
        }
        BigDecimal taxableBase = subtotal.subtract(discount);
        BigDecimal tax = taxableBase.multiply(new BigDecimal("0.15")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = taxableBase.add(tax).setScale(2, RoundingMode.HALF_UP);

        sale.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        sale.setDiscount(discount);
        sale.setTax(tax);
        sale.setTotal(total);
        sale.setAmountTendered(request.getAmountTendered());
        sale.setChangeGiven(request.getAmountTendered().subtract(total).max(BigDecimal.ZERO));
        sale.setStatus(Status.COMPLETED);

        // Step 1: persist the sale (cascades to items)
        Sale saved = saleRepository.save(sale);

        // Step 5: record payment
        Payment payment = new Payment();
        payment.setSale(saved);
        payment.setMethod(method);
        payment.setAmount(total);
        payment.setReference("PAY-" + saved.getSaleNumber());
        saved.setPayment(payment);

        // Step 6: loyalty points - R10 spent = 1 point awarded on the amount paid
        if (customer != null) {
            if (redeemedPoints) {
                int pointsUsed = discount.multiply(new BigDecimal("10")).divideToIntegralValue(BigDecimal.ONE)
                        .intValue();
                customer.setLoyaltyPoints((customer.getLoyaltyPoints() - pointsUsed));
            }
            int points = total.divideToIntegralValue(new BigDecimal("10")).intValue();
            customer.setLoyaltyPoints(customer.getLoyaltyPoints() + points);
            customerRepository.update(customer);
        }

        saleRepository.update(saved);

        // Record notifications/audit
        notificationService.notify("SALE", "SALE COMPLETED",
                "Sale #" + saved.getSaleNumber() + " completed. Total: R" + total, null);
        if (anyLowStock) {
            // low stock notifications were created inside deductStock
        }
        auditService.log("CREATE_SALE", "Sale", saved.getId(),
                "Sale " + saved.getSaleNumber() + " completed");

        return toDto(saved);
    }

    public SaleDto find(Long id) {
        return toDto(saleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sale not found.")));
    }

    public List<SaleDto> list(String search) {
        return saleRepository.search(search).stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<SaleDto> listByCustomer(Long customerId) {
        return saleRepository.findByCustomer(customerId).stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Generates a unique sale number, e.g. "S-20260831-0001".
     * Uses a timestamp so it is unique per second; combined with the DB unique
     * constraint this is sufficient for the demo.
     */
    private String generateSaleNumber() {
        return "S-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private PaymentMethod parsePaymentMethod(String value) {
        try {
            return PaymentMethod.valueOf(value == null ? "" : value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid payment method.");
        }
    }

    public SaleDto toDto(Sale s) {
        SaleDto dto = new SaleDto();
        dto.setId(s.getId());
        dto.setSaleNumber(s.getSaleNumber());
        dto.setSaleDate(s.getSaleDate());
        dto.setSubtotal(s.getSubtotal());
        dto.setTax(s.getTax());
        dto.setDiscount(s.getDiscount());
        dto.setTotal(s.getTotal());
        dto.setAmountTendered(s.getAmountTendered());
        dto.setChangeGiven(s.getChangeGiven());
        dto.setStatus(s.getStatus().name());
        if (s.getCustomer() != null) {
            dto.setCustomerId(s.getCustomer().getId());
            dto.setCustomerName(s.getCustomer().getFullName());
        }
        if (s.getPayment() != null) {
            dto.setPaymentMethod(s.getPayment().getMethod().name());
        }
        for (SaleItem item : s.getItems()) {
            SaleDto.SaleItemDto idto = new SaleDto.SaleItemDto();
            idto.setProductId(item.getProduct().getId());
            idto.setProductName(item.getProduct().getName());
            idto.setBarcode(item.getProduct().getBarcode());
            idto.setQuantity(item.getQuantity());
            idto.setUnitPrice(item.getUnitPrice());
            idto.setLineTotal(item.getLineTotal());
            dto.getItems().add(idto);
        }
        return dto;
    }
}
