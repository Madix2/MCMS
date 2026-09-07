package com.redcode.mcms.service;

import com.redcode.mcms.dto.ChartPointDto;
import com.redcode.mcms.dto.DashboardSummaryDto;
import com.redcode.mcms.dto.NotificationDto;
import com.redcode.mcms.entity.*;
import com.redcode.mcms.repository.*;
import com.redcode.mcms.security.AuthContext;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds the role-aware central dashboard. Managers and administrators see the
 * most comprehensive view; other roles see a subset relevant to their work.
 */
@Stateless
public class DashboardService {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private SaleRepository saleRepository;

    @Inject
    private ProductRepository productRepository;

    @Inject
    private PurchaseOrderRepository purchaseOrderRepository;

    @Inject
    private CustomerRepository customerRepository;

    @Inject
    private EmployeeRepository employeeRepository;

    @Inject
    private AuditLogRepository auditLogRepository;

    @Inject
    private NotificationService notificationService;

    @Inject
    private AuthContext authContext;

    public DashboardSummaryDto summary() {
        LocalDate today = LocalDate.now();
        DashboardSummaryDto dto = new DashboardSummaryDto();
        dto.setTodaysSales(saleRepository.sumToday());
        dto.setTodaysSalesCount(saleRepository.countToday());
        dto.setMonthlyRevenue(saleRepository.sumRevenueBetween(
                today.withDayOfMonth(1).atStartOfDay(), today.plusDays(1).atStartOfDay()));
        dto.setTotalProducts(productRepository.count());
        dto.setLowStockProducts(productRepository.findLowStock().size());
        dto.setPendingPurchaseOrders(purchaseOrderRepository.countByStatus(PurchaseOrderStatus.PENDING_APPROVAL));
        dto.setTotalCustomers(customerRepository.count());
        dto.setActiveEmployees(employeeRepository.countActive());
        dto.setPendingTransactions(purchaseOrderRepository.countByStatus(PurchaseOrderStatus.PENDING_APPROVAL)
                + saleRepository.countBetween(today.atStartOfDay(), today.plusDays(1).atStartOfDay()));
        return dto;
    }

    public List<ChartPointDto> topSellingProducts(int limit) {
        List<ChartPointDto> result = new ArrayList<>();
        for (Object[] row : saleRepository.topSellingProducts(limit)) {
            result.add(new ChartPointDto((String) row[0], ((Number) row[1]).longValue()));
        }
        return result;
    }

    public List<ChartPointDto> inventoryByCategory() {
        List<ChartPointDto> result = new ArrayList<>();
        try {
            List<Object[]> rows = em.createQuery(
                            "SELECT c.name, SUM(p.quantity) FROM Product p LEFT JOIN p.category c "
                                    + "GROUP BY c.name ORDER BY c.name", Object[].class)
                    .getResultList();
            for (Object[] row : rows) {
                result.add(new ChartPointDto((String) row[0], ((Number) row[1]).longValue()));
            }
        } catch (Exception e) {
            // ignore - keep dashboard resilient
        }
        return result;
    }

    public List<ChartPointDto> revenueTrend(int days) {
        List<ChartPointDto> result = new ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        java.util.LinkedHashMap<String, BigDecimal> byDate = new java.util.LinkedHashMap<>();
        for (int i = days - 1; i >= 0; i--) {
            byDate.put(today.minusDays(i).toString(), BigDecimal.ZERO);
        }
        for (Object[] row : saleRepository.dailyRevenue(days)) {
            byDate.put((String) row[0], (BigDecimal) row[1]);
        }
        byDate.forEach((k, v) -> result.add(new ChartPointDto(k, v)));
        return result;
    }

    /** Human-readable recent activity derived from the audit log. */
    public List<String> recentActivity(int limit) {
        List<String> activity = new ArrayList<>();
        for (AuditLog log : auditLogRepository.findRecent(limit)) {
            activity.add("[" + log.getTimestamp().toLocalDate() + "] "
                    + log.getUsername() + " — " + log.getDescription());
        }
        return activity;
    }

    /** Role-filtered notifications for the current dashboard. */
    public List<NotificationDto> notifications(int limit) {
        String role = authContext.isAuthenticated() ? authContext.getUser().getRole() : "";
        return notificationService.forRole(limit, role).stream()
                .map(n -> new NotificationDto(n.getId(), n.getType(), n.getTitle(),
                        n.getMessage(), n.isRead(), n.getCreatedAt()))
                .collect(Collectors.toList());
    }
}
