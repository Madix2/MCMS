package com.redcode.mcms.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Attendance record for an employee.
 */
@Entity
@Table(name = "attendance",
        uniqueConstraints = @UniqueConstraint(name = "uq_attendance_emp_day", columnNames = {"employee_id", "attendance_date"}))
public class Attendance implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate date;

    @Column(name = "clock_in")
    private String clockIn;

    @Column(name = "clock_out")
    private String clockOut;

    @Column(nullable = false)
    private boolean present = true;

    @Column(length = 255)
    private String notes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
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
