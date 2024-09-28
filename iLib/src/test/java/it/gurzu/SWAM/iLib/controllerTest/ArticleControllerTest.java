package it.gurzu.SWAM.iLib.controllerTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import it.gurzu.swam.iLib.controllers.ArticleController;
import it.gurzu.swam.iLib.dao.ArticleDao;
import it.gurzu.swam.iLib.dao.BookDao;
import it.gurzu.swam.iLib.dao.BookingDao;
import it.gurzu.swam.iLib.dao.LoanDao;
import it.gurzu.swam.iLib.dao.MagazineDao;
import it.gurzu.swam.iLib.dao.MovieDVDDao;
import it.gurzu.swam.iLib.dto.ArticleDTO;
import it.gurzu.swam.iLib.dto.ArticleType;
import it.gurzu.swam.iLib.exceptions.ArticleDoesNotExistException;
import it.gurzu.swam.iLib.exceptions.InvalidOperationException;
import it.gurzu.swam.iLib.exceptions.InvalidStateTransitionException;
import it.gurzu.swam.iLib.exceptions.SearchHasGivenNoResultsException;
import it.gurzu.swam.iLib.model.Article;
import it.gurzu.swam.iLib.model.ArticleState;
import it.gurzu.swam.iLib.model.Book;
import it.gurzu.swam.iLib.model.Booking;
import it.gurzu.swam.iLib.model.BookingState;
import it.gurzu.swam.iLib.model.Loan;
import it.gurzu.swam.iLib.model.Magazine;
import it.gurzu.swam.iLib.model.ModelFactory;
import it.gurzu.swam.iLib.model.MovieDVD;
import it.gurzu.swam.iLib.model.User;

public class ArticleControllerTest {

	private ArticleController articleController;
	private ArticleDao articleDaoMock;
	private BookDao bookDaoMock;
	private MagazineDao magazineDaoMock;
	private MovieDVDDao movieDVDDaoMock;
	private BookingDao bookingDaoMock;
	private LoanDao loanDaoMock;

	@BeforeEach
	public void setup() throws IllegalAccessException {
		articleController = new ArticleController();
		articleDaoMock = mock(ArticleDao.class);
		bookDaoMock = mock(BookDao.class);
		magazineDaoMock = mock(MagazineDao.class);
		movieDVDDaoMock = mock(MovieDVDDao.class);
		bookingDaoMock = mock(BookingDao.class);
		loanDaoMock = mock(LoanDao.class);

		FieldUtils.writeField(articleController, "articleDao", articleDaoMock, true);
		FieldUtils.writeField(articleController, "bookDao", bookDaoMock, true);
		FieldUtils.writeField(articleController, "magazineDao", magazineDaoMock, true);
		FieldUtils.writeField(articleController, "movieDVDDao", movieDVDDaoMock, true);
		FieldUtils.writeField(articleController, "bookingDao", bookingDaoMock, true);
		FieldUtils.writeField(articleController, "loanDao", loanDaoMock, true);
	}

	@Test
	public void testAddArticle() {
		ArticleDTO articleDTO = new ArticleDTO();
		articleDTO.setType(ArticleType.BOOK);
		articleDTO.setIsbn("isbn");
		articleDTO.setAuthor("author");
		articleDTO.setDescription("description");
		articleDTO.setGenre("genre");
		articleDTO.setLocation("location");
		articleDTO.setPublisher("publisher");
		articleDTO.setTitle("title");
		articleDTO.setYearEdition(LocalDate.now());

		articleController.addArticle(articleDTO);

		ArgumentCaptor<Article> articleCaptor = ArgumentCaptor.forClass(Article.class);
		verify(articleDaoMock).save(articleCaptor.capture());

		Article savedArticle = articleCaptor.getValue();

		assertTrue(savedArticle instanceof Book);
		assertEquals(articleDTO.getIsbn(), ((Book) savedArticle).getIsbn());
		assertEquals(articleDTO.getAuthor(), ((Book) savedArticle).getAuthor());
		assertEquals(articleDTO.getDescription(), savedArticle.getDescription());
		assertEquals(articleDTO.getGenre(), savedArticle.getGenre());
		assertEquals(articleDTO.getLocation(), savedArticle.getLocation());
		assertEquals(articleDTO.getPublisher(), savedArticle.getPublisher());
		assertEquals(articleDTO.getTitle(), savedArticle.getTitle());
		assertEquals(articleDTO.getYearEdition(), savedArticle.getYearEdition());
	}

