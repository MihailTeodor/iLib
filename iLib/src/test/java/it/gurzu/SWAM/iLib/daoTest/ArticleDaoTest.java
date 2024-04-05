package it.gurzu.SWAM.iLib.daoTest;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import it.gurzu.swam.iLib.dao.ArticleDao;
import it.gurzu.swam.iLib.model.Article;
import it.gurzu.swam.iLib.model.ModelFactory;
import it.gurzu.swam.iLib.model.MovieDVD;

public class ArticleDaoTest extends JPATest {

	private ArticleDao articleDao;
	private Article article;
	
	@Override
	protected void init() throws IllegalAccessException {
		article = ModelFactory.book();
		article.setTitle("Cujo");
		article.setGenre("horror");
		
		em.persist(article);
		
		articleDao = new ArticleDao();
		FieldUtils.writeField(articleDao, "em", em, true); 
	}
	
	@Test
	public void testFindArticles() {
		MovieDVD articleToAdd = ModelFactory.movieDVD();
		articleToAdd.setTitle("Cujo");
		articleToAdd.setGenre("horror");
		articleToAdd.setDirector("Teague");
		em.persist(articleToAdd);
		
		List<Article> retrievedArticles = articleDao.findArticles("Cujo", "inexistingGenre", null, null, null, null, null);
		Assertions.assertEquals(true, retrievedArticles.isEmpty());
		
		retrievedArticles = articleDao.findArticles("Cujo", "horror", null, null, null, null, "Teague");
		Assertions.assertEquals(1, retrievedArticles.size());
		Assertions.assertEquals(true, retrievedArticles.contains(articleToAdd));
		
		retrievedArticles = articleDao.findArticles("Cujo", "horror", null, null, null, null, null);
		List<Article> targetArticleList = new ArrayList<Article>();
		targetArticleList.add(article);
		targetArticleList.add(articleToAdd);
		Assertions.assertEquals(true, retrievedArticles.containsAll(targetArticleList));
	}
}
