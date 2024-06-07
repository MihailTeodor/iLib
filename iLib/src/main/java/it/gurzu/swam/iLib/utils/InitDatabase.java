package it.gurzu.swam.iLib.utils;

import it.gurzu.swam.iLib.dao.UserDao;
import it.gurzu.swam.iLib.model.ModelFactory;
import it.gurzu.swam.iLib.model.User;
import it.gurzu.swam.iLib.model.UserRole;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

@Startup
@Singleton
public class InitDatabase {

	@Inject
	UserDao userDao;

	@PostConstruct
	public void init() {
		final String adminEmail = "admin@example.com";
		User admin;
		try {
			admin = userDao.findUsersByEmail(adminEmail);
		} catch (Exception e) {
			admin = null;
		}

		if (admin == null) {
			admin = ModelFactory.user();
			admin.setEmail(adminEmail);
			admin.setPassword(PasswordUtils.hashPassword("admin password"));
			admin.setRole(UserRole.ADMINISTRATOR);
			admin.setName("Mihail");
			admin.setSurname("Gurzu");
			admin.setAddress("admin address");
			admin.setTelephoneNumber("1234567890");

			userDao.save(admin);
		}
	}
}
