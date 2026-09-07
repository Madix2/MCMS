package com.redcode.mcms.service;

import com.redcode.mcms.dto.ChartPointDto;
import com.redcode.mcms.dto.FinanceSummaryDto;
import com.redcode.mcms.entity.PaymentMethod;
import com.redcode.mcms.entity.PurchaseOrderStatus;
import com.redcode.mcms.entity.Sale;
import com.redcode.mcms.repository.PurchaseOrderRepository;
import com.redcode.mcms.repository.SaleRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Finance module: revenue, expenses (purchase-order costs), payment-method
 * breakdown and net revenue. All figures are derived from actual recorded sales
 * and purchase orders, so the dashboard reflects real integrated data.
 */
@Stateless
public class FinanceService {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private SaleRepository saleRepository;

    @Inject
    private PurchaseOrderRepository purchaseOrderRepository;

    public FinanceSummaryDto summary() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate monthStart = today.withDayOfMonth(1);

        FinanceSummaryDto dto = new FinanceSummaryDto();
        dto.setDailyRevenue(saleRepository.sumRevenueBetween(today.atStartOfDay(), today.plusDays(1).atStartOfDay()));
        dto.setWeeklyRevenue(saleRepository.sumRevenueBetween(weekStart.atStartOfDay(), today.plusDays(1).atStartOfDay()));
        dto.setMonthlyRevenue(saleRepository.sumRevenueBetween(monthStart.atStartOfDay(), today.plusDays(1).atStartOfDay()));
        dto.setTotalExpenses(purchaseCostBetween(LocalDate.of(2000, 1, 1).atStartOfDay(), today.plusDays(1).atStartOfDay()));
        dto.setNetRevenue(dto.getMonthlyRevenue().subtract(purchaseCostBetween(monthStart.atStartOfDay(), today.plusDays(1).atStartOfDay())));

        // Payment-method breakdown for the current month
        List<Object[]> rows = em.createQuery(
                        "SELECT p.method, SUM(p.amount) FROM Payment p "
                                + "WHERE p.paymentDate >= :start GROUP BY p.method", Object[].class)
                .setParameter("start", monthStart.atStartOfDay())
                .getResultList();
        for (Object[] row : rows) {
            PaymentMethod method = (PaymentMethod) row[0];
            BigDecimal value = (BigDecimal) row[1];
            switch (method) {
                case CASH -> dto.setCashTotal(value);
                case CARD -> dto.setCardTotal(value);
                case EFT -> dto.setEftTotal(value);
                case DIGITAL -> dto.setDigitalTotal(value);
            }
        }

        dto.setDailySeries(dailyRevenueSeries(14));
        dto.setMonthlySeries(monthlyRevenueSeries(12));
        return dto;
    }

    private BigDecimal purchaseCostBetween(LocalDateTime from, LocalDateTime to) {
        Object result = em.createQuery(
                        "SELECT COALESCE(SUM(po.total), 0) FROM PurchaseOrder po "
                                + "WHERE po.receivedAt IS NOT NULL AND po.receivedAt BETWEEN :from AND :to",
                        BigDecimal.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
        return (BigDecimal) result;
    }

    /** Daily revenue for the last N days (zero-filled so charts are continuous). */
    private List<ChartPointDto> dailyRevenueSeries(int days) {
        Map<String, BigDecimal> byDate = new LinkedHashMap<>();
        for (int i = days - 1; i >= 0; i--) {
            byDate.put(LocalDate.now().minusDays(i).toString(), BigDecimal.ZERO);
        }
        for (Object[] row : saleRepository.dailyRevenue(days)) {
            byDate.put((String) row[0], (BigDecimal) row[1]);
        }
        List<ChartPointDto> series = new ArrayList<>();
        byDate.forEach((k, v) -> series.add(new ChartPointDto(k, v)));
        return series;
    }

    /** Monthly revenue for the last N months. */
    private List<ChartPointDto> monthlyRevenueSeries(int months) {
        List<ChartPointDto> series = new ArrayList<>();
        for (Object[] row : saleRepository.monthlyRevenue(months)) {
            series.add(new ChartPointDto((String) row[0], (BigDecimal) row[1]));
        }
        return series;
    }
}