	@Test
	public void testUpdateArticle_WhenArticleToUpdateDoesNotExist_ThrowsArticleDoesNotExistException() {
		ArticleDTO articleDTO = new ArticleDTO();
		when(articleDaoMock.findById(1L)).thenReturn(null);
		Exception thrownException = assertThrows(ArticleDoesNotExistException.class, () -> {
			articleController.updateArticle(1L, articleDTO);

		});
		assertEquals("Article does not exist!", thrownException.getMessage());
	}

	@Test
	public void testUpdateArticle_Book_TypeMismatch_ThrowsIllegalArgumentException() {
		ArticleDTO articleDTO = new ArticleDTO();
		articleDTO.setType(ArticleType.BOOK);
		when(articleDaoMock.findById(1L)).thenReturn(ModelFactory.magazine());

		Exception thrownException = assertThrows(IllegalArgumentException.class, () -> {
			articleController.updateArticle(1L, articleDTO);
		});
		assertEquals("Cannot change type of Article", thrownException.getMessage());
	}

	@Test
	public void testUpdateArticle_Book_WhenIsbnIsNull_ThrowsIllegalArgumentException() {
		ArticleDTO articleDTO = new ArticleDTO();
		articleDTO.setType(ArticleType.BOOK);
		when(articleDaoMock.findById(1L)).thenReturn(ModelFactory.book());

		Exception thrownException = assertThrows(IllegalArgumentException.class, () -> {
			articleController.updateArticle(1L, articleDTO);
		});
		assertEquals("Article identifier is required", thrownException.getMessage());
	}

	@Test
	public void testUpdateArticle_Book_WhenAuthorIsNull_ThrowsIllegalArgumentException() {
		ArticleDTO articleDTO = new ArticleDTO();
		articleDTO.setType(ArticleType.BOOK);
		articleDTO.setIsbn("isbn");

		when(articleDaoMock.findById(1L)).thenReturn(ModelFactory.book());

		Exception thrownException = assertThrows(IllegalArgumentException.class, () -> {
			articleController.updateArticle(1L, articleDTO);
		});
		assertEquals("Author is required!", thrownException.getMessage());
	}

	@Test
	public void testUpdateArticle_Magazine_TypeMismatch_ThrowsIllegalArgumentException() {
		ArticleDTO articleDTO = new ArticleDTO();
		articleDTO.setType(ArticleType.MAGAZINE);
		when(articleDaoMock.findById(1L)).thenReturn(ModelFactory.book());

		Exception thrownException = assertThrows(IllegalArgumentException.class, () -> {
			articleController.updateArticle(1L, articleDTO);
		});
		assertEquals("Cannot change type of Article", thrownException.getMessage());
	}

	@Test
	public void testUpdateArticle_Magazine_WhenIssnIsNull_ThrowsIllegalArgumentException() {
		ArticleDTO articleDTO = new ArticleDTO();
		articleDTO.setType(ArticleType.MAGAZINE);
		when(articleDaoMock.findById(1L)).thenReturn(ModelFactory.magazine());

		Exception thrownException = assertThrows(IllegalArgumentException.class, () -> {
			articleController.updateArticle(1L, articleDTO);
		});
		assertEquals("Article identifier is required", thrownException.getMessage());
	}

