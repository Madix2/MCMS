package com.redcode.mcms.repository;

import com.redcode.mcms.entity.Customer;
import com.redcode.mcms.entity.Status;
import jakarta.ejb.Stateless;

import java.util.List;

/**
 * Data-access for {@link Customer}.
 */
@Stateless
public class CustomerRepository extends GenericRepository<Customer, Long> {

    public List<Customer> search(String term) {
        if (term == null || term.trim().isEmpty()) {
            return findAll();
        }
        String like = "%" + term.trim().toLowerCase() + "%";
        return em.createQuery(
                        "SELECT c FROM Customer c WHERE lower(c.fullName) LIKE :t"
                                + " OR lower(c.email) LIKE :t OR lower(c.phone) LIKE :t ORDER BY c.fullName",
                        Customer.class)
                .setParameter("t", like)
                .getResultList();
    }

    public long countByStatus(Status status) {
        return em.createQuery("SELECT COUNT(c) FROM Customer c WHERE c.status = :s", Long.class)
                .setParameter("s", status)
                .getSingleResult();
    }
}
