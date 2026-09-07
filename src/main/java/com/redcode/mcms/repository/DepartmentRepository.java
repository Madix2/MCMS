package com.redcode.mcms.repository;

import com.redcode.mcms.entity.Department;
import jakarta.ejb.Stateless;

/**
 * Data-access for {@link Department}.
 */
@Stateless
public class DepartmentRepository extends GenericRepository<Department, Long> {

    public Department findByName(String name) {
        return em.createQuery("SELECT d FROM Department d WHERE d.name = :n", Department.class)
                .setParameter("n", name)
                .getResultList().stream().findFirst().orElse(null);
    }
}
