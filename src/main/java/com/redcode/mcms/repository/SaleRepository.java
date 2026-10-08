package com.redcode.mcms.repository;

import com.redcode.mcms.entity.Sale;
import com.redcode.mcms.entity.Status;
import jakarta.ejb.Stateless;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data-access for {@link Sale} records.
 */
@Stateless
public class SaleRepository extends GenericRepository<Sale, Long> {

    public List<Sale> search(String term) {
        return search(term, null, null);
    }

    public List<Sale> search(String term, LocalDate from, LocalDate to) {
        StringBuilder jpql = new StringBuilder("SELECT s FROM Sale s WHERE 1 = 1");
        boolean hasTerm = term != null && !term.trim().isEmpty();
        if (hasTerm) jpql.append(" AND (lower(s.saleNumber) LIKE :t OR lower(s.customer.fullName) LIKE :t)");
        if (from != null) jpql.append(" AND s.saleDate >= :from");
        if (to != null) jpql.append(" AND s.saleDate < :to");
        jpql.append(" ORDER BY s.saleDate DESC");

        var query = em.createQuery(jpql.toString(), Sale.class);
        if (hasTerm) query.setParameter("t", "%" + term.trim().toLowerCase() + "%");
        if (from != null) query.setParameter("from", from.atStartOfDay());
        if (to != null) query.setParameter("to", to.plusDays(1).atStartOfDay());
        return query.getResultList();
    }

    public List<Sale> findAllOrderedDesc() {
        return em.createQuery("SELECT s FROM Sale s ORDER BY s.saleDate DESC", Sale.class)
                .getResultList();
    }

    public List<Sale> findByCustomer(Long customerId) {
        return em.createQuery("SELECT s FROM Sale s WHERE s.customer.id = :id ORDER BY s.saleDate DESC",
                        Sale.class)
                .setParameter("id", customerId)
                .getResultList();
    }

    public List<Sale> findByCashier(Long cashierId) {
        return em.createQuery("SELECT s FROM Sale s WHERE s.cashier.id = :cashierId ORDER BY s.saleDate DESC",
                        Sale.class)
                .setParameter("cashierId", cashierId)
                .getResultList();
    }

    public List<Sale> findTodayByCashier(Long cashierId, LocalDateTime start, LocalDateTime end) {
        return em.createQuery("SELECT s FROM Sale s WHERE s.cashier.id = :cashierId "
                        + "AND s.saleDate >= :start AND s.saleDate < :end ORDER BY s.saleDate DESC", Sale.class)
                .setParameter("cashierId", cashierId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }

    public BigDecimal sumRevenueBetween(LocalDateTime from, LocalDateTime to) {
        BigDecimal total = em.createQuery(
                        "SELECT COALESCE(SUM(s.total), 0) FROM Sale s WHERE s.saleDate BETWEEN :from AND :to",
                        BigDecimal.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
        return total;
    }

    public long countBetween(LocalDateTime from, LocalDateTime to) {
        return em.createQuery(
                        "SELECT COUNT(s) FROM Sale s WHERE s.saleDate BETWEEN :from AND :to", Long.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
    }

    public long countToday() {
        LocalDate today = LocalDate.now();
        return countBetween(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
    }

    public BigDecimal sumToday() {
        LocalDate today = LocalDate.now();
        return sumRevenueBetween(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
    }

    /** Monthly revenue aggregated per month (YYYY-MM) for the last 12 months. */
    public List<Object[]> monthlyRevenue(int months) {
        LocalDateTime start = LocalDate.now().minusMonths(months).atStartOfDay();
        return em.createNativeQuery(
                        "SELECT to_char(sale_date, 'YYYY-MM') AS ym, SUM(total) AS total "
                                + "FROM sale WHERE sale_date >= ?1 GROUP BY 1 ORDER BY 1")
                .setParameter(1, start)
                .getResultList();
    }

    /** Daily revenue for the last N days. */
    public List<Object[]> dailyRevenue(int days) {
        LocalDateTime start = LocalDate.now().minusDays(days).atStartOfDay();
        return em.createNativeQuery(
                        "SELECT to_char(sale_date, 'YYYY-MM-DD') AS d, SUM(total) AS total "
                                + "FROM sale WHERE sale_date >= ?1 GROUP BY 1 ORDER BY 1")
                .setParameter(1, start)
                .getResultList();
    }

    /** Top-N selling products by total quantity. */
    public List<Object[]> topSellingProducts(int limit) {
        return em.createQuery(
                        "SELECT si.product.name, SUM(si.quantity) FROM SaleItem si "
                                + "GROUP BY si.product.name ORDER BY SUM(si.quantity) DESC", Object[].class)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<Sale> findByStatus(Status status) {
        return em.createQuery("SELECT s FROM Sale s WHERE s.status = :s ORDER BY s.saleDate DESC",
                        Sale.class)
                .setParameter("s", status)
                .getResultList();
    }
}
