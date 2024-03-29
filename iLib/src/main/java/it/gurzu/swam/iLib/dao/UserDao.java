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
	
	public List<User> findUsersByName(String name){
		return this.em.createQuery("FROM User where name = :name", User.class)
			.setParameter("name", name)
			.getResultList();
	}
	
	public List<User> findUsersBySurname(String surname){
		return this.em.createQuery("FROM User where surname = :surname", User.class)
			.setParameter("surname", surname)
			.getResultList();
	}


	public List<User> findUsersByTelephoneNumber(String telephoneNumber){
		return this.em.createQuery("FROM User where telephoneNumber = :telephoneNumber", User.class)
			.setParameter("telephoneNumber", telephoneNumber)
			.getResultList();
	}

}
