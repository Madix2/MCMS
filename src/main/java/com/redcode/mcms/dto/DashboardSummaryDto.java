package com.redcode.mcms.dto;

import java.math.BigDecimal;

/**
 * Aggregated figures for the management dashboard.
 */
public class DashboardSummaryDto {

    private BigDecimal todaysSales = BigDecimal.ZERO;
    private long todaysSalesCount;
    private BigDecimal monthlyRevenue = BigDecimal.ZERO;
    private long totalProducts;
    private long lowStockProducts;
    private long pendingPurchaseOrders;
    private long totalCustomers;
    private long activeEmployees;
    private long pendingTransactions;

    public BigDecimal getTodaysSales() { return todaysSales; }
    public void setTodaysSales(BigDecimal todaysSales) { this.todaysSales = todaysSales; }
    public long getTodaysSalesCount() { return todaysSalesCount; }
    public void setTodaysSalesCount(long todaysSalesCount) { this.todaysSalesCount = todaysSalesCount; }
    public BigDecimal getMonthlyRevenue() { return monthlyRevenue; }
    public void setMonthlyRevenue(BigDecimal monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }
    public long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }
    public long getLowStockProducts() { return lowStockProducts; }
    public void setLowStockProducts(long lowStockProducts) { this.lowStockProducts = lowStockProducts; }
    public long getPendingPurchaseOrders() { return pendingPurchaseOrders; }
    public void setPendingPurchaseOrders(long pendingPurchaseOrders) { this.pendingPurchaseOrders = pendingPurchaseOrders; }
    public long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(long totalCustomers) { this.totalCustomers = totalCustomers; }
    public long getActiveEmployees() { return activeEmployees; }
    public void setActiveEmployees(long activeEmployees) { this.activeEmployees = activeEmployees; }
    public long getPendingTransactions() { return pendingTransactions; }
    public void setPendingTransactions(long pendingTransactions) { this.pendingTransactions = pendingTransactions; }
}
