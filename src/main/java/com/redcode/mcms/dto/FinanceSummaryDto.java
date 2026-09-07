package com.redcode.mcms.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Financial summary returned by the finance dashboard/reports module.
 */
public class FinanceSummaryDto {

    private BigDecimal dailyRevenue = BigDecimal.ZERO;
    private BigDecimal weeklyRevenue = BigDecimal.ZERO;
    private BigDecimal monthlyRevenue = BigDecimal.ZERO;
    private BigDecimal totalExpenses = BigDecimal.ZERO;
    private BigDecimal netRevenue = BigDecimal.ZERO;
    private BigDecimal cashTotal = BigDecimal.ZERO;
    private BigDecimal cardTotal = BigDecimal.ZERO;
    private BigDecimal eftTotal = BigDecimal.ZERO;
    private BigDecimal digitalTotal = BigDecimal.ZERO;

    /** List of {label, value} for charting. */
    private List<ChartPointDto> dailySeries = new ArrayList<>();
    private List<ChartPointDto> monthlySeries = new ArrayList<>();

    public BigDecimal getDailyRevenue() { return dailyRevenue; }
    public void setDailyRevenue(BigDecimal dailyRevenue) { this.dailyRevenue = dailyRevenue; }
    public BigDecimal getWeeklyRevenue() { return weeklyRevenue; }
    public void setWeeklyRevenue(BigDecimal weeklyRevenue) { this.weeklyRevenue = weeklyRevenue; }
    public BigDecimal getMonthlyRevenue() { return monthlyRevenue; }
    public void setMonthlyRevenue(BigDecimal monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }
    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }
    public BigDecimal getNetRevenue() { return netRevenue; }
    public void setNetRevenue(BigDecimal netRevenue) { this.netRevenue = netRevenue; }
    public BigDecimal getCashTotal() { return cashTotal; }
    public void setCashTotal(BigDecimal cashTotal) { this.cashTotal = cashTotal; }
    public BigDecimal getCardTotal() { return cardTotal; }
    public void setCardTotal(BigDecimal cardTotal) { this.cardTotal = cardTotal; }
    public BigDecimal getEftTotal() { return eftTotal; }
    public void setEftTotal(BigDecimal eftTotal) { this.eftTotal = eftTotal; }
    public BigDecimal getDigitalTotal() { return digitalTotal; }
    public void setDigitalTotal(BigDecimal digitalTotal) { this.digitalTotal = digitalTotal; }
    public List<ChartPointDto> getDailySeries() { return dailySeries; }
    public void setDailySeries(List<ChartPointDto> dailySeries) { this.dailySeries = dailySeries; }
    public List<ChartPointDto> getMonthlySeries() { return monthlySeries; }
    public void setMonthlySeries(List<ChartPointDto> monthlySeries) { this.monthlySeries = monthlySeries; }
}
