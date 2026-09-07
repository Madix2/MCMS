package com.redcode.mcms.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

/**
 * Generic repository providing basic CRUD for an entity type.
 * Concrete repositories extend this and add domain-specific queries.
 *
 * The entity class is resolved by walking up the class hierarchy so that it
 * still works when CDI/Weld wraps the repository in a proxy subclass (whose
 * generic superclass is a plain {@link Class} rather than a
 * {@link ParameterizedType}).
 */
public abstract class GenericRepository<T, ID> {

    @PersistenceContext
    protected EntityManager em;

    private final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    protected GenericRepository() {
        this.entityClass = resolveEntityClass();
    }

    @SuppressWarnings("unchecked")
    private Class<T> resolveEntityClass() {
        Class<?> current = getClass();
        while (current != null && current != GenericRepository.class) {
            Type superType = current.getGenericSuperclass();
            if (superType instanceof ParameterizedType) {
                Type[] args = ((ParameterizedType) superType).getActualTypeArguments();
                if (args.length > 0 && args[0] instanceof Class) {
                    return (Class<T>) args[0];
                }
            }
            current = current.getSuperclass();
        }
        throw new IllegalStateException("Could not resolve entity class for repository " + getClass().getName());
    }

    public T save(T entity) {
        if (hasId(entity)) {
            return em.merge(entity);
        }
        em.persist(entity);
        return entity;
    }

    public T update(T entity) {
        return em.merge(entity);
    }

    private boolean hasId(T entity) {
        try {
            java.lang.reflect.Method getId = entityClass.getMethod("getId");
            return getId.invoke(entity) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public Optional<T> findById(ID id) {
        return Optional.ofNullable(em.find(entityClass, id));
    }

    public List<T> findAll() {
        return em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass)
                .getResultList();
    }

    public long count() {
        return em.createQuery("SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e", Long.class)
                .getSingleResult();
    }
}
