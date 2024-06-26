package it.gurzu.swam.iLib.controllers;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import it.gurzu.swam.iLib.dao.ArticleDao;
import it.gurzu.swam.iLib.dao.BookDao;
import it.gurzu.swam.iLib.dao.BookingDao;
import it.gurzu.swam.iLib.dao.LoanDao;
import it.gurzu.swam.iLib.dao.MagazineDao;
import it.gurzu.swam.iLib.dao.MovieDVDDao;
import it.gurzu.swam.iLib.dto.ArticleDTO;
import it.gurzu.swam.iLib.dto.ArticleMapper;
import it.gurzu.swam.iLib.dto.ArticleType;
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
import it.gurzu.swam.iLib.model.MovieDVD;
import it.gurzu.swam.iLib.model.ArticleState;
import jakarta.enterprise.inject.Model;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@Model
@Transactional
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

	public Long addArticle(ArticleDTO articleDTO) {
		ArticleMapper mapper = new ArticleMapper();
		Article articleToAdd = mapper.toEntity(articleDTO);

		articleToAdd.setState(ArticleState.AVAILABLE);
		articleDao.save(articleToAdd);

		return articleToAdd.getId();
	}

	public void updateArticle(Long id, ArticleDTO articleDTO) {
		Article articleToUpdate = articleDao.findById(id);

		if (articleToUpdate == null)
			throw new ArticleDoesNotExistException("Article does not exist!");

		if (articleDTO.getType() == ArticleType.BOOK) {
			if (!(articleToUpdate instanceof Book))
				throw new IllegalArgumentException("Cannot change type of Article");
			if (articleDTO.getIsbn() == null)
				throw new IllegalArgumentException("Article identifier is required");
			if (articleDTO.getAuthor() == null)
				throw new IllegalArgumentException("Author is required!");

			((Book) articleToUpdate).setIsbn(articleDTO.getIsbn());
			((Book) articleToUpdate).setAuthor(articleDTO.getAuthor());
		}

		if (articleDTO.getType() == ArticleType.MAGAZINE) {
			if (!(articleToUpdate instanceof Magazine))
				throw new IllegalArgumentException("Cannot change type of Article");
			if (articleDTO.getIssn() == null)
				throw new IllegalArgumentException("Article identifier is required");
			if (articleDTO.getIssueNumber() == null)
				throw new IllegalArgumentException("Issue number is required!");

			((Magazine) articleToUpdate).setIssn(articleDTO.getIssn());
			((Magazine) articleToUpdate).setIssueNumber(articleDTO.getIssueNumber());
		}

		if (articleDTO.getType() == ArticleType.MOVIEDVD) {
			if (!(articleToUpdate instanceof MovieDVD))
				throw new IllegalArgumentException("Cannot change type of Article");
			if (articleDTO.getIsan() == null)
				throw new IllegalArgumentException("Article identifier is required");
			if (articleDTO.getDirector() == null)
				throw new IllegalArgumentException("Director is required!");

			((MovieDVD) articleToUpdate).setIsan(articleDTO.getIsan());
			((MovieDVD) articleToUpdate).setDirector(articleDTO.getDirector());
		}

		articleToUpdate.setTitle(articleDTO.getTitle());
		articleToUpdate.setGenre(articleDTO.getGenre());
		articleToUpdate.setDescription(articleDTO.getDescription());
		articleToUpdate.setPublisher(articleDTO.getPublisher());
		articleToUpdate.setYearEdition(articleDTO.getYearEdition());
		articleToUpdate.setLocation(articleDTO.getLocation());
		// To change the state to/from BOOKED or ON LOAN the Admnistrator must register
		// a booking/unbooking or a lona/return.
		if ((articleToUpdate.getState() == ArticleState.UNAVAILABLE && articleDTO.getState() == ArticleState.AVAILABLE)
				|| (articleToUpdate.getState() == ArticleState.AVAILABLE
						&& articleDTO.getState() == ArticleState.UNAVAILABLE)) {
			articleToUpdate.setState(articleDTO.getState());
		} else if (articleDTO.getState() != null && articleToUpdate.getState() != articleDTO.getState())
			throw new InvalidStateTransitionException("Cannot change state to inserted value!");

		articleDao.save(articleToUpdate);
	}

	public List<? extends Article> searchArticles(String isbn, String issn, String isan, String title, String genre,
			String publisher, LocalDate yearEdition, String author, Integer issueNumber, String director, int fromIndex,
			int limit) {
		List<? extends Article> retrievedArticles = Collections.emptyList();
		if (isbn != null) {
			retrievedArticles = bookDao.findBooksByIsbn(isbn);
		} else if (issn != null) {
			retrievedArticles = magazineDao.findMagazinesByIssn(issn);
		} else if (isan != null) {
			retrievedArticles = movieDVDDao.findMoviesByIsan(isan);
		} else
			retrievedArticles = articleDao.findArticles(title, genre, publisher, yearEdition, author, issueNumber,
					director, fromIndex, limit);

		if (retrievedArticles.isEmpty())
			throw new SearchHasGivenNoResultsException("The search has given 0 results!");

		return retrievedArticles;
	}

	public Long countArticles(String isbn, String issn, String isan, String title, String genre, String publisher,
			LocalDate yearEdition, String author, Integer issueNumber, String director) {
		if (isbn != null) {
			return bookDao.countBooksByIsbn(isbn);
		} else if (issn != null) {
			return magazineDao.countMagazinesByIssn(issn);
		} else if (isan != null) {
			return movieDVDDao.countMoviesByIsan(isan);
		} else {
			return articleDao.countArticles(title, genre, publisher, yearEdition, author, issueNumber, director);
		}
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
			List<Booking> retrievedBookings = bookingDao.searchBookings(null, articleToRemove, 0, 1);
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

	public ArticleDTO getArticleInfoExtended(Long articleId) {
		Article article = articleDao.findById(articleId);

		if (article == null)
			throw new ArticleDoesNotExistException("Article does not exist!");

		List<Loan> loans = null;
		LocalDate loanDueDate = null;
		LocalDate bookingEndDate = null;

		switch (article.getState()) {
		case BOOKED:
			List<Booking> bookings = bookingDao.searchBookings(null, article, 0, 1);
			bookings.get(0).validateState();
			bookingEndDate = bookings.get(0).getBookingEndDate();
			break;
		case ONLOAN:
			loans = loanDao.searchLoans(null, article, 0, 1);
			loans.get(0).validateState();
			loanDueDate = loans.get(0).getDueDate();
			break;
		case ONLOANBOOKED:
			loans = loanDao.searchLoans(null, article, 0, 1);
			loans.get(0).validateState();
			loanDueDate = loans.get(0).getDueDate();
			break;
		default:
			break;
		}

		return ArticleMapper.toDTO(article, loanDueDate, bookingEndDate);
	}
}