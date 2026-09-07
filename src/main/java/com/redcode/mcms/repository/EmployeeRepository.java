package com.redcode.mcms.repository;

import com.redcode.mcms.entity.Employee;
import com.redcode.mcms.entity.Role;
import jakarta.ejb.Stateless;

import java.util.List;

/**
 * Data-access for {@link Employee} records.
 */
@Stateless
public class EmployeeRepository extends GenericRepository<Employee, Long> {

    public List<Employee> search(String term) {
        if (term == null || term.trim().isEmpty()) {
            return findAll();
        }
        String like = "%" + term.trim().toLowerCase() + "%";
        return em.createQuery(
                        "SELECT e FROM Employee e WHERE lower(e.fullName) LIKE :t"
                                + " OR lower(e.email) LIKE :t OR lower(e.position) LIKE :t ORDER BY e.fullName",
                        Employee.class)
                .setParameter("t", like)
                .getResultList();
    }

    public List<Employee> findByDepartment(Long departmentId) {
        return em.createQuery("SELECT e FROM Employee e WHERE e.department.id = :id ORDER BY e.fullName",
                        Employee.class)
                .setParameter("id", departmentId)
                .getResultList();
    }

    public List<Employee> findByRole(Role role) {
        return em.createQuery("SELECT e FROM Employee e WHERE e.role = :r ORDER BY e.fullName",
                        Employee.class)
                .setParameter("r", role)
                .getResultList();
    }

    public long countActive() {
        return em.createQuery("SELECT COUNT(e) FROM Employee e WHERE e.active = true", Long.class)
                .getSingleResult();
    }
}
