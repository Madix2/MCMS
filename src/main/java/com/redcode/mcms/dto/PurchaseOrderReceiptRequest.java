package com.redcode.mcms.dto;

import jakarta.validation.constraints.Size;

public class PurchaseOrderReceiptRequest {
    @Size(max = 1000) private String receivingNotes;
    @Size(max = 1000) private String missingItems;
    public String getReceivingNotes() { return receivingNotes; }
    public void setReceivingNotes(String receivingNotes) { this.receivingNotes = receivingNotes; }
    public String getMissingItems() { return missingItems; }
    public void setMissingItems(String missingItems) { this.missingItems = missingItems; }
}
