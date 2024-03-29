package it.gurzu.SWAM.iLib.DaoTest;

import java.sql.Date;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import it.gurzu.swam.iLib.dao.ArticleDao;
import it.gurzu.swam.iLib.model.Article;
import it.gurzu.swam.iLib.model.ModelFactory;

public class ArticleDaoTest extends JPATest {

	private ArticleDao articleDao;
	private Article article;
	
	@Override
	protected void init() throws IllegalAccessException {
		article = ModelFactory.book();
		article.setTitle("Cujo");
		article.setGenre("horror");
		article.setPublisher("publisher");
		article.setYearEdition(Date.valueOf("2024-01-01"));
		
		em.persist(article);
		
		articleDao = new ArticleDao();
		FieldUtils.writeField(articleDao, "em", em, true); 
	}

	@Test 
	public void testFindArticlesByTitle() {
		List<Article> retrievedArticles = articleDao.findArticlesByTitle("Cujo");
		Assertions.assertEquals(1, retrievedArticles.size());
		Assertions.assertEquals(true, retrievedArticles.contains(article));
	}
	
	@Test 
	public void testFindArticlesByGenre() {
		List<Article> retrievedArticles = articleDao.findArticlesByGenre("horror");
		Assertions.assertEquals(1, retrievedArticles.size());
		Assertions.assertEquals(true, retrievedArticles.contains(article));
	}

	@Test 
	public void testFindArticlesByPublisher() {
		List<Article> retrievedArticles = articleDao.findArticlesByPublisher("publisher");
		Assertions.assertEquals(1, retrievedArticles.size());
		Assertions.assertEquals(true, retrievedArticles.contains(article));
	}

	@Test 
	public void testFindArticlesByYearEdition() {
		List<Article> retrievedArticles = articleDao.findArticlesByYearEdition("2024-01-01");
		Assertions.assertEquals(1, retrievedArticles.size());
		Assertions.assertEquals(true, retrievedArticles.contains(article));
	}

	
	
	
}
