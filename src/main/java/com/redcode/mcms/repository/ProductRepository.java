package com.redcode.mcms.repository;

import com.redcode.mcms.entity.Product;
import com.redcode.mcms.entity.Status;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Data-access for {@link Product}.
 */
@Stateless
public class ProductRepository extends GenericRepository<Product, Long> {

    public List<Product> search(String term) {
        if (term == null || term.trim().isEmpty()) {
            return findAll();
        }
        String like = "%" + term.trim().toLowerCase() + "%";
        return em.createQuery(
                        "SELECT p FROM Product p WHERE"
                                + " lower(p.name) LIKE :t OR lower(p.barcode) LIKE :t"
                                + " OR lower(p.category.name) LIKE :t ORDER BY p.name", Product.class)
                .setParameter("t", like)
                .getResultList();
    }

    public List<Product> findByCategory(Long categoryId) {
        return em.createQuery("SELECT p FROM Product p WHERE p.category.id = :id ORDER BY p.name",
                        Product.class)
                .setParameter("id", categoryId)
                .getResultList();
    }

    public List<Product> findLowStock() {
        // JPQL cannot reference the helper method, so compare quantity directly.
        return em.createQuery("SELECT p FROM Product p WHERE p.quantity <= p.minStockLevel "
                        + "AND p.status = :status ORDER BY p.name", Product.class)
                .setParameter("status", Status.ACTIVE)
                .getResultList();
    }

    public List<Product> findBySupplier(Long supplierId) {
        return em.createQuery("SELECT p FROM Product p WHERE p.supplier.id = :id ORDER BY p.name",
                        Product.class)
                .setParameter("id", supplierId)
                .getResultList();
    }

    public List<Product> findActive() {
        return em.createQuery("SELECT p FROM Product p WHERE p.status = :s ORDER BY p.name",
                        Product.class)
                .setParameter("s", Status.ACTIVE)
                .getResultList();
    }

    public long countByStatus(Status status) {
        return em.createQuery("SELECT COUNT(p) FROM Product p WHERE p.status = :s", Long.class)
                .setParameter("s", status)
                .getSingleResult();
    }
}
