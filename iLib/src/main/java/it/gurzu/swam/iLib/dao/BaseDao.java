package it.gurzu.swam.iLib.dao;

import it.gurzu.swam.iLib.model.BaseEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public abstract class BaseDao<E extends BaseEntity> {

	private final Class<E> type;

	@PersistenceContext
	protected EntityManager em;
	
	protected BaseDao(Class<E> type) {
		this.type = type;
	}
	
	public E findById(Long id) {
		return em.find(type, id);
	}
	
	public void save(E entity) {
		if(entity.getId() != null)
			em.merge(entity);
		else 
			em.persist(entity);
	}
	
	public void delete(E entity) {
		if(entity.getId() != null)
			em.remove(em.contains(entity) ? entity : em.merge(entity));
		else 
			throw new IllegalArgumentException("Entity is not persisted!");
	}
}
