package it.gurzu.swam.iLib.dao;

import java.util.List;

import it.gurzu.swam.iLib.model.MovieDVD;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class MovieDVDDao extends BaseDao<MovieDVD> {
	
	public MovieDVDDao() {
		super(MovieDVD.class);
	}
	
	public List<MovieDVD> findMoviesByIsan(String isan) {
		return this.em.createQuery("FROM MovieDVD where isan = :isan", MovieDVD.class)
				.setParameter("isan", isan)
				.getResultList();		
	}

	public Long countMoviesByIsan(String isan) {
		return this.em.createQuery("SELECT COUNT(m) FROM MovieDVD m where m.isan = :isan", Long.class)
				.setParameter("isan", isan)
				.getSingleResult();		
	}

}
