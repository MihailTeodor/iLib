package it.gurzu.SWAM.iLib.controllerTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import it.gurzu.swam.iLib.controllers.ArticleController;
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
import it.gurzu.swam.iLib.model.ArticleState;
import it.gurzu.swam.iLib.model.Book;
import it.gurzu.swam.iLib.model.Booking;
import it.gurzu.swam.iLib.model.BookingState;
import it.gurzu.swam.iLib.model.Loan;
import it.gurzu.swam.iLib.model.Magazine;
import it.gurzu.swam.iLib.model.ModelFactory;
import it.gurzu.swam.iLib.model.MovieDVD;

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
	public void testAddArticle_WhenIsbnIsNotNull_AddsABookArticle() {
		Book book = ModelFactory.book();
		try(MockedStatic<ModelFactory> modelFactoryMock = Mockito.mockStatic(ModelFactory.class)){
			modelFactoryMock.when(() -> ModelFactory.book()).thenReturn(book);
			articleController.addArticle("isbn", null, null, null, null, null, null, null, null, null, 0, null);
			modelFactoryMock.verify(() -> ModelFactory.book());
		}
	}
	
	@Test
	public void testAddArticle_WhenIssnIsNotNull_AddsAMagazineArticle() {
		Magazine magazine = ModelFactory.magazine();
		try(MockedStatic<ModelFactory> modelFactoryMock = Mockito.mockStatic(ModelFactory.class)){
			modelFactoryMock.when(() -> ModelFactory.magazine()).thenReturn(magazine);
			articleController.addArticle(null, "issn", null, null, null, null, null, null, null, null, 0, null);
			modelFactoryMock.verify(() -> ModelFactory.magazine());
		}
	}
	
	@Test
	public void testAddArticle_WhenIsanIsNotNull_AddsAMovieDVDArticle() {
		MovieDVD movieDVD = ModelFactory.movieDVD();
		try(MockedStatic<ModelFactory> modelFactoryMock = Mockito.mockStatic(ModelFactory.class)){
			modelFactoryMock.when(() -> ModelFactory.movieDVD()).thenReturn(movieDVD);
			articleController.addArticle(null, null, "isan", null, null, null, null, null, null, null, 0, null);
			modelFactoryMock.verify(() -> ModelFactory.movieDVD());
		}
	}
	
	@Test
	public void testAddArticle_WhenNoneBetweenIsbnIssnIsanSpecified_ThrowsIllegalArgumentEsception() {
		Exception thrownException = assertThrows(IllegalArgumentException.class, ()->{
			articleController.addArticle(null, null, null, null, null, null, null, null, null, null, 0, null);
			
		});
		assertEquals("Unique article identifier not inserted! Please insert one of isbn, issn, isan!", thrownException.getMessage());	
	}
	
	@Test
	public void testAddArticle_WhenAllOk() {
		articleController.addArticle("isbn", null, null, null, null, null, null, null, null, null, 0, null);
		
		verify(articleDaoMock).save(any(Article.class));
	}
	
	
	@Test
	public void testUpdateArticle_WhenArticleToUpdateDoesNotExist_ThrowsArticleDoesNotExistException() {
		when(articleDaoMock.findById(1L)).thenReturn(null);
		Exception thrownException = assertThrows(ArticleDoesNotExistException.class, ()->{
			articleController.updateArticle(1L, null, null, null, null, null, null, null, null, null, null, 0, null, null);
			
		});
		assertEquals("Article does not exist!", thrownException.getMessage());	
	}
	
	@Test
	public void testUpdateArticle_WhenArticleToUpdateExistsAndIsbnNotNull_UpdatesBookAttributes() {
		Book mockArticle = mock(Book.class);
		when(mockArticle.getState()).thenReturn(ArticleState.UNAVAILABLE);
		when(articleDaoMock.findById(1L)).thenReturn(mockArticle);
		
		articleController.updateArticle(1L, "isbn", null, null, null, null, null, null, null, null, null, 0, null, ArticleState.AVAILABLE);
		
		verify(mockArticle).setIsbn("isbn");
	}
	
	@Test
	public void testUpdateArticle_WhenArticleToUpdateExistsAndIssnNotNull_UpdatesMagazineAttributes() {
		Magazine mockArticle = mock(Magazine.class);
		when(mockArticle.getState()).thenReturn(ArticleState.UNAVAILABLE);
		when(articleDaoMock.findById(1L)).thenReturn(mockArticle);
		
		articleController.updateArticle(1L, null, "issn", null, null, null, null, null, null, null, null, 0, null, ArticleState.AVAILABLE);
		
		verify(mockArticle).setIssn("issn");
	}
	
	@Test
	public void testUpdateArticle_WhenArticleToUpdateExistsAndIsanNotNull_UpdatesMovieDVDAttributes() {
		MovieDVD mockArticle = mock(MovieDVD.class);
		when(mockArticle.getState()).thenReturn(ArticleState.UNAVAILABLE);
		when(articleDaoMock.findById(1L)).thenReturn(mockArticle);
		
		articleController.updateArticle(1L, null, null, "isan", null, null, null, null, null, null, null, 0, null, ArticleState.AVAILABLE);
		
		verify(mockArticle).setIsan("isan");
	}
	
    @ParameterizedTest
    @MethodSource("testUpdateArticleArgumentsProvider")
	public void testUpdateArticle_StateTransitions_HandledCorrectly(ArticleState initialState, ArticleState newState, boolean shouldSucceed) {
    	Article mockArticle = mock(Article.class);
    	when(mockArticle.getState()).thenReturn(initialState);
    	when(articleDaoMock.findById(1L)).thenReturn(mockArticle);
    	
    	try {
    		articleController.updateArticle(1l, null, null, null, null, null, null, null, null, null, null, 0, null, newState);
    		if(!shouldSucceed) {
    			fail("Expected InvalidStateTransitionException but none was thrown!");
    		}
    	}catch(InvalidStateTransitionException e) {
    		if(shouldSucceed) {
    			fail("Dod not expected InvalidStateTransitionException to be thrown!");
    		}
    		assertTrue(e instanceof InvalidStateTransitionException, "Cannot change state to inserted value!");
    	}
    	
    	if(shouldSucceed) {
    		verify(mockArticle).setState(newState);
    		verify(articleDaoMock).save(mockArticle);
    	}else {
    		verify(mockArticle, never()).setState(any(ArticleState.class));
    	}
	}
	
    @Test
    public void testgetArticleInfo_WhenArticleDoesNotExist_ThrowsArticleDoesNotExistException() {
    	when(articleDaoMock.findById(1L)).thenReturn(null);
    	Exception thrownException = assertThrows(ArticleDoesNotExistException.class, ()->{
			articleController.getArticleInfo(1L);
		});
		assertEquals("Article does not exist!", thrownException.getMessage());	
    }
    
    @ParameterizedTest
    @MethodSource("testGetArticleInfoArgumentsProvider")
    public void testGetArticleInfo_WhenArticleExists_CallsValidateState(ArticleState state, Class<?> validationClass) throws Exception {
        Article mockArticle = mock(Article.class);
        when(mockArticle.getState()).thenReturn(state);
        when(articleDaoMock.findById(1l)).thenReturn(mockArticle);

        Booking mockBooking = null;
        Loan mockLoan = null;
        
        if(validationClass == null) {}
        else 
        	if (validationClass.equals(Booking.class)) {
	            mockBooking = mock(Booking.class);
	            when(bookingDaoMock.searchBookings(null, mockArticle)).thenReturn(Arrays.asList(mockBooking));
	        } else if (validationClass.equals(Loan.class)) {
	            mockLoan = mock(Loan.class);
	            when(loanDaoMock.searchLoans(null, mockArticle)).thenReturn(Arrays.asList(mockLoan));
	        }

        Article resultArticle = articleController.getArticleInfo(1L);

        assertEquals(mockArticle, resultArticle);
        if(validationClass == null) {}
        
        else 
        	if (validationClass.equals(Booking.class)) {
	            verify(bookingDaoMock).searchBookings(null, mockArticle);
	            verify(mockBooking).validateState();
	        } else if (validationClass.equals(Loan.class)) {
	            verify(loanDaoMock).searchLoans(null, mockArticle);
	            verify(mockLoan).validateState();
	        }
    }
	
    @Test
    void testSearchArticles_WhenNoResults_ThrowsSearchHasGivenNoResultsException() {
        when(articleDaoMock.findArticles(null, null, null, null, null, 0, null)).thenReturn(Collections.emptyList());

    	Exception thrownException = assertThrows(SearchHasGivenNoResultsException.class, () -> {
    		articleController.searchArticles(null, null, null, null, null, null, null, null, 0, null);
		});
		assertEquals("The search has given 0 results!", thrownException.getMessage());	
    }
	
    @SuppressWarnings("unchecked")
	@ParameterizedTest
    @MethodSource("testSearchArticlesArgumentsProvider")
    void testSearchArticles_WhenSpecificIdentifier_PerformsSpecificSearch_OtherwisePerformsGeneralSearch(String isbn, String issn, String isan, String title, String genre,
                                      String publisher, Date yearEdition, String author, int issueNumber,
                                      String director, Object expectedResult, Class<?> daoClass) {
        if (daoClass.equals(BookDao.class)) {
            when(bookDaoMock.findBooksByIsbn(isbn)).thenReturn((List<Book>)expectedResult);
        } else if (daoClass.equals(MagazineDao.class)) {
            when(magazineDaoMock.findMagazinesByIssn(issn)).thenReturn((List<Magazine>)expectedResult);
        } else if (daoClass.equals(MovieDVDDao.class)) {
            when(movieDVDDaoMock.findMoviesByIsan(isan)).thenReturn((List<MovieDVD>)expectedResult);
        } else if (daoClass.equals(ArticleDao.class)) {
            when(articleDaoMock.findArticles(title, genre, publisher, yearEdition, author, issueNumber, director)).thenReturn((List<Article>)expectedResult);
        }

        List<? extends Article> result = articleController.searchArticles(isbn, issn, isan, title, genre, publisher, yearEdition, author, issueNumber, director);

        assertFalse(result.isEmpty());
        assertEquals(expectedResult, result);
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
    void testRemoveArticle_BehavesCorrectlyBasedOnArticleState(ArticleState articleState, BookingState bookingState, Class<? extends RuntimeException> expectedException, String exceptionMessage) {
        Article article = (articleState != null) ? mock(Article.class) : null;
        Booking booking = (bookingState != null) ? mock(Booking.class) : null;

        when(articleDaoMock.findById(1L)).thenReturn(article);
        if (article != null) {
            when(article.getState()).thenReturn(articleState);
        }
        if (articleState == ArticleState.BOOKED) {
            when(bookingDaoMock.searchBookings(null, article)).thenReturn(List.of(booking));
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
        return Stream.of(
            Arguments.of("UNAVAILABLE", "AVAILABLE", true),
            Arguments.of("AVAILABLE", "UNAVAILABLE", true),
            Arguments.of("UNAVAILABLE", "ONLOAN", false),
            Arguments.of("UNAVAILABLE", "ONLOANBOOKED", false),
            Arguments.of("UNAVAILABLE", "BOOKED", false),
            Arguments.of("AVAILABLE", "ONLOAN", false),
            Arguments.of("AVAILABLE", "ONLOANBOOKED", false),
            Arguments.of("AVAILABLE", "BOOKED", false),
            Arguments.of("ONLOAN", "UNAVAILABLE", false),
            Arguments.of("ONLOAN", "AVAILABLE", false),
            Arguments.of("ONLOAN", "BOOKED", false),
            Arguments.of("ONLOAN", "ONLOANBOOKED", false),
            Arguments.of("ONLOANBOOKED", "UNAVAILABLE", false),
            Arguments.of("ONLOANBOOKED", "AVAILABLE", false),
            Arguments.of("ONLOANBOOKED", "ONLOAN", false),
            Arguments.of("ONLOANBOOKED", "BOOKED", false),
            Arguments.of("BOOKED", "ONLOAN", false),
            Arguments.of("BOOKED", "ONLOANBOOKED", false),
            Arguments.of("BOOKED", "AVAILABLE", false),
            Arguments.of("BOOKED", "UNAVAILABLE", false)
        );
    }
    
    private static Stream<Arguments> testGetArticleInfoArgumentsProvider() {
        return Stream.of(
            Arguments.of(ArticleState.BOOKED, Booking.class),
            Arguments.of(ArticleState.ONLOAN, Loan.class),
            Arguments.of(ArticleState.ONLOANBOOKED, Loan.class),
            Arguments.of(ArticleState.AVAILABLE, null),
            Arguments.of(ArticleState.UNAVAILABLE, null)
        );
    } 
    
    private static Stream<Arguments> testSearchArticlesArgumentsProvider() {
        Article mockArticle = mock(Article.class); 
        Book mockBook = mock(Book.class);
        Magazine mockMagazine = mock(Magazine.class);
        MovieDVD mockMovieDVD = mock(MovieDVD.class);
        
        return Stream.of(
            Arguments.of("isbn", null, null, null, null, null, null, null, 0, null, Arrays.asList(mockBook), BookDao.class),
            Arguments.of(null, "issn", null, null, null, null, null, null, 0, null, Arrays.asList(mockMagazine), MagazineDao.class),
            Arguments.of(null, null, "isan", null, null, null, null, null, 0, null, Arrays.asList(mockMovieDVD), MovieDVDDao.class),
            Arguments.of(null, null, null, "Title", null, null, null, null, 0, null, Arrays.asList(mockArticle), ArticleDao.class)
        );
    }
    
    private static Stream<Arguments> testRemoveArticleArgumentsProvider() {
        return Stream.of(
            Arguments.of(null, null, ArticleDoesNotExistException.class, "Cannot remove Article! Article not in catalogue!"), // Article does not exist
            Arguments.of(ArticleState.ONLOAN, null, InvalidOperationException.class, "Cannot remove Article from catalogue! Article currently on loan!"), // Article on loan
            Arguments.of(ArticleState.ONLOANBOOKED, null, InvalidOperationException.class, "Cannot remove Article from catalogue! Article currently on loan!"), // Article on loan booked
            Arguments.of(ArticleState.BOOKED, BookingState.ACTIVE, null, null), // Article booked, active booking
            Arguments.of(ArticleState.BOOKED, BookingState.CANCELLED, InvalidOperationException.class, "Cannot remove Article from catalogue! Inconsistent state!"), // Article booked, inactive booking
            Arguments.of(ArticleState.AVAILABLE, null, null, null), // Article available (default case)
            Arguments.of(ArticleState.UNAVAILABLE, null, null, null) // Article unavailable (default case)
        );
    }
}
