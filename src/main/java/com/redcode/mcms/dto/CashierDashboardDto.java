package com.redcode.mcms.dto;

public class CashierDashboardDto {
    private long totalCustomers;
    public CashierDashboardDto() { }
    public CashierDashboardDto(long totalCustomers) { this.totalCustomers = totalCustomers; }
    public long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(long totalCustomers) { this.totalCustomers = totalCustomers; }
}
