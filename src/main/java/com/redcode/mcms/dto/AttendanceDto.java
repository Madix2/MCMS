package com.redcode.mcms.dto;

import java.time.LocalDate;

/**
 * Attendance record for HR.
 */
public class AttendanceDto {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate date;
    private String clockIn;
    private String clockOut;
    private boolean present;
    private String notes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getClockIn() { return clockIn; }
    public void setClockIn(String clockIn) { this.clockIn = clockIn; }
    public String getClockOut() { return clockOut; }
    public void setClockOut(String clockOut) { this.clockOut = clockOut; }
    public boolean isPresent() { return present; }
    public void setPresent(boolean present) { this.present = present; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