	@Test
	public void testUpdateArticle_Magazine_WhenIssueNumberIsNull_ThrowsIllegalArgumentException() {
		ArticleDTO articleDTO = new ArticleDTO();
		articleDTO.setType(ArticleType.MAGAZINE);
		articleDTO.setIssn("issn");

		when(articleDaoMock.findById(1L)).thenReturn(ModelFactory.magazine());

		Exception thrownException = assertThrows(IllegalArgumentException.class, () -> {
			articleController.updateArticle(1L, articleDTO);
		});
		assertEquals("Issue number is required!", thrownException.getMessage());
	}

	@Test
	public void testUpdateArticle_MovieDVD_TypeMismatch_ThrowsIllegalArgumentException() {
		ArticleDTO articleDTO = new ArticleDTO();
		articleDTO.setType(ArticleType.MOVIEDVD);
		when(articleDaoMock.findById(1L)).thenReturn(ModelFactory.magazine());

		Exception thrownException = assertThrows(IllegalArgumentException.class, () -> {
			articleController.updateArticle(1L, articleDTO);
		});
		assertEquals("Cannot change type of Article", thrownException.getMessage());
	}

	@Test
	public void testUpdateArticle_MovieDVD_WhenIssnIsNull_ThrowsIllegalArgumentException() {
		ArticleDTO articleDTO = new ArticleDTO();
		articleDTO.setType(ArticleType.MOVIEDVD);
		when(articleDaoMock.findById(1L)).thenReturn(ModelFactory.movieDVD());

		Exception thrownException = assertThrows(IllegalArgumentException.class, () -> {
			articleController.updateArticle(1L, articleDTO);
		});
		assertEquals("Article identifier is required", thrownException.getMessage());
	}

	@Test
	public void testUpdateArticle_MovieDVD_WhenIssueNumberIsNull_ThrowsIllegalArgumentException() {
		ArticleDTO articleDTO = new ArticleDTO();
		articleDTO.setType(ArticleType.MOVIEDVD);
		articleDTO.setIsan("isan");

		when(articleDaoMock.findById(1L)).thenReturn(ModelFactory.movieDVD());

		Exception thrownException = assertThrows(IllegalArgumentException.class, () -> {
			articleController.updateArticle(1L, articleDTO);
		});
		assertEquals("Director is required!", thrownException.getMessage());
	}

	@Test
	public void testUpdateArticle_WhenArticleToUpdateExistsAndIsbnNotNull_UpdatesBookAttributes() {
		ArticleDTO articleDTO = new ArticleDTO();
		articleDTO.setType(ArticleType.BOOK);
		articleDTO.setIsbn("isbn");
		articleDTO.setAuthor("author");

		Book mockArticle = mock(Book.class);
		when(articleDaoMock.findById(1L)).thenReturn(mockArticle);

		articleController.updateArticle(1L, articleDTO);

		verify(mockArticle).setIsbn(articleDTO.getIsbn());
	}

	@Test
	public void testUpdateArticle_WhenArticleToUpdateExistsAndIssnNotNull_UpdatesMagazineAttributes() {
		ArticleDTO articleDTO = new ArticleDTO();
		articleDTO.setType(ArticleType.MAGAZINE);
		articleDTO.setIssn("issn");
		articleDTO.setIssueNumber(Integer.valueOf(1));

		Magazine mockArticle = mock(Magazine.class);
		when(articleDaoMock.findById(1L)).thenReturn(mockArticle);

		articleController.updateArticle(1L, articleDTO);

		verify(mockArticle).setIssn(articleDTO.getIssn());
	}

	@Test
	public void testUpdateArticle_WhenArticleToUpdateExistsAndIsanNotNull_UpdatesMovieDVDAttributes() {
		ArticleDTO articleDTO = new ArticleDTO();
		articleDTO.setType(ArticleType.MOVIEDVD);
		articleDTO.setIsan("isan");
		articleDTO.setDirector("director");

		MovieDVD mockArticle = mock(MovieDVD.class);
		when(articleDaoMock.findById(1L)).thenReturn(mockArticle);

		articleController.updateArticle(1L, articleDTO);

		verify(mockArticle).setIsan(articleDTO.getIsan());
	}

