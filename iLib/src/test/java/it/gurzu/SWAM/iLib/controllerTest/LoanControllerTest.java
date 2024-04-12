package it.gurzu.SWAM.iLib.controllerTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import it.gurzu.swam.iLib.controllers.LoanController;
import it.gurzu.swam.iLib.dao.ArticleDao;
import it.gurzu.swam.iLib.dao.BookingDao;
import it.gurzu.swam.iLib.dao.LoanDao;
import it.gurzu.swam.iLib.dao.UserDao;
import it.gurzu.swam.iLib.exceptions.ArticleDoesNotExistException;
import it.gurzu.swam.iLib.exceptions.InvalidOperationException;
import it.gurzu.swam.iLib.exceptions.LoanDoesNotExistException;
import it.gurzu.swam.iLib.exceptions.UserDoesNotExistException;
import it.gurzu.swam.iLib.model.Article;
import it.gurzu.swam.iLib.model.ArticleState;
import it.gurzu.swam.iLib.model.Booking;
import it.gurzu.swam.iLib.model.Loan;
import it.gurzu.swam.iLib.model.LoanState;
import it.gurzu.swam.iLib.model.User;

public class LoanControllerTest {
	private LoanController loanController;
	private UserDao userDaoMock;
	private ArticleDao articleDaoMock;
	private LoanDao loanDaoMock;
	private BookingDao bookingDaoMock;
	
