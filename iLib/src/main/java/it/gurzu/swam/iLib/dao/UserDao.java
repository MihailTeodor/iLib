package it.gurzu.swam.iLib.dao;

import java.util.List;

import it.gurzu.swam.iLib.model.User;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class UserDao extends BaseDao<User> {
	
	public UserDao() {
		super(User.class);
	}
	
	public User findUsersByEmail(String email){
		return this.em.createQuery("FROM User where email = :email", User.class)
			.setParameter("email", email)
			.getSingleResult();
	}
	
	public List<User> findUsers(String name, String surname, String telephoneNumber, int fromIndex, int limit){
		return this.em.createQuery(
				"FROM User u WHERE "
				+ "(:name is null or u.name = :name) and "
				+ "(:surname is null or u.surname = :surname) and"
				+ "(:telephoneNumber is null or u.telephoneNumber = :telephoneNumber)"
				+ "ORDER BY u.name ASC, u.surname ASC", User.class)
				.setParameter("name", name)
				.setParameter("surname", surname)
				.setParameter("telephoneNumber", telephoneNumber)
				.setFirstResult(fromIndex)
				.setMaxResults(limit)
				.getResultList();
	}

	public Long countUsers(String name, String surname, String telephoneNumber){
		return this.em.createQuery(
				"SELECT COUNT(u) FROM User u WHERE "
				+ "(:name is null or u.name = :name) and "
				+ "(:surname is null or u.surname = :surname) and"
				+ "(:telephoneNumber is null or u.telephoneNumber = :telephoneNumber)", Long.class)
				.setParameter("name", name)
				.setParameter("surname", surname)
				.setParameter("telephoneNumber", telephoneNumber)
				.getSingleResult();
	}

}
