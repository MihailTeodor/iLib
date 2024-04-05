package it.gurzu.SWAM.iLib.daoTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;

import it.gurzu.swam.iLib.dao.MagazineDao;
import it.gurzu.swam.iLib.model.Magazine;
import it.gurzu.swam.iLib.model.ModelFactory;

public class MagazineDaoTest extends JPATest {

	private Magazine magazine;
	private MagazineDao magazineDao;
	
	@Override
	protected void init() throws IllegalAccessException {
		magazine = ModelFactory.magazine();
		magazine.setIssueNumber(3);
		magazine.setIssn("1234567");
		
		em.persist(magazine);
		
		magazineDao = new MagazineDao();
		FieldUtils.writeField(magazineDao, "em", em, true); 
	}
	
	@Test
	public void testFindMagazinesByIssn() {
		List<Magazine> retrievedMagazines = magazineDao.findMagazinesByIssn("1234567");
		assertEquals(1, retrievedMagazines.size());
		assertEquals(true, retrievedMagazines.contains(magazine));
	}

	
	
}
