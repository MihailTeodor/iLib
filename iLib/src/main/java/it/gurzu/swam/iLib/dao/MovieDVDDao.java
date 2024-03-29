package it.gurzu.swam.iLib.dao;

import java.util.List;

import it.gurzu.swam.iLib.model.MovieDVD;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class MovieDVDDao extends BaseDao<MovieDVD> {
	
	public MovieDVDDao() {
		super(MovieDVD.class);
	}
	
	public List<MovieDVD> findMoviesByDirector(String director) {
		return this.em.createQuery("FROM MovieDVD where director = :director", MovieDVD.class)
				.setParameter("director", director)
				.getResultList();		
	}

	public List<MovieDVD> findMoviesByIsan(String isan) {
		return this.em.createQuery("FROM MovieDVD where isan = :isan", MovieDVD.class)
				.setParameter("isan", isan)
				.getResultList();		
	}
	
}
