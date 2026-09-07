package com.redcode.mcms.repository;

import com.redcode.mcms.entity.Category;
import jakarta.ejb.Stateless;

/**
 * Data-access for {@link Category}.
 */
@Stateless
public class CategoryRepository extends GenericRepository<Category, Long> {

    public Category findByName(String name) {
        return em.createQuery("SELECT c FROM Category c WHERE c.name = :n", Category.class)
                .setParameter("n", name)
                .getResultList().stream().findFirst().orElse(null);
    }
}
