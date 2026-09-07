package com.redcode.mcms.repository;

import com.redcode.mcms.entity.Attendance;
import jakarta.ejb.Stateless;

import java.time.LocalDate;
import java.util.List;

/**
 * Data-access for {@link Attendance}.
 */
@Stateless
public class AttendanceRepository extends GenericRepository<Attendance, Long> {

    public List<Attendance> findByEmployee(Long employeeId) {
        return em.createQuery("SELECT a FROM Attendance a WHERE a.employee.id = :id ORDER BY a.date DESC",
                        Attendance.class)
                .setParameter("id", employeeId)
                .getResultList();
    }

    public List<Attendance> findByDate(LocalDate date) {
        return em.createQuery("SELECT a FROM Attendance a WHERE a.date = :d ORDER BY a.employee.fullName",
                        Attendance.class)
                .setParameter("d", date)
                .getResultList();
    }

    public Attendance findUnique(Long employeeId, LocalDate date) {
        return em.createQuery(
                        "SELECT a FROM Attendance a WHERE a.employee.id = :e AND a.date = :d", Attendance.class)
                .setParameter("e", employeeId)
                .setParameter("d", date)
                .getResultList().stream().findFirst().orElse(null);
    }
}
