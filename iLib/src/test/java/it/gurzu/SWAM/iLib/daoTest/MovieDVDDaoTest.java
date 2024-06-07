package it.gurzu.SWAM.iLib.daoTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;

import it.gurzu.swam.iLib.dao.MovieDVDDao;
import it.gurzu.swam.iLib.model.ModelFactory;
import it.gurzu.swam.iLib.model.MovieDVD;

public class MovieDVDDaoTest extends JPATest {

	private MovieDVDDao movieDVDDao;
	private MovieDVD movieDVD;
	
	@Override
	protected void init() throws IllegalAccessException {
		movieDVD = ModelFactory.movieDVD();
		movieDVD.setIsan("123");
		
		em.persist(movieDVD);
		
		movieDVDDao = new MovieDVDDao();
		FieldUtils.writeField(movieDVDDao, "em", em, true); 
	}
	
	@Test
	public void testFindMoviesByIsan() {
		List<MovieDVD> retrievedMovieDVDs = movieDVDDao.findMoviesByIsan("123");
		assertEquals(1, retrievedMovieDVDs.size());
		assertEquals(true, retrievedMovieDVDs.contains(movieDVD));
	}

	@Test
	public void testCountMoviesByIsan() {
		MovieDVD movieDVD2 = ModelFactory.movieDVD();
		movieDVD2.setIsan("123");
		
		em.persist(movieDVD2);

		MovieDVD movieDVD3 = ModelFactory.movieDVD();
		movieDVD3.setIsan("123");
		
		em.persist(movieDVD3);

		Long resultsNumber = movieDVDDao.countMoviesByIsan("123");
		
		assertEquals(3, resultsNumber);
	}
}
