package it.gurzu.SWAM.iLib.daoTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;

import it.gurzu.swam.iLib.dao.ArticleDao;
import it.gurzu.swam.iLib.model.Article;
import it.gurzu.swam.iLib.model.Book;
import it.gurzu.swam.iLib.model.Magazine;
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
		article.setYearEdition(LocalDate.of(2024, 01, 01));
		
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
		
		List<Article> retrievedArticles = articleDao.findArticles("Cujo", "inexistingGenre", null, null, null, null, null, 0, 10);
		assertEquals(true, retrievedArticles.isEmpty());
		
		retrievedArticles = articleDao.findArticles("Cujo", "horror", null, null, null, null, "Teague", 0, 10);
		assertEquals(1, retrievedArticles.size());
		assertEquals(true, retrievedArticles.contains(articleToAdd));
		
		retrievedArticles = articleDao.findArticles("Cujo", "horror", null, null, null, null, null, 0, 10);
		List<Article> targetArticleList = new ArrayList<Article>();
		targetArticleList.add(article);
		targetArticleList.add(articleToAdd);
		assertEquals(true, retrievedArticles.containsAll(targetArticleList));
	}
	
	@Test
	public void testfindArticlesPaginationAndOrder() {
		Book book = ModelFactory.book();
		book.setYearEdition(LocalDate.of(2023, 01, 01));
		em.persist(book);
		
		Magazine magazine = ModelFactory.magazine();
		magazine.setYearEdition(LocalDate.of(2022, 01, 01));
		em.persist(magazine);
		
		List<Article> retrievedArticlesFirstPage = articleDao.findArticles(null, null, null, null, null, null, null, 0, 2);
		List<Article> retrievedArticlesSecondPage = articleDao.findArticles(null, null, null, null, null, null, null, 2, 1);
		
		List<Article> targetArticleList = new ArrayList<Article>();
		targetArticleList.add(article);
		targetArticleList.add(book);
		
		assertEquals(2, retrievedArticlesFirstPage.size());
		assertEquals(targetArticleList.get(0), retrievedArticlesFirstPage.get(0));
		assertEquals(targetArticleList.get(1), retrievedArticlesFirstPage.get(1));
		
		assertEquals(1, retrievedArticlesSecondPage.size());
		assertEquals(true, retrievedArticlesSecondPage.contains(magazine));
	}
	
	@Test
	public void testCountArticles() {
		Book book = ModelFactory.book();
		book.setYearEdition(LocalDate.of(2023, 01, 01));
		em.persist(book);
		
		Magazine magazine = ModelFactory.magazine();
		magazine.setYearEdition(LocalDate.of(2022, 01, 01));
		em.persist(magazine);

		Long resultsNumber = articleDao.countArticles(null, null, null, null, null, null, null);
		
		assertEquals(3, resultsNumber);
	}
		
}
