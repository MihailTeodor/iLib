package it.gurzu.SWAM.iLib.daoTest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public abstract class JPATest {
	private static EntityManagerFactory emf;
	protected EntityManager em;
	
	@BeforeAll
	public static void setupEM() {
		emf = Persistence.createEntityManagerFactory("test");
	}
	
	@BeforeEach
	public void setup() throws IllegalAccessException {
		em = emf.createEntityManager();
		em.getTransaction().begin();
		em.createNativeQuery("TRUNCATE SCHEMA public AND COMMIT").executeUpdate();
		em.getTransaction().commit();
		em.getTransaction().begin();
		init();
		em.getTransaction().commit();
		em.clear();
		em.getTransaction().begin();
	}
	
	@AfterEach
	public void close() {
		if(em.getTransaction().isActive()) {
			em.getTransaction().rollback();
		}
		em.close();
	}
	
	@AfterAll
	public static void tearDownDB() {
		emf.close();
	}
	
	protected abstract void init() throws IllegalAccessException;
}
