package com.redcode.mcms.repository;

import com.redcode.mcms.entity.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

/**
 * Data-access for {@link User} login credentials.
 */
@Stateless
public class UserRepository {

    @PersistenceContext
    private EntityManager em;

    public User save(User user) {
        if (user.getId() == null) {
            em.persist(user);
            return user;
        }
        return em.merge(user);
    }

    public User update(User user) {
        return em.merge(user);
    }

    public Optional<User> findById(Long id) {
        User u = em.find(User.class, id);
        return Optional.ofNullable(u);
    }

    public Optional<User> findByUsername(String username) {
        TypedQuery<User> q = em.createQuery(
                "SELECT u FROM User u WHERE lower(u.username) = lower(:username)", User.class);
        q.setParameter("username", username);
        return q.getResultList().stream().findFirst();
    }

    public List<User> findAll() {
        return em.createQuery("SELECT u FROM User u", User.class).getResultList();
    }

    public long count() {
        return em.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult();
    }
}