	@ParameterizedTest
	@MethodSource("testUpdateArticleArgumentsProvider")
	public void testUpdateArticle_StateTransitions_HandledCorrectly(ArticleState initialState, ArticleState newState,
			boolean shouldSucceed) {
		ArticleDTO articleDTO = new ArticleDTO();
		articleDTO.setState(newState);

		Article mockArticle = mock(Article.class);
		when(mockArticle.getState()).thenReturn(initialState);
		when(articleDaoMock.findById(1L)).thenReturn(mockArticle);

		try {
			articleController.updateArticle(1l, articleDTO);
			if (!shouldSucceed && newState != null) {
				fail("Expected InvalidStateTransitionException but none was thrown!");
			}
		} catch (InvalidStateTransitionException e) {
			if (shouldSucceed) {
				fail("Dod not expected InvalidStateTransitionException to be thrown!");
			}
			assertTrue(e instanceof InvalidStateTransitionException, "Cannot change state to inserted value!");
		}

		if (shouldSucceed) {
			if (initialState != newState)
				verify(mockArticle).setState(newState);
			verify(articleDaoMock).save(mockArticle);
		} else {
			verify(mockArticle, never()).setState(any(ArticleState.class));
		}
	}

	@Test
    public void testGetArticleInfoExtended_WhenArticleDoesNotExist_ThrowsArticleDoesNotExistException() {
    	when(articleDaoMock.findById(1L)).thenReturn(null);
    	Exception thrownException = assertThrows(ArticleDoesNotExistException.class, ()->{
			articleController.getArticleInfoExtended(1L);
		});
		assertEquals("Article does not exist!", thrownException.getMessage());	
    }

	@ParameterizedTest
	@MethodSource("testGetArticleInfoArgumentsProvider")
	public void testGetArticleInfoExtended_WhenArticleExists_CallsValidateState(ArticleState state) throws Exception {
		Article mockArticle = mock(Article.class);
		User mockUser = mock(User.class);
		when(mockArticle.getId()).thenReturn(1L);
		when(mockUser.getId()).thenReturn(1L);
		when(mockArticle.getState()).thenReturn(state);
		when(articleDaoMock.findById(1l)).thenReturn(mockArticle);

		Booking mockBooking = null;
		Loan mockLoan = null;

		if (state.equals(ArticleState.BOOKED)) {
			mockBooking = mock(Booking.class);
			when(mockBooking.getBookedArticle()).thenReturn(mockArticle);
			when(mockBooking.getBookingUser()).thenReturn(mockUser);
			when(bookingDaoMock.searchBookings(null, mockArticle, 0, 1)).thenReturn(Arrays.asList(mockBooking));
		} else if (state.equals(ArticleState.ONLOAN)) {
			mockLoan = mock(Loan.class);
			when(mockLoan.getArticleOnLoan()).thenReturn(mockArticle);
			when(mockLoan.getLoaningUser()).thenReturn(mockUser);
			when(loanDaoMock.searchLoans(null, mockArticle, 0, 1)).thenReturn(Arrays.asList(mockLoan));
		} else if (state.equals(ArticleState.ONLOANBOOKED)) {
			mockBooking = mock(Booking.class);
			when(mockBooking.getBookedArticle()).thenReturn(mockArticle);
			when(mockBooking.getBookingUser()).thenReturn(mockUser);
			when(bookingDaoMock.searchBookings(null, mockArticle, 0, 1)).thenReturn(Arrays.asList(mockBooking));

			mockLoan = mock(Loan.class);
			when(mockLoan.getArticleOnLoan()).thenReturn(mockArticle);
			when(mockLoan.getLoaningUser()).thenReturn(mockUser);
			when(loanDaoMock.searchLoans(null, mockArticle, 0, 1)).thenReturn(Arrays.asList(mockLoan));
		}

		articleController.getArticleInfoExtended(1L);

		if (state.equals(ArticleState.BOOKED)) {
			verify(bookingDaoMock).searchBookings(null, mockArticle, 0, 1);
			verify(mockBooking).validateState();
		} else if (state.equals(ArticleState.ONLOAN)) {
			verify(loanDaoMock).searchLoans(null, mockArticle, 0, 1);
			verify(mockLoan).validateState();
		} else if (state.equals(ArticleState.ONLOANBOOKED)) {
			verify(bookingDaoMock).searchBookings(null, mockArticle, 0, 1);
			verify(mockBooking).validateState();

			verify(loanDaoMock).searchLoans(null, mockArticle, 0, 1);
			verify(mockLoan).validateState();
		}
	}

