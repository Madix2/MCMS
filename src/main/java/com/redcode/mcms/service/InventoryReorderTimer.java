package com.redcode.mcms.service;

import com.redcode.mcms.entity.Product;
import com.redcode.mcms.entity.PurchaseOrder;
import com.redcode.mcms.entity.PurchaseOrderItem;
import com.redcode.mcms.entity.PurchaseOrderStatus;
import com.redcode.mcms.entity.Supplier;
import com.redcode.mcms.entity.SupplierProductMap;
import com.redcode.mcms.repository.ProductRepository;
import com.redcode.mcms.repository.PurchaseOrderRepository;
import com.redcode.mcms.repository.SupplierProductMapRepository;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Singleton
@Startup
public class InventoryReorderTimer {
    @Inject private ProductRepository productRepository;
    @Inject private SupplierProductMapRepository mapRepository;
    @Inject private PurchaseOrderRepository purchaseOrderRepository;

    @Schedule(hour = "2", minute = "0", second = "0", persistent = false)
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void nightlyReorder() {
        Map<Supplier, PurchaseOrder> orders = new LinkedHashMap<>();
        for (Product product : productRepository.findLowStock()) {
            int quantity = product.getMaxStockLevel() - product.getQuantity();
            if (quantity <= 0) continue;
            SupplierProductMap source = mapRepository.findPrimaryForProduct(product.getId()).orElse(null);
            Supplier supplier = source == null ? product.getSupplier() : source.getSupplier();
            if (supplier == null) continue;
            BigDecimal unitCost = source == null ? product.getCostPrice() : source.getUnitCost();
            PurchaseOrder po = orders.get(supplier);
            if (po == null) {
                po = new PurchaseOrder();
                po.setPoNumber("PO-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                        + "-" + supplier.getId());
                po.setSupplier(supplier);
                po.setStatus(PurchaseOrderStatus.DRAFT);
                orders.put(supplier, po);
            }
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(po);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitCost(unitCost);
            item.setLineTotal(unitCost.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP));
            po.getItems().add(item);
            po.setTotal(po.getTotal().add(item.getLineTotal()).setScale(2, RoundingMode.HALF_UP));
        }
        for (PurchaseOrder po : orders.values()) purchaseOrderRepository.save(po);
    }
}
