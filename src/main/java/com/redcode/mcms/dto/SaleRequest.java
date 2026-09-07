package com.redcode.mcms.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Request body for completing a sale.
 * Contains the cart lines plus payment information.
 */
public class SaleRequest {

    private Long customerId;

    @NotEmpty(message = "A sale must contain at least one product.")
    private List<CartItem> items = new ArrayList<>();

    @NotNull(message = "Payment method is required.")
    private String paymentMethod;

    @NotNull(message = "Amount tendered is required.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Amount tendered cannot be negative.")
    private BigDecimal amountTendered;

    /** Whether the (registered) customer wants to redeem loyalty points on this sale. */
    private boolean usePoints;

    public static class CartItem {
        @NotNull(message = "Product is required.")
        private Long productId;

        @Min(value = 1, message = "Quantity must be at least 1.")
        private int quantity;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public BigDecimal getAmountTendered() { return amountTendered; }
    public void setAmountTendered(BigDecimal amountTendered) { this.amountTendered = amountTendered; }
    public boolean isUsePoints() { return usePoints; }
    public void setUsePoints(boolean usePoints) { this.usePoints = usePoints; }
}