	@ParameterizedTest
	@EnumSource(value = ArticleState.class, names = { "BOOKED", "ONLOAN", "ONLOANBOOKED" })
	public void testGetArticleInfoExtended_WhenArticleExists_ReturnsArticleDTO(ArticleState state) {
		Article mockArticle = mock(Article.class);
		User mockUser = mock(User.class);
		Booking mockBooking = mock(Booking.class);
		Loan mockLoan = mock(Loan.class);
		LocalDate today = LocalDate.now();

		when(mockArticle.getId()).thenReturn(1L);
		when(mockArticle.getTitle()).thenReturn("Cujo");
		when(mockArticle.getLocation()).thenReturn("upstairs");
		when(mockArticle.getYearEdition()).thenReturn(today.minusYears(1));
		when(mockArticle.getPublisher()).thenReturn("publisher");
		when(mockArticle.getGenre()).thenReturn("horror");
		when(mockArticle.getDescription()).thenReturn("description");
		when(mockArticle.getState()).thenReturn(state);

		when(mockBooking.getBookedArticle()).thenReturn(mockArticle);
		when(mockBooking.getBookingUser()).thenReturn(mockUser);
		when(bookingDaoMock.searchBookings(null, mockArticle, 0, 1)).thenReturn(Collections.singletonList(mockBooking));

		when(mockLoan.getArticleOnLoan()).thenReturn(mockArticle);
		when(mockLoan.getLoaningUser()).thenReturn(mockUser);
		when(loanDaoMock.searchLoans(null, mockArticle, 0, 1)).thenReturn(Collections.singletonList(mockLoan));

		when(articleDaoMock.findById(1L)).thenReturn(mockArticle);

		ArticleDTO resultDTO = articleController.getArticleInfoExtended(1L);

		assertNotNull(resultDTO);
		assertEquals(Long.valueOf(1L), resultDTO.getId());
		assertEquals("Cujo", resultDTO.getTitle());
		assertEquals("upstairs", resultDTO.getLocation());
		assertEquals(today.minusYears(1), resultDTO.getYearEdition());
		assertEquals("publisher", resultDTO.getPublisher());
		assertEquals("horror", resultDTO.getGenre());
		assertEquals("description", resultDTO.getDescription());
		assertEquals(state, resultDTO.getState());

		if (state.equals(ArticleState.ONLOAN) || state.equals(ArticleState.ONLOANBOOKED)) {
			assertEquals(mockLoan.getArticleOnLoan().getId(), resultDTO.getLoanDTO().getArticleId());
			assertEquals(mockLoan.getLoaningUser().getId(), resultDTO.getLoanDTO().getLoaningUserId());
		}

		if (state.equals(ArticleState.BOOKED) || state.equals(ArticleState.ONLOANBOOKED)) {
			assertEquals(mockBooking.getBookedArticle().getId(), resultDTO.getBookingDTO().getBookedArticleId());
			assertEquals(mockBooking.getBookingUser().getId(), resultDTO.getBookingDTO().getBookingUserId());
		}
	}

