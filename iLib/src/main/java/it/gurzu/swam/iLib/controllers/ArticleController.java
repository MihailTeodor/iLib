package it.gurzu.swam.iLib.controllers;

import java.sql.Date;
import java.util.Collections;
import java.util.List;

import it.gurzu.swam.iLib.dao.ArticleDao;
import it.gurzu.swam.iLib.dao.BookDao;
import it.gurzu.swam.iLib.dao.BookingDao;
import it.gurzu.swam.iLib.dao.LoanDao;
import it.gurzu.swam.iLib.dao.MagazineDao;
import it.gurzu.swam.iLib.dao.MovieDVDDao;
import it.gurzu.swam.iLib.exceptions.ArticleDoesNotExistException;
import it.gurzu.swam.iLib.exceptions.InvalidOperationException;
import it.gurzu.swam.iLib.exceptions.InvalidStateTransitionException;
import it.gurzu.swam.iLib.exceptions.SearchHasGivenNoResultsException;
import it.gurzu.swam.iLib.model.Article;
import it.gurzu.swam.iLib.model.Book;
import it.gurzu.swam.iLib.model.Booking;
import it.gurzu.swam.iLib.model.BookingState;
import it.gurzu.swam.iLib.model.Loan;
import it.gurzu.swam.iLib.model.Magazine;
import it.gurzu.swam.iLib.model.ModelFactory;
import it.gurzu.swam.iLib.model.MovieDVD;
import it.gurzu.swam.iLib.model.ArticleState;
import jakarta.inject.Inject;

public class ArticleController {

	@Inject
	private ArticleDao articleDao;

	@Inject
	private BookDao bookDao;

	@Inject
	private MagazineDao magazineDao;

	@Inject
	private MovieDVDDao movieDVDDao;

	@Inject
	private BookingDao bookingDao;

	@Inject
	private LoanDao loanDao;

	public void addArticle(String isbn, String issn, String isan, String title, String genre, String description,
			String publisher, Date yearEdition, String location, String author, int issueNumber, String director) {
		Article articleToAdd = null;
		if (isbn != null) {
			articleToAdd = ModelFactory.book();
			((Book) articleToAdd).setAuthor(author);
		} else if (issn != null) {
			articleToAdd = ModelFactory.magazine();
			((Magazine) articleToAdd).setIssueNumber(issueNumber);
		} else if (isan != null) {
			articleToAdd = ModelFactory.movieDVD();
			((MovieDVD) articleToAdd).setDirector(director);
		} else
			throw new IllegalArgumentException("Unique article identifier not inserted! Please insert one of isbn, issn, isan!");
			
		articleToAdd.setTitle(title);
		articleToAdd.setGenre(genre);
		articleToAdd.setDescription(description);
		articleToAdd.setPublisher(publisher);
		articleToAdd.setYearEdition(yearEdition);
		articleToAdd.setLocation(location);
		articleToAdd.setState(ArticleState.AVAILABLE);

		articleDao.save(articleToAdd);
	}

	public void updateArticle(Long articleId, String isbn, String issn, String isan, String title, String genre,
			String description, String publisher, Date yearEdition, String location, String author, int issueNumber,
			String director, ArticleState state) {
		Article articleToUpdate = articleDao.findById(articleId);

		if (articleToUpdate == null)
			throw new ArticleDoesNotExistException("Article does not exist!");

		if (isbn != null) {
			((Book) articleToUpdate).setIsbn(isbn);
			((Book) articleToUpdate).setAuthor(author);
		}
		if (issn != null) {
			((Magazine) articleToUpdate).setIssn(issn);
			((Magazine) articleToUpdate).setIssueNumber(issueNumber);
		}
		if (isan != null) {
			((MovieDVD) articleToUpdate).setIsan(isan);
			((MovieDVD) articleToUpdate).setDirector(director);
		}

		articleToUpdate.setTitle(title);
		articleToUpdate.setGenre(genre);
		articleToUpdate.setDescription(description);
		articleToUpdate.setPublisher(publisher);
		articleToUpdate.setYearEdition(yearEdition);
		articleToUpdate.setLocation(location);
		// To change the state to/from BOOKED or ON LOAN the Admnistrator must register
		// a booking/unbooking or a lona/return.
		if ((articleToUpdate.getState() == ArticleState.UNAVAILABLE && state == ArticleState.AVAILABLE)
				|| (articleToUpdate.getState() == ArticleState.AVAILABLE && state == ArticleState.UNAVAILABLE)) {
			articleToUpdate.setState(state);
		} else
			throw new InvalidStateTransitionException("Cannot change state to inserted value!");

		articleDao.save(articleToUpdate);
	}

	public Article getArticleInfo(Long articleId) {
		Article article = articleDao.findById(articleId);

		if (article == null)
			throw new ArticleDoesNotExistException("Article does not exist!");

		List<Loan> loans = null;
		switch(article.getState()) {
		case BOOKED:
			List<Booking> bookings = bookingDao.searchBookings(null, article);
			bookings.get(0).validateState();
			break;
		case ONLOAN:
			loans = loanDao.searchLoans(null, article);
			loans.get(0).validateState();
			break;
		case ONLOANBOOKED:
			loans = loanDao.searchLoans(null, article);
			loans.get(0).validateState();
			break;
		default:
			break;
		}
		
		return article;
	}

	public List<? extends Article> searchArticles(String isbn, String issn, String isan, String title, String genre,
			String publisher, Date yearEdition, String author, int issueNumber, String director) {
		List<? extends Article> retrievedArticles = Collections.emptyList();
		if (isbn != null) {
			retrievedArticles = bookDao.findBooksByIsbn(isbn);
		} else if (issn != null) {
			retrievedArticles = magazineDao.findMagazinesByIssn(issn);
		} else if (isan != null) {
			retrievedArticles = movieDVDDao.findMoviesByIsan(isan);
		} else
			retrievedArticles = articleDao.findArticles(title, genre, publisher, yearEdition, author, issueNumber,
					director);

		if (retrievedArticles.isEmpty())
			throw new SearchHasGivenNoResultsException("The search has given 0 results!");

		return retrievedArticles;
	}

	public void removeArticle(Long articleId) {
		Article articleToRemove = articleDao.findById(articleId);

		if (articleToRemove == null)
			throw new ArticleDoesNotExistException("Cannot remove Article! Article not in catalogue!");

		switch (articleToRemove.getState()) {
		case ONLOAN:
			throw new InvalidOperationException("Cannot remove Article from catalogue! Article currently on loan!");
		case ONLOANBOOKED:
			throw new InvalidOperationException("Cannot remove Article from catalogue! Article currently on loan!");
		case BOOKED:
			List<Booking> retrievedBookings = bookingDao.searchBookings(null, articleToRemove);
			if (retrievedBookings.get(0).getState() != BookingState.ACTIVE)
				throw new InvalidOperationException("Cannot remove Article from catalogue! Inconsistent state!");
			else
				retrievedBookings.get(0).setState(BookingState.CANCELLED);
				break;
		default:
			break;
		}

		articleDao.delete(articleToRemove);
	}
}