package it.gurzu.SWAM.iLib.controllerTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import it.gurzu.swam.iLib.controllers.BookingController;
import it.gurzu.swam.iLib.dao.ArticleDao;
import it.gurzu.swam.iLib.dao.BookingDao;
import it.gurzu.swam.iLib.dao.UserDao;
import it.gurzu.swam.iLib.dto.BookingDTO;
import it.gurzu.swam.iLib.exceptions.ArticleDoesNotExistException;
import it.gurzu.swam.iLib.exceptions.BookingDoesNotExistException;
import it.gurzu.swam.iLib.exceptions.InvalidOperationException;
import it.gurzu.swam.iLib.exceptions.SearchHasGivenNoResultsException;
import it.gurzu.swam.iLib.exceptions.UserDoesNotExistException;
import it.gurzu.swam.iLib.model.Article;
import it.gurzu.swam.iLib.model.ArticleState;
import it.gurzu.swam.iLib.model.Booking;
import it.gurzu.swam.iLib.model.BookingState;
import it.gurzu.swam.iLib.model.User;

public class BookingControllerTest {
	private BookingController bookingController;
	private BookingDao bookingDaoMock;
	private UserDao userDaoMock;
	private ArticleDao articleDaoMock;
	
	private final LocalDate today = LocalDate.now();

	
	@BeforeEach
	public void setup() throws IllegalAccessException {
		bookingController = new BookingController();
		bookingDaoMock = mock(BookingDao.class);
		userDaoMock = mock(UserDao.class);
		articleDaoMock = mock(ArticleDao.class);
		
		FieldUtils.writeField(bookingController, "bookingDao", bookingDaoMock, true);
		FieldUtils.writeField(bookingController, "userDao", userDaoMock, true);
		FieldUtils.writeField(bookingController, "articleDao", articleDaoMock, true);
	}
	
	
	@Test
	public void testRegisterBooking_WhenUserDoesNotExist_ThrowsUserDoesNotExistException() {
		Article mockArticle = mock(Article.class);
		when(userDaoMock.findById(1L)).thenReturn(null);
		when(articleDaoMock.findById(1L)).thenReturn(mockArticle);
		
		Exception thrownException = assertThrows(UserDoesNotExistException.class, ()->{
			bookingController.registerBooking(1L, 1L);
			
		});
		assertEquals("Cannot register Booking, specified User not present in the system!", thrownException.getMessage());			
	}
	
	@Test
	public void testRegisterBooking_WhenArticleDoesNotExist_ThrowsArticleDoesNotExistException() {
		User mockUser = mock(User.class);
		when(articleDaoMock.findById(1L)).thenReturn(null);
		when(userDaoMock.findById(1L)).thenReturn(mockUser);
		
		Exception thrownException = assertThrows(ArticleDoesNotExistException.class, ()->{
			bookingController.registerBooking(1L, 1L);
			
		});
		assertEquals("Cannot register Booking, specified Article not present in catalogue!", thrownException.getMessage());			
	}
	
    @ParameterizedTest
    @EnumSource(value = ArticleState.class, names = {"BOOKED", "ONLOANBOOKED", "UNAVAILABLE"})
    public void testRegisterBooking_WhenInvalidArticleStateConditions_ThrowsInvalidOperationException(ArticleState state) {
        User user = mock(User.class);
        Article article = mock(Article.class);

        when(userDaoMock.findById(1L)).thenReturn(user);
        when(articleDaoMock.findById(1L)).thenReturn(article);
        when(article.getState()).thenReturn(state);

		Exception thrownException = assertThrows(InvalidOperationException.class, ()->{
			bookingController.registerBooking(1L, 1L);
			
		});
		if(state == ArticleState.BOOKED || state == ArticleState.ONLOANBOOKED)
			assertEquals("Cannot register Booking, specified Article is already booked!", thrownException.getMessage());			
		else
			assertEquals("Cannot register Booking, specified Article is UNAVAILABLE!", thrownException.getMessage());			
    }
	
	
    @ParameterizedTest
    @MethodSource("testRegisterBooking_SuccessfulRegistrationArgumentsProvider")
    public void testRegisterBooking_SuccessfulRegistration(ArticleState initialState, ArticleState expectedState) {
        User user = mock(User.class);
        Article article = mock(Article.class);

        when(userDaoMock.findById(1L)).thenReturn(user);
        when(articleDaoMock.findById(1L)).thenReturn(article);
        when(article.getState()).thenReturn(initialState);

        Long returnedId = bookingController.registerBooking(1L, 1L);

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingDaoMock).save(bookingCaptor.capture());
        Booking registeredBooking = bookingCaptor.getValue();

        assertEquals(returnedId, registeredBooking.getId());
        assertEquals(user, registeredBooking.getBookingUser());
        assertEquals(article, registeredBooking.getBookedArticle());
        assertEquals(BookingState.ACTIVE, registeredBooking.getState());
        verify(article).setState(expectedState);

