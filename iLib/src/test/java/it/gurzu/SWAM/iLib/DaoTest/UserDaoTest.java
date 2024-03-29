package it.gurzu.SWAM.iLib.DaoTest;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import it.gurzu.swam.iLib.dao.UserDao;
import it.gurzu.swam.iLib.model.ModelFactory;
import it.gurzu.swam.iLib.model.User;

public class UserDaoTest extends JPATest{

	private UserDao userDao;
	private User user;
	
	@Override
	protected void init() throws IllegalAccessException {
		user = ModelFactory.user();
		user.setName("Mihail");
		user.setSurname("Gurzu");
		user.setEmail("myEmail");
		user.setTelephoneNumber("123456789");
		user.setPassword("password");
		
		em.persist(user);
		
		userDao = new UserDao();
		FieldUtils.writeField(userDao, "em", em, true); 
	}
	
	@Test
	public void testFindByIdExistingUser() {
		User retrievedUser = userDao.findById(user.getId());
		Assertions.assertEquals(user, retrievedUser);
	}

	@Test
	public void testFindByIdNonExistingUser() {
		User retrievedUser = userDao.findById(user.getId() + 1);
		Assertions.assertEquals(null, retrievedUser);
	}

	@Test
	public void testSave() {
		User userToPersist = ModelFactory.user();
		userToPersist.setName("Teodor");
		
		userDao.save(userToPersist);
		User manuallyRetrievedUser = 
				em.createQuery("FROM User WHERE uuid = :uuid", User.class)
				  .setParameter("uuid", userToPersist.getUuid())
				  .getSingleResult();
		
		Assertions.assertEquals(userToPersist, manuallyRetrievedUser);
	}
	
	@Test
	public void testDeleteExistingUser() {
		User userToDelete = ModelFactory.user();
		userToDelete.setName("Teodor");
		em.persist(userToDelete);
		
		List<User> manuallyRetrievedUsers = em.createQuery("FROM User WHERE", User.class)
											  .getResultList();
		List<User> tmpUsersList = new ArrayList<User>();
		tmpUsersList.add(user);
		tmpUsersList.add(userToDelete);
		
		Assertions.assertEquals(tmpUsersList, manuallyRetrievedUsers);
	
		userDao.delete(userToDelete);
		tmpUsersList.remove(userToDelete);
		manuallyRetrievedUsers = em.createQuery("FROM User WHERE", User.class)
				  .getResultList();
		
		Assertions.assertEquals(tmpUsersList, manuallyRetrievedUsers);
	}

	@Test
	public void testDeleteNonExistingUser() {
		User tmpUser = ModelFactory.user();
		Assertions.assertThrows(IllegalArgumentException.class, ()->{
			userDao.delete(tmpUser);
		});
	}

	@Test
	public void testFindUsersByEmail() {
		User retrievedUsers = userDao.findUsersByEmail("myEmail");
		Assertions.assertEquals(user, retrievedUsers);
	}
	
	@Test
	public void testFindUsersByName() {
		List<User> retrievedUsers = userDao.findUsersByName("Mihail");
		Assertions.assertEquals(1, retrievedUsers.size());
		Assertions.assertEquals(true, retrievedUsers.contains(user));
	}
	
	@Test
	public void testFindUsersBySurname() {
		List<User> retrievedUsers = userDao.findUsersBySurname("Gurzu");
		Assertions.assertEquals(1, retrievedUsers.size());
		Assertions.assertEquals(true, retrievedUsers.contains(user));
	}
	
	@Test
	public void testFindUsersByTelephoneNumber() {
		List<User> retrievedUsers = userDao.findUsersByTelephoneNumber("123456789");
		Assertions.assertEquals(1, retrievedUsers.size());
		Assertions.assertEquals(true, retrievedUsers.contains(user));
	}
}