	@Test
    void testSearchArticles_WhenNoResults_ThrowsSearchHasGivenNoResultsException() {
        when(articleDaoMock.findArticles(null, null, null, null, null, 0, null, 0, 0)).thenReturn(Collections.emptyList());

    	Exception thrownException = assertThrows(SearchHasGivenNoResultsException.class, () -> {
    		articleController.searchArticles(null, null, null, null, null, null, null, null, 0, null, 0, 0);
		});
		assertEquals("The search has given 0 results!", thrownException.getMessage());	
    }

	@SuppressWarnings("unchecked")
	@ParameterizedTest
	@MethodSource("testSearchArticlesArgumentsProvider")
	void testSearchArticles_WhenSpecificIdentifier_PerformsSpecificSearch_OtherwisePerformsGeneralSearch(String isbn,
			String issn, String isan, String title, String genre, String publisher, LocalDate yearEdition,
			String author, int issueNumber, String director, Object expectedResult, Class<?> daoClass) {
		if (daoClass.equals(BookDao.class)) {
			when(bookDaoMock.findBooksByIsbn(isbn)).thenReturn((List<Book>) expectedResult);
		} else if (daoClass.equals(MagazineDao.class)) {
			when(magazineDaoMock.findMagazinesByIssn(issn)).thenReturn((List<Magazine>) expectedResult);
		} else if (daoClass.equals(MovieDVDDao.class)) {
			when(movieDVDDaoMock.findMoviesByIsan(isan)).thenReturn((List<MovieDVD>) expectedResult);
		} else if (daoClass.equals(ArticleDao.class)) {
			when(articleDaoMock.findArticles(title, genre, publisher, yearEdition, author, issueNumber, director, 0, 0))
					.thenReturn((List<Article>) expectedResult);
		}

		List<? extends Article> result = articleController.searchArticles(isbn, issn, isan, title, genre, publisher,
				yearEdition, author, issueNumber, director, 0, 0);

		assertFalse(result.isEmpty());
		assertEquals(expectedResult, result);
	}
	
	@Test
	void testSearchArticles_WhenArticleIsBooked_PerformsValidateState() {
		Book mockBook = mock(Book.class);
		Booking mockBooking = mock(Booking.class);
		
		when(mockBook.getState()).thenReturn(ArticleState.BOOKED);
		when(articleDaoMock.findArticles(null, null, null, null, null, null, null, 0, 0)).thenReturn(List.of(mockBook));
		when(bookingDaoMock.searchBookings(null, mockBook, 0, 1)).thenReturn(List.of(mockBooking));
		
		articleController.searchArticles(null, null, null, null, null, null, null, null, null, null, 0, 0);

		verify(mockBooking).validateState();
	}

	@Test
	void testSearchArticles_WhenArticleIsOnloan_PerformsValidateState() {
		Book mockBook = mock(Book.class);
		Loan mockLoan = mock(Loan.class);
		
		when(mockBook.getState()).thenReturn(ArticleState.ONLOAN);
		when(articleDaoMock.findArticles(null, null, null, null, null, null, null, 0, 0)).thenReturn(List.of(mockBook));
		when(loanDaoMock.searchLoans(null, mockBook, 0, 1)).thenReturn(List.of(mockLoan));
		
		articleController.searchArticles(null, null, null, null, null, null, null, null, null, null, 0, 0);

		verify(mockLoan).validateState();
	}

	@Test
	void testSearchArticles_WhenArticleIsOnloanBooked_PerformsValidateState() {
		Book mockBook = mock(Book.class);
		Booking mockBooking = mock(Booking.class);
		Loan mockLoan = mock(Loan.class);
		
		when(mockBook.getState()).thenReturn(ArticleState.ONLOANBOOKED);
		when(articleDaoMock.findArticles(null, null, null, null, null, null, null, 0, 0)).thenReturn(List.of(mockBook));
		when(loanDaoMock.searchLoans(null, mockBook, 0, 1)).thenReturn(List.of(mockLoan));
		when(bookingDaoMock.searchBookings(null, mockBook, 0, 1)).thenReturn(List.of(mockBooking));

		articleController.searchArticles(null, null, null, null, null, null, null, null, null, null, 0, 0);

		verify(mockLoan).validateState();
		verify(mockBooking).validateState();
	}