        if (initialState == ArticleState.AVAILABLE) {
            assertEquals(today.plusDays(3), registeredBooking.getBookingEndDate());
        }
    }
	
	@Test
	public void testGetBookingInfo_WhenBookingNotInTheSystem_ThrowsBookingDoesNotExistException() {
		when(bookingDaoMock.findById(1L)).thenReturn(null);
		
		Exception thrownException = assertThrows(BookingDoesNotExistException.class, ()->{
			bookingController.getBookingInfo(1L);
			
		});
		assertEquals("Specified Booking not registered in the system!", thrownException.getMessage());
	}
	
	@Test
	public void testGetBookingInfo_WhenBookingFoundAndActive_CallsValidateState() {
	    Booking mockBooking = mock(Booking.class);
    	Article mockArticle = mock(Article.class);
    	User mockUser = mock(User.class);

    	when(mockArticle.getId()).thenReturn(3L);
    	when(mockUser.getId()).thenReturn(5L);

    	when(mockBooking.getBookedArticle()).thenReturn(mockArticle);
    	when(mockBooking.getBookingUser()).thenReturn(mockUser);

	    when(mockBooking.getState()).thenReturn(BookingState.ACTIVE);
	    when(bookingDaoMock.findById(1L)).thenReturn(mockBooking);
	    
	    bookingController.getBookingInfo(1L);

	    verify(mockBooking).validateState();
	}
	
    @ParameterizedTest
    @EnumSource(value = BookingState.class, names = {"CANCELLED", "COMPLETED", "EXPIRED"})
	public void testGetBookingInfo_WhenBookingFoundAndNotActive_DoesNotCallValidateState(BookingState state) {
	    Booking mockBooking = mock(Booking.class);
    	Article mockArticle = mock(Article.class);
    	User mockUser = mock(User.class);

    	when(mockArticle.getId()).thenReturn(3L);
    	when(mockUser.getId()).thenReturn(5L);

    	when(mockBooking.getBookedArticle()).thenReturn(mockArticle);
    	when(mockBooking.getBookingUser()).thenReturn(mockUser);
    	
        when(mockBooking.getState()).thenReturn(state);
	    when(bookingDaoMock.findById(1L)).thenReturn(mockBooking);

	    bookingController.getBookingInfo(1L);

	    verify(mockBooking, never()).validateState();
	}
    
    @Test
    public void testGetBookingInfo_ReturnsCorrectDTO() {
    	Booking mockBooking = mock(Booking.class);
    	Article mockArticle = mock(Article.class);
    	User mockUser = mock(User.class);
    	
    	when(mockArticle.getId()).thenReturn(3L);
    	when(mockArticle.getTitle()).thenReturn("a title");
    	
    	when(mockUser.getId()).thenReturn(5L);
    	
    	when(mockBooking.getId()).thenReturn(1L);
		when(mockBooking.getState()).thenReturn(BookingState.ACTIVE);
    	when(mockBooking.getBookedArticle()).thenReturn(mockArticle);
    	when(mockBooking.getBookingUser()).thenReturn(mockUser);
    	when(mockBooking.getBookingDate()).thenReturn(today);
    	when(mockBooking.getBookingEndDate()).thenReturn(today.plusDays(3));
	    when(bookingDaoMock.findById(1L)).thenReturn(mockBooking);

    	BookingDTO bookingDTO = bookingController.getBookingInfo(1L);
    	
    	assertNotNull(bookingDTO);
    	assertEquals(bookingDTO.getId(), mockBooking.getId());
    	assertEquals(bookingDTO.getState(), mockBooking.getState());
    	assertEquals(bookingDTO.getBookedArticleId(), mockBooking.getBookedArticle().getId());
    	assertEquals(bookingDTO.getBookedArticleTitle(), mockBooking.getBookedArticle().getTitle());
    	assertEquals(bookingDTO.getBookingUserId(), mockBooking.getBookingUser().getId());
    	assertEquals(bookingDTO.getBookingDate(), mockBooking.getBookingDate());
    	assertEquals(bookingDTO.getBookingEndDate(), mockBooking.getBookingEndDate());
    }
	
    @Test
    public void testCancelBooking_WhenBookingNotFound_ThrowsBookingDoesNotExistException() {
        when(bookingDaoMock.findById(1L)).thenReturn(null);

		Exception thrownException = assertThrows(BookingDoesNotExistException.class, ()->{
			bookingController.cancelBooking(1L);
			
		});
		assertEquals("Cannot cancel Booking. Specified Booking not registered in the system!", thrownException.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = BookingState.class, names = {"CANCELLED", "COMPLETED", "EXPIRED"})
    public void testCancelBooking_WhenBookingStateIsNotActive_ThrowsInvalidOperationException(BookingState state) {
    	Booking mockBooking = mock(Booking.class);
    	when(mockBooking.getState()).thenReturn(state);
    	when(bookingDaoMock.findById(1L)).thenReturn(mockBooking);
    	
		Exception thrownException = assertThrows(InvalidOperationException.class, ()->{
			bookingController.cancelBooking(1L);
			
		});
		assertEquals("Cannot cancel Booking. Specified Booking is not active!", thrownException.getMessage());
    }
    
    @Test
    public void testCancelBooking_WhenBookingStateIsActive_SuccessfullyCancelBooking() {
        Booking mockBooking = mock(Booking.class);
        Article mockArticle = mock(Article.class);
        
        when(mockBooking.getState()).thenReturn(BookingState.ACTIVE);
        when(mockBooking.getBookedArticle()).thenReturn(mockArticle);
        when(bookingDaoMock.findById(1L)).thenReturn(mockBooking);

        bookingController.cancelBooking(1L);

        verify(mockBooking).setState(BookingState.CANCELLED);
        verify(mockArticle).setState(ArticleState.AVAILABLE);
    }

    
    @Test
    public void testGetBookedArticlesByUser_WhenUserDoesNotExist_ThrowsUserDoesNotExistException() {
    	when(userDaoMock.findById(1L)).thenReturn(null);
    	
		Exception thrownException = assertThrows(UserDoesNotExistException.class, ()->{
			bookingController.getBookingsByUser(1L, 0, 0);
			
		});
		assertEquals("Specified user is not registered in the system!", thrownException.getMessage());
    }
    
    @Test
    public void testGetBookedArticlesByUser_WhenNoBookingsFound_ThrowsSearchHasGivenNoResultsException() {
    	User user = mock(User.class);
    	when(userDaoMock.findById(1L)).thenReturn(user);
    	when(bookingDaoMock.searchBookings(user, null, 0, 0)).thenReturn(Collections.emptyList());
    	
		Exception thrownException = assertThrows(SearchHasGivenNoResultsException.class, ()->{
			bookingController.getBookingsByUser(1L, 0, 0);
			
		});
		assertEquals("No bookings relative to the specified user found!", thrownException.getMessage());
    }
    
    @Test
    public void testGetBookedArticlesByUser_WhenUserHasActiveBookings_ValidateStateCalled() {
        User user = mock(User.class);
        Booking activeBooking1 = mock(Booking.class);
        Booking activeBooking2 = mock(Booking.class);

        when(activeBooking1.getState()).thenReturn(BookingState.ACTIVE);
        when(activeBooking2.getState()).thenReturn(BookingState.ACTIVE);


        when(userDaoMock.findById(1L)).thenReturn(user);
        when(bookingDaoMock.searchBookings(user, null, 0, 0)).thenReturn(Arrays.asList(activeBooking1, activeBooking2));

        bookingController.getBookingsByUser(1L, 0, 0);

        verify(activeBooking1).validateState();
        verify(activeBooking1).validateState();
    }

    
    @ParameterizedTest
    @EnumSource(value = BookingState.class, names = {"CANCELLED", "COMPLETED", "EXPIRED"})
    public void testGetBookedArticlesByUser_WhenUserHasNotActiveBookings_ValidateStateNotCalled(BookingState state) {
        User user = mock(User.class);
        Booking activeBooking1 = mock(Booking.class);
        Booking activeBooking2 = mock(Booking.class);

        when(activeBooking1.getState()).thenReturn(BookingState.ACTIVE);
        when(activeBooking2.getState()).thenReturn(state);


        when(userDaoMock.findById(1L)).thenReturn(user);
        when(bookingDaoMock.searchBookings(user, null, 0, 0)).thenReturn(Arrays.asList(activeBooking1, activeBooking2));

        bookingController.getBookingsByUser(1L, 0, 0);

        verify(activeBooking1).validateState();
        verify(activeBooking2, never()).validateState();
    }
    
    @Test
    public void testCountBookingsByUser_WhenUserDoesNotExist_ThrowsUserDoesNotExistException() {
    	when(userDaoMock.findById(1L)).thenReturn(null);
    	
		Exception thrownException = assertThrows(UserDoesNotExistException.class, ()->{
			bookingController.countBookingsByUser(1L);
			
		});
		assertEquals("Specified user is not registered in the system!", thrownException.getMessage());

    }
    
    @Test
    public void testCountBookingsByUser_WhenUserExists() {
    	User userMock = mock(User.class);
    	when(userDaoMock.findById(1L)).thenReturn(userMock);
    	
    	bookingController.countBookingsByUser(1L);
    	
    	verify(bookingDaoMock).countBookings(userMock, null);
    }
	
    private static Stream<Arguments> testRegisterBooking_SuccessfulRegistrationArgumentsProvider() {
        return Stream.of(
            Arguments.of(ArticleState.AVAILABLE, ArticleState.BOOKED),
            Arguments.of(ArticleState.ONLOAN, ArticleState.ONLOANBOOKED)
        );
    }
	
}
