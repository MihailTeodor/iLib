package it.gurzu.swam.iLib.dao;

import java.time.LocalDate;
import java.util.List;

import it.gurzu.swam.iLib.model.Article;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class ArticleDao extends BaseDao<Article> {
	
	public ArticleDao() {
		super(Article.class);
	}
			
	public List<Article> findArticles(String title, String genre, String publisher, LocalDate yearEdition, String author, Integer issueNumber, String director, int fromIndex, int limit){
		return this.em.createQuery(
				"FROM Article a WHERE "
				+ "(:title is null or a.title = :title) and "
				+ "(:genre is null or a.genre = :genre) and"
				+ "(:publisher is null or a.publisher = :publisher) and"
				+ "(:yearEdition is null or a.yearEdition = :yearEdition) and"
				+ "(:author is null or a.author = :author) and"
				+ "(:issueNumber is null or a.issueNumber = :issueNumber) and"
				+ "(:director is null or a.director = :director)"
				+ "ORDER BY a.yearEdition DESC, a.title ASC", Article.class)
				.setParameter("title", title)
				.setParameter("genre", genre)
				.setParameter("publisher", publisher)
				.setParameter("yearEdition", yearEdition)
				.setParameter("author", author)
				.setParameter("issueNumber", issueNumber)
				.setParameter("director", director)
				.setFirstResult(fromIndex)
				.setMaxResults(limit)
				.getResultList();
	}

	
	public Long countArticles(String title, String genre, String publisher, LocalDate yearEdition, String author, Integer issueNumber, String director){
		return this.em.createQuery(
				"SELECT COUNT(a) FROM Article a WHERE "
				+ "(:title is null or a.title = :title) and "
				+ "(:genre is null or a.genre = :genre) and"
				+ "(:publisher is null or a.publisher = :publisher) and"
				+ "(:yearEdition is null or a.yearEdition = :yearEdition) and"
				+ "(:author is null or a.author = :author) and"
				+ "(:issueNumber is null or a.issueNumber = :issueNumber) and"
				+ "(:director is null or a.director = :director)", Long.class)
				.setParameter("title", title)
				.setParameter("genre", genre)
				.setParameter("publisher", publisher)
				.setParameter("yearEdition", yearEdition)
				.setParameter("author", author)
				.setParameter("issueNumber", issueNumber)
				.setParameter("director", director)
				.getSingleResult();
	}

}