	private final LocalDate today = LocalDate.now();
	
	
	@BeforeEach
	public void setup() throws IllegalAccessException {
		loanController = new LoanController();
		userDaoMock = mock(UserDao.class);
		articleDaoMock = mock(ArticleDao.class);
		loanDaoMock = mock(LoanDao.class);
		bookingDaoMock = mock(BookingDao.class);
		
		FieldUtils.writeField(loanController, "userDao", userDaoMock, true);
		FieldUtils.writeField(loanController, "articleDao", articleDaoMock, true);
		FieldUtils.writeField(loanController, "loanDao", loanDaoMock, true);
		FieldUtils.writeField(loanController, "bookingDao", bookingDaoMock, true);
	}
	
	
	@Test
	public void testRegisterLoan_WhenUserIdIsNull_ThrowsIllegalArgumentException() {
		Exception thrownException = assertThrows(IllegalArgumentException.class, ()->{
			loanController.registerLoan(null, 1L);
			
		});
		assertEquals("Cannot register Loan, User not specified!", thrownException.getMessage());	
	}

	
	@Test
	public void testRegisterLoan_WhenArticleIdIsNull_ThrowsIllegalArgumentException() {
		Exception thrownException = assertThrows(IllegalArgumentException.class, ()->{
			loanController.registerLoan(1L, null);
			
		});
		assertEquals("Cannot register Loan, Article not specified!", thrownException.getMessage());	
	}
	
	
	@Test
	public void testRegisterLoan_WhenUserDoesNotExist_ThrowsUserDoesNotExistException() {
		Article mockArticle = mock(Article.class);
		when(userDaoMock.findById(1L)).thenReturn(null);
		when(articleDaoMock.findById(1L)).thenReturn(mockArticle);
		
		Exception thrownException = assertThrows(UserDoesNotExistException.class, ()->{
			loanController.registerLoan(1L, 1L);
			
		});
		assertEquals("Cannot register Loan, specified User not present in the system!", thrownException.getMessage());			
	}
	
	
	@Test
	public void testRegisterLoan_WhenArticleDoesNotExist_ThrowsArticleDoesNotExistException() {
		User mockUser = mock(User.class);
		when(articleDaoMock.findById(1L)).thenReturn(null);
		when(userDaoMock.findById(1L)).thenReturn(mockUser);
		
		Exception thrownException = assertThrows(ArticleDoesNotExistException.class, ()->{
			loanController.registerLoan(1L, 1L);
			
		});
		assertEquals("Cannot register Loan, specified Article not present in catalogue!", thrownException.getMessage());			
	}
	
	
	@Test
	public void testregisterLoan_WhenArticleBookedByAnotherUser_ThrowsInvalidOperationException() {
	    User mockUser = mock(User.class);
	    User mockOtherUser = mock(User.class);
	    Article article = mock(Article.class);
	    Booking booking = mock(Booking.class);
	    booking.setBookingUser(mockOtherUser);

	    when(userDaoMock.findById(1L)).thenReturn(mockUser);
	    when(articleDaoMock.findById(1L)).thenReturn(article);
	    when(article.getState()).thenReturn(ArticleState.BOOKED);
	    when(bookingDaoMock.searchBookings(null, article)).thenReturn(List.of(booking));

		Exception thrownException = assertThrows(InvalidOperationException.class, ()->{
			loanController.registerLoan(1L, 1L);
			
		});
		assertEquals("Cannot register Loan, specified Article is booked by another user!", thrownException.getMessage());			
	}

	
	@ParameterizedTest
	@EnumSource(value = ArticleState.class, names = {"ONLOAN", "ONLOANBOOKED", "UNAVAILABLE"})
	public void testRegisterLoan_WhenArticleNotLendable_ThrowsInvalidOperationException(ArticleState state) {
		User mockUser = mock(User.class);
		when(userDaoMock.findById(anyLong())).thenReturn(mockUser);
	    Article mockArticle = mock(Article.class);
	    when(articleDaoMock.findById(anyLong())).thenReturn(mockArticle);
	    when(mockArticle.getState()).thenReturn(state);

		Exception thrownException = assertThrows(InvalidOperationException.class, ()->{
			loanController.registerLoan(1L, 1L);
			
		});
		
		if(state == ArticleState.UNAVAILABLE)
			assertEquals("Cannot register Loan, specified Article is UNAVAILABLE!", thrownException.getMessage());
		else
			assertEquals("Cannot register Loan, specified Article is already on loan!", thrownException.getMessage());
	}
	
	
	@Test
	public void testRegisterLoan_WhenSuccessfulRegistration() {
	    User mockUser = mock(User.class);
	    Article mockArticle = mock(Article.class);
	    Booking mockBooking = mock(Booking.class);

	    when(userDaoMock.findById(1L)).thenReturn(mockUser);
	    when(articleDaoMock.findById(1L)).thenReturn(mockArticle);
	    when(mockBooking.getBookingUser()).thenReturn(mockUser);
	    when(mockArticle.getState()).thenReturn(ArticleState.BOOKED);
	    when(bookingDaoMock.searchBookings(null, mockArticle)).thenReturn(List.of(mockBooking));

	    ArgumentCaptor<Loan> loanCaptor = ArgumentCaptor.forClass(Loan.class);
	    
	    loanController.registerLoan(1L, 1L);
	    
	    verify(loanDaoMock).save(loanCaptor.capture());
	    Loan capturedLoan = loanCaptor.getValue();
	    
	    verify(mockArticle).setState(ArticleState.ONLOAN);
	    assertEquals(mockUser, capturedLoan.getLoaningUser());
	    assertEquals(mockArticle, capturedLoan.getArticleOnLoan());
	    assertEquals(today, capturedLoan.getLoanDate().toLocalDate());
	    assertEquals(Date.valueOf(today.plusMonths(1)), capturedLoan.getDueDate());
	    assertFalse(capturedLoan.isRenewed());
	    assertEquals(LoanState.ACTIVE, capturedLoan.getState());
	    assertEquals(today.plusMonths(1), capturedLoan.getDueDate().toLocalDate());
	}
	
	
	@Test
	public void testRegisterReturn_WhenLoanNotFound_ThrowsLoanDoesNotExistException() {
	    when(loanDaoMock.findById(1L)).thenReturn(null);

		Exception thrownException = assertThrows(LoanDoesNotExistException.class, ()->{
			loanController.registerReturn(1L);
			
		});
		assertEquals("Cannot return article! Loan not registered!", thrownException.getMessage());			
	}
	
	
	@Test
	public void testRegisterReturn_WhenLoanAlreadyReturned_ThrowsInvalidOperationEsception() {
		Loan mockLoan = mock(Loan.class);
		when(mockLoan.getState()).thenReturn(LoanState.RETURNED);
		when(loanDaoMock.findById(1L)).thenReturn(mockLoan);
		
		Exception thrownException = assertThrows(InvalidOperationException.class, ()->{
			loanController.registerReturn(1L);
			
		});
		assertEquals("Cannot return article! Loan has already been returned!", thrownException.getMessage());			
	}
	
	
	@ParameterizedTest
	@EnumSource(value = ArticleState.class, names = {"ONLOAN", "UNAVAILABLE"})
	public void testRegisterReturn_WhenArticleOnloanOrUnavailalbe_ThenReturnsSuccessfully(ArticleState state) {
	    Loan mockLoan = mock(Loan.class);
	    Article mockArticle = mock(Article.class);

	    when(loanDaoMock.findById(1L)).thenReturn(mockLoan);
	    when(mockLoan.getState()).thenReturn(LoanState.ACTIVE);
	    when(mockLoan.getArticleOnLoan()).thenReturn(mockArticle);
	    when(mockArticle.getState()).thenReturn(state);

	    loanController.registerReturn(1L);

	    verify(mockArticle).setState(ArticleState.AVAILABLE);
	    verify(mockLoan).setState(LoanState.RETURNED);
	}
	
	
	@Test
	public void testRegisterReturn_WhenArticleOnLoanBooked_ThenUpdatesBooking() {
	    Long loanId = 4L;
	    Loan mockLoan = mock(Loan.class);
	    Article mockArticle = mock(Article.class);
	    Booking mockBooking = mock(Booking.class);

	    when(loanDaoMock.findById(loanId)).thenReturn(mockLoan);
	    when(mockLoan.getState()).thenReturn(LoanState.ACTIVE);
	    when(mockLoan.getArticleOnLoan()).thenReturn(mockArticle);
	    when(mockArticle.getState()).thenReturn(ArticleState.ONLOANBOOKED);
	    when(bookingDaoMock.searchBookings(null, mockArticle)).thenReturn(List.of(mockBooking));

	    loanController.registerReturn(loanId);

	    verify(mockArticle).setState(ArticleState.BOOKED);
	    verify(mockBooking).setBookingEndDate(Date.valueOf(today.plusDays(3)));
	    verify(mockLoan).setState(LoanState.RETURNED);
	}
	
	
	@Test
	public void testGetLoanInfo_WhenLoanDoesNotExist_ThrowsLoanDoesNotExistException() {
		when(loanDaoMock.findById(1L)).thenReturn(null);
		
		Exception thrownException = assertThrows(LoanDoesNotExistException.class, ()->{
			loanController.getLoanInfo(1L);
			
		});
		assertEquals("Specified Loan not registered in the system!", thrownException.getMessage());			

	}
	
	
	@ParameterizedTest
	@EnumSource(value = LoanState.class, names = {"RETURNED", "OVERDUE"})
	public void testGetLoanInfo_WhenLoanNotActive(LoanState state) {
	    Loan mockLoan = mock(Loan.class);
	    when(mockLoan.getState()).thenReturn(state);
	    when(loanDaoMock.findById(1L)).thenReturn(mockLoan);

	    Loan loan = loanController.getLoanInfo(1L);
	    assertNotNull(loan);
	    assertEquals(mockLoan, loan);
	    verify(mockLoan, never()).validateState();
	}
	
	
	@Test
	public void testGetLoanInfo_WhenLoanActive_ThenCallsValidateState() {
		Long loanId = 3L;
		Loan mockLoan = mock(Loan.class);
		when(mockLoan.getState()).thenReturn(LoanState.ACTIVE);
		when(loanDaoMock.findById(loanId)).thenReturn(mockLoan);

		Loan loan = loanController.getLoanInfo(loanId);
		assertNotNull(loan);
		assertEquals(mockLoan, loan);
		verify(mockLoan).validateState();		
	}
	
	
	@Test
	public void testGetLoansByUser_WhenUserDoesNotExist_ThrowsUserDoesNotExistException() {
		when(userDaoMock.findById(1L)).thenReturn(null);
		
		Exception thrownException = assertThrows(UserDoesNotExistException.class, ()->{
			loanController.getLoansByUser(1L);
			
		});
		assertEquals("Specified user is not registered in the system!", thrownException.getMessage());					
	}
	
	
	@Test
	public void testGetLoansByUser_WhenUserHasNoLoans_ThrowsLoanDoesNotExistException() {
		User mockUser = mock(User.class);
		when(userDaoMock.findById(1L)).thenReturn(mockUser);
		when(loanDaoMock.searchLoans(mockUser, null)).thenReturn(Collections.emptyList());
		
		Exception thrownException = assertThrows(LoanDoesNotExistException.class, ()->{
			loanController.getLoansByUser(1L);
			
		});
		assertEquals("No loans relative to the specified user found!", thrownException.getMessage());					
	}
	
	
	@Test
	public void testGetLoansByUser_WhenUserHasLoans_ValidatesOnlyActiveLoans() {
	    User mockUser = mock(User.class);
	    Loan activeLoan = mock(Loan.class);
	    Loan overdueLoan = mock(Loan.class);
	    Loan returnedLoan = mock(Loan.class);

	    when(userDaoMock.findById(1L)).thenReturn(mockUser);
	    when(loanDaoMock.searchLoans(mockUser, null)).thenReturn(List.of(activeLoan, overdueLoan, returnedLoan));
	    when(activeLoan.getState()).thenReturn(LoanState.ACTIVE);
	    when(overdueLoan.getState()).thenReturn(LoanState.OVERDUE);
	    when(returnedLoan.getState()).thenReturn(LoanState.RETURNED);

	    List<Loan> returnedLoans = loanController.getLoansByUser(1L);

	    assertNotNull(returnedLoans);
	    assertEquals(3, returnedLoans.size());  
	    verify(activeLoan).validateState();  
	    verify(overdueLoan, never()).validateState();  
	    verify(returnedLoan, never()).validateState(); 
	}
	
	
	@Test
	public void testExtendLoan_WhenLoanDoesNotExist_ThrowsLoanDoesNotExistException() {
		when(loanDaoMock.findById(1L)).thenReturn(null);

		Exception thrownException = assertThrows(LoanDoesNotExistException.class, ()->{
			loanController.extendLoan(1L);
			
		});
		assertEquals("Cannot extend Loan! Loan does not exist!", thrownException.getMessage());					
	}
	
	
	@Test
	public void testExtendLoan_WhenLoanExistsButBookedByAnotherUser_ThrowsInvalidOperationException() {
		Loan mockLoanToExtend = mock(Loan.class);
		Article mockArticle = mock(Article.class);
		
		when(loanDaoMock.findById(1L)).thenReturn(mockLoanToExtend);
		when(mockLoanToExtend.getArticleOnLoan()).thenReturn(mockArticle);
		when(mockArticle.getState()).thenReturn(ArticleState.ONLOANBOOKED);
		
		Exception thrownException = assertThrows(InvalidOperationException.class, ()->{
			loanController.extendLoan(1L);
			
		});
		assertEquals("Cannot extend loan, another User has booked the Article!", thrownException.getMessage());					
	}
	
	
	@Test
	public void testExtendLoan_WhenLoanExistsAndNotBookedByAnotherUser_UpdatesDueDate() {
		Loan mockLoanToExtend = mock(Loan.class);
		Article mockArticle = mock(Article.class);
		
		when(loanDaoMock.findById(1L)).thenReturn(mockLoanToExtend);
		when(mockLoanToExtend.getArticleOnLoan()).thenReturn(mockArticle);
		when(mockArticle.getState()).thenReturn(ArticleState.ONLOAN);
		
		loanController.extendLoan(1L);
		
		verify(mockLoanToExtend).setDueDate(Date.valueOf(today.plusMonths(1)));
	}
}
