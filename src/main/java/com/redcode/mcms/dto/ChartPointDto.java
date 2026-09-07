package com.redcode.mcms.dto;

import java.math.BigDecimal;

/**
 * A generic (label, value) pair used to power dashboard charts.
 */
public class ChartPointDto {

    private String label;
    private BigDecimal value;

    public ChartPointDto() { }

    public ChartPointDto(String label, BigDecimal value) {
        this.label = label;
        this.value = value;
    }

    public ChartPointDto(String label, long value) {
        this(label, BigDecimal.valueOf(value));
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
}
