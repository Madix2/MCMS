package com.redcode.mcms.repository;

import com.redcode.mcms.entity.Supplier;
import com.redcode.mcms.entity.Status;
import jakarta.ejb.Stateless;

import java.util.List;

/**
 * Data-access for {@link Supplier}.
 */
@Stateless
public class SupplierRepository extends GenericRepository<Supplier, Long> {

    public List<Supplier> search(String term) {
        if (term == null || term.trim().isEmpty()) {
            return findAll();
        }
        String like = "%" + term.trim().toLowerCase() + "%";
        return em.createQuery(
                        "SELECT s FROM Supplier s WHERE lower(s.name) LIKE :t"
                                + " OR lower(s.contactPerson) LIKE :t ORDER BY s.name", Supplier.class)
                .setParameter("t", like)
                .getResultList();
    }

    public List<Supplier> findActive() {
        return em.createQuery("SELECT s FROM Supplier s WHERE s.status = :s ORDER BY s.name", Supplier.class)
                .setParameter("s", Status.ACTIVE)
                .getResultList();
    }
}
