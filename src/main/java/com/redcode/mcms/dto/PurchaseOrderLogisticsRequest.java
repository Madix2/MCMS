package com.redcode.mcms.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class PurchaseOrderLogisticsRequest {
    @NotNull private Boolean paid;
    @NotNull private Boolean inTransit;
    @NotNull private LocalDate expectedArrivalDate;
    private String notes;
    public Boolean getPaid() { return paid; }
    public void setPaid(Boolean paid) { this.paid = paid; }
    public Boolean getInTransit() { return inTransit; }
    public void setInTransit(Boolean inTransit) { this.inTransit = inTransit; }
    public LocalDate getExpectedArrivalDate() { return expectedArrivalDate; }
    public void setExpectedArrivalDate(LocalDate expectedArrivalDate) { this.expectedArrivalDate = expectedArrivalDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