	@Test
	public void testCountArticles_WhenIsbnNotNull_PerformsCountBooksByIsbn() {
		articleController.countArticles("1234567890", null, null, null, null, null, null, null, null, null);

		verify(bookDaoMock).countBooksByIsbn("1234567890");
	}

	@Test
	public void testCountArticles_WhenIssnNotNull_PerformsCountMagazinesByIssn() {
		articleController.countArticles(null, "9876543210", null, null, null, null, null, null, null, null);

		verify(magazineDaoMock).countMagazinesByIssn("9876543210");
	}

	@Test
	public void testCountArticles_WhenIsanNotNull_PerformsCountMoviesByIsan() {
		articleController.countArticles(null, null, "1357924680", null, null, null, null, null, null, null);

		verify(movieDVDDaoMock).countMoviesByIsan("1357924680");
	}

	@Test
	public void testCountArticlesByOtherParameters() {
		articleController.countArticles(null, null, null, "Some Title", "Some Genre", "Some Publisher", LocalDate.now(),
				"Some Author", 1, "Some Director");

		verify(articleDaoMock).countArticles("Some Title", "Some Genre", "Some Publisher", LocalDate.now(),
				"Some Author", 1, "Some Director");
	}

	@Test
    public void testRemoveArticle_WhenArticleDoesNotExist_ThrowsArticleDoesNotExistException() {
    	when(articleDaoMock.findById(1L)).thenReturn(null);
    	
    	Exception thrownException = assertThrows(ArticleDoesNotExistException.class, () -> {
    		articleController.removeArticle(1L);
		});
		assertEquals("Cannot remove Article! Article not in catalogue!", thrownException.getMessage());	
    }

	@ParameterizedTest
	@MethodSource("testRemoveArticleArgumentsProvider")
	void testRemoveArticle_BehavesCorrectlyBasedOnArticleState(ArticleState articleState, BookingState bookingState,
			Class<? extends RuntimeException> expectedException, String exceptionMessage) {
		Article article = (articleState != null) ? mock(Article.class) : null;
		Booking booking = (bookingState != null) ? mock(Booking.class) : null;

		when(articleDaoMock.findById(1L)).thenReturn(article);
		if (article != null) {
			when(article.getState()).thenReturn(articleState);
		}
		if (articleState == ArticleState.BOOKED) {
			when(bookingDaoMock.searchBookings(null, article, 0, 1)).thenReturn(List.of(booking));
			when(booking.getState()).thenReturn(bookingState);
		}

		if (expectedException == null) {
			articleController.removeArticle(1L);
			if (bookingState == BookingState.ACTIVE) {
				verify(booking).setState(BookingState.CANCELLED);
				verify(articleDaoMock).delete(article);
			}
		} else {
			Exception thrownException = assertThrows(expectedException, () -> {
				articleController.removeArticle(1L);
			});
			assertEquals(exceptionMessage, thrownException.getMessage());

		}
	}

