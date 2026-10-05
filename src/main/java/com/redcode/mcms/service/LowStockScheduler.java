package com.redcode.mcms.service;

import com.redcode.mcms.entity.Product;
import com.redcode.mcms.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

/** Periodically reconciles low-stock alerts after imports or external updates. */
@Singleton
@Startup
@Lock(LockType.READ)
public class LowStockScheduler {

    @Inject
    private ProductRepository productRepository;

    @Inject
    private ProductService productService;

    @PostConstruct
    public void initialScan() {
        scan();
    }

    @Schedule(hour = "*", minute = "0", second = "0", persistent = false)
    public void hourlyScan() {
        scan();
    }

    private void scan() {
        for (Product product : productRepository.findLowStock()) {
            productService.checkLowStock(product);
        }
    }
}
