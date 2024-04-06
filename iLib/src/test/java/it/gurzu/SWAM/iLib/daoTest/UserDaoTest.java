package it.gurzu.SWAM.iLib.daoTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
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
		
		em.persist(user);
		
		userDao = new UserDao();
		FieldUtils.writeField(userDao, "em", em, true); 
	}
	
	@Test
	public void testFindByIdExistingUser() {
		User retrievedUser = userDao.findById(user.getId());
		assertEquals(user, retrievedUser);
	}

	@Test
	public void testFindByIdNonExistingUser() {
		User retrievedUser = userDao.findById(user.getId() + 1);
		assertEquals(null, retrievedUser);
	}

	@Test
	public void testSave() {
		User userToPersist = ModelFactory.user();
		
		userDao.save(userToPersist);
		User manuallyRetrievedUser = 
				em.createQuery("FROM User WHERE uuid = :uuid", User.class)
				  .setParameter("uuid", userToPersist.getUuid())
				  .getSingleResult();
		
		assertEquals(userToPersist, manuallyRetrievedUser);
	}
	
	@Test
	public void testUpdate() {
		user.setName("Teodor");
		
		userDao.save(user);
		User manuallyRetrievedUser = 
				em.createQuery("FROM User WHERE uuid = :uuid", User.class)
					.setParameter("uuid", user.getUuid())
					.getSingleResult();

		assertEquals(user, manuallyRetrievedUser);
	}
	
	@Test
	public void testDeleteExistingUser() {
		User userToDelete = ModelFactory.user();
		em.persist(userToDelete);
		
		List<User> manuallyRetrievedUsers = em.createQuery("FROM User WHERE", User.class)
											  .getResultList();
		List<User> tmpUsersList = new ArrayList<User>();
		tmpUsersList.add(user);
		tmpUsersList.add(userToDelete);
		
		assertEquals(tmpUsersList, manuallyRetrievedUsers);
	
		userDao.delete(userToDelete);
		tmpUsersList.remove(userToDelete);
		manuallyRetrievedUsers = em.createQuery("FROM User WHERE", User.class)
				  .getResultList();
		
		assertEquals(tmpUsersList, manuallyRetrievedUsers);
	}

	@Test
	public void testDelete_WhenUserNotExist_ThrowsIllegalArgumentException() {
		User tmpUser = ModelFactory.user();
		Exception thrownException = assertThrows(IllegalArgumentException.class, ()->{
			userDao.delete(tmpUser);			
		});
		assertEquals("Entity is not persisted!", thrownException.getMessage());	
	}

	@Test
	public void testFindUsersByEmail() {
		User retrievedUsers = userDao.findUsersByEmail("myEmail");
		assertEquals(user, retrievedUsers);
	}

	@Test
	public void testFindUsers() {
		User userToAdd = ModelFactory.user();
		userToAdd.setName("Mihail");
		em.persist(userToAdd);
		
		List<User> retrievedUsers = userDao.findUsers("Mihail", "inexistingSurname", null);
		assertEquals(true, retrievedUsers.isEmpty());
		
		retrievedUsers = userDao.findUsers("Mihail", "Gurzu", null);
		assertEquals(1, retrievedUsers.size());
		assertEquals(true, retrievedUsers.contains(user));
		
		retrievedUsers = userDao.findUsers("Mihail", null, null);
		List<User> targetUserList = new ArrayList<User>();
		targetUserList.add(user);
		targetUserList.add(userToAdd);
		assertEquals(true, retrievedUsers.containsAll(targetUserList));
	}
}