	private static Stream<Arguments> testUpdateArticleArgumentsProvider() {
		return Stream.of(Arguments.of("UNAVAILABLE", "AVAILABLE", true), Arguments.of("AVAILABLE", "UNAVAILABLE", true),

				Arguments.of("UNAVAILABLE", "UNAVAILABLE", true), Arguments.of("UNAVAILABLE", "ONLOAN", false),
				Arguments.of("UNAVAILABLE", "ONLOANBOOKED", false), Arguments.of("UNAVAILABLE", "BOOKED", false),
				Arguments.of("UNAVAILABLE", null, false),

				Arguments.of("AVAILABLE", "AVAILABLE", true), Arguments.of("AVAILABLE", "ONLOAN", false),
				Arguments.of("AVAILABLE", "ONLOANBOOKED", false), Arguments.of("AVAILABLE", "BOOKED", false),
				Arguments.of("AVAILABLE", null, false),

				Arguments.of("ONLOAN", "ONLOAN", true), Arguments.of("ONLOAN", "UNAVAILABLE", false),
				Arguments.of("ONLOAN", "AVAILABLE", false), Arguments.of("ONLOAN", "BOOKED", false),
				Arguments.of("ONLOAN", "ONLOANBOOKED", false), Arguments.of("ONLOAN", null, false),

				Arguments.of("ONLOANBOOKED", "ONLOANBOOKED", true), Arguments.of("ONLOANBOOKED", "UNAVAILABLE", false),
				Arguments.of("ONLOANBOOKED", "AVAILABLE", false), Arguments.of("ONLOANBOOKED", "ONLOAN", false),
				Arguments.of("ONLOANBOOKED", "BOOKED", false), Arguments.of("ONLOANBOOKED", null, false),

				Arguments.of("BOOKED", "BOOKED", true), Arguments.of("BOOKED", "ONLOAN", false),
				Arguments.of("BOOKED", "ONLOANBOOKED", false), Arguments.of("BOOKED", "AVAILABLE", false),
				Arguments.of("BOOKED", "UNAVAILABLE", false), Arguments.of("BOOKED", null, false));
	}

	private static Stream<Arguments> testGetArticleInfoArgumentsProvider() {
		return Stream.of(Arguments.of(ArticleState.BOOKED), Arguments.of(ArticleState.ONLOAN),
				Arguments.of(ArticleState.ONLOANBOOKED), Arguments.of(ArticleState.AVAILABLE),
				Arguments.of(ArticleState.UNAVAILABLE));
	}

	private static Stream<Arguments> testSearchArticlesArgumentsProvider() {
		Article mockArticle = mock(Article.class);
		Book mockBook = mock(Book.class);
		Magazine mockMagazine = mock(Magazine.class);
		MovieDVD mockMovieDVD = mock(MovieDVD.class);

		return Stream.of(
				Arguments.of("isbn", null, null, null, null, null, null, null, 0, null, Arrays.asList(mockBook),
						BookDao.class),
				Arguments.of(null, "issn", null, null, null, null, null, null, 0, null, Arrays.asList(mockMagazine),
						MagazineDao.class),
				Arguments.of(null, null, "isan", null, null, null, null, null, 0, null, Arrays.asList(mockMovieDVD),
						MovieDVDDao.class),
				Arguments.of(null, null, null, "Title", null, null, null, null, 0, null, Arrays.asList(mockArticle),
						ArticleDao.class));
	}

	private static Stream<Arguments> testRemoveArticleArgumentsProvider() {
		return Stream.of(
				Arguments.of(null, null, ArticleDoesNotExistException.class,
						"Cannot remove Article! Article not in catalogue!"), // Article does not exist
				Arguments.of(ArticleState.ONLOAN, null, InvalidOperationException.class,
						"Cannot remove Article from catalogue! Article currently on loan!"), // Article on loan
				Arguments.of(ArticleState.ONLOANBOOKED, null, InvalidOperationException.class,
						"Cannot remove Article from catalogue! Article currently on loan!"), // Article on loan booked
				Arguments.of(ArticleState.BOOKED, BookingState.ACTIVE, null, null), // Article booked, active booking
				Arguments.of(ArticleState.BOOKED, BookingState.CANCELLED, InvalidOperationException.class,
						"Cannot remove Article from catalogue! Inconsistent state!"), // Article booked, inactive
																						// booking
				Arguments.of(ArticleState.AVAILABLE, null, null, null), // Article available (default case)
				Arguments.of(ArticleState.UNAVAILABLE, null, null, null) // Article unavailable (default case)
		);
	}
}
