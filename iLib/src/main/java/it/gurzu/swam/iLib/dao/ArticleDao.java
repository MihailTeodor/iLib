package it.gurzu.swam.iLib.dao;

import java.sql.Date;
import java.util.List;

import it.gurzu.swam.iLib.model.Article;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class ArticleDao extends BaseDao<Article> {
	
	public ArticleDao() {
		super(Article.class);
	}
		
	public List<Article> findArticlesByTitle(String title){
		return this.em.createQuery("FROM Article where title = :title", Article.class)
			.setParameter("title", title)
			.getResultList();
	}
	
	public List<Article> findArticlesByGenre(String genre){
		return this.em.createQuery("FROM Article where genre = :genre", Article.class)
			.setParameter("genre", genre)
			.getResultList();
	}
	
	public List<Article> findArticlesByPublisher(String publisher){
		return this.em.createQuery("FROM Article where publisher = :publisher", Article.class)
			.setParameter("publisher", publisher)
			.getResultList();
	}

	public List<Article> findArticlesByYearEdition(String yearEdition){
		return this.em.createQuery("FROM Article where yearEdition = :yearEdition", Article.class)
			.setParameter("yearEdition", Date.valueOf(yearEdition))
			.getResultList();
	}

}
