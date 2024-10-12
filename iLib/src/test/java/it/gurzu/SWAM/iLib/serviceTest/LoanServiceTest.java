package it.gurzu.SWAM.iLib.serviceTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import it.gurzu.swam.iLib.dao.ArticleDao;
import it.gurzu.swam.iLib.dao.BookingDao;
import it.gurzu.swam.iLib.dao.LoanDao;
import it.gurzu.swam.iLib.dao.UserDao;
import it.gurzu.swam.iLib.dto.LoanDTO;
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
import it.gurzu.swam.iLib.services.LoanService;

public class LoanServiceTest {
	private LoanService loanService;
	private UserDao userDaoMock;
	private ArticleDao articleDaoMock;
	private LoanDao loanDaoMock;
	private BookingDao bookingDaoMock;
	
	private final LocalDate today = LocalDate.now();
	
	
	@BeforeEach
	public void setup() throws IllegalAccessException {
		loanService = new LoanService();
		userDaoMock = mock(UserDao.class);
		articleDaoMock = mock(ArticleDao.class);
		loanDaoMock = mock(LoanDao.class);
		bookingDaoMock = mock(BookingDao.class);
		
		FieldUtils.writeField(loanService, "userDao", userDaoMock, true);
		FieldUtils.writeField(loanService, "articleDao", articleDaoMock, true);
		FieldUtils.writeField(loanService, "loanDao", loanDaoMock, true);
		FieldUtils.writeField(loanService, "bookingDao", bookingDaoMock, true);
	}
	
	
	@Test
	public void testRegisterLoan_WhenUserDoesNotExist_ThrowsUserDoesNotExistException() {
		Article mockArticle = mock(Article.class);
		when(userDaoMock.findById(1L)).thenReturn(null);
		when(articleDaoMock.findById(1L)).thenReturn(mockArticle);
		
		Exception thrownException = assertThrows(UserDoesNotExistException.class, ()->{
			loanService.registerLoan(1L, 1L);
			
		});
		assertEquals("Cannot register Loan, specified User not present in the system!", thrownException.getMessage());			
	}
	
	
	@Test
	public void testRegisterLoan_WhenArticleDoesNotExist_ThrowsArticleDoesNotExistException() {
		User mockUser = mock(User.class);
		when(articleDaoMock.findById(1L)).thenReturn(null);
		when(userDaoMock.findById(1L)).thenReturn(mockUser);
		
		Exception thrownException = assertThrows(ArticleDoesNotExistException.class, ()->{
			loanService.registerLoan(1L, 1L);
			
		});
		assertEquals("Cannot register Loan, specified Article not present in catalogue!", thrownException.getMessage());			
	}
	
	
	@Test
	public void testRegisterLoan_WhenArticleBookedByAnotherUser_ThrowsInvalidOperationException() {
	    User mockUser = mock(User.class);
	    User mockOtherUser = mock(User.class);
	    Article article = mock(Article.class);
	    Booking booking = mock(Booking.class);
	    booking.setBookingUser(mockOtherUser);

	    when(userDaoMock.findById(1L)).thenReturn(mockUser);
	    when(articleDaoMock.findById(1L)).thenReturn(article);
	    when(article.getState()).thenReturn(ArticleState.BOOKED);
	    when(bookingDaoMock.searchBookings(null, article, 0, 1)).thenReturn(List.of(booking));

		Exception thrownException = assertThrows(InvalidOperationException.class, ()->{
			loanService.registerLoan(1L, 1L);
			
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
			loanService.registerLoan(1L, 1L);
			
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
	    when(bookingDaoMock.searchBookings(null, mockArticle, 0, 1)).thenReturn(List.of(mockBooking));

	    ArgumentCaptor<Loan> loanCaptor = ArgumentCaptor.forClass(Loan.class);
	    
	    Long returnedId = loanService.registerLoan(1L, 1L);
	    
	    verify(loanDaoMock).save(loanCaptor.capture());
	    Loan capturedLoan = loanCaptor.getValue();
	    
	    verify(mockArticle).setState(ArticleState.ONLOAN);
	    assertEquals(returnedId, capturedLoan.getId());
	    assertEquals(mockUser, capturedLoan.getLoaningUser());
	    assertEquals(mockArticle, capturedLoan.getArticleOnLoan());
	    assertEquals(today, capturedLoan.getLoanDate());
	    assertEquals(today.plusMonths(1), capturedLoan.getDueDate());
	    assertFalse(capturedLoan.isRenewed());
	    assertEquals(LoanState.ACTIVE, capturedLoan.getState());
	    assertEquals(today.plusMonths(1), capturedLoan.getDueDate());
	}
	
	
	@Test
	public void testRegisterReturn_WhenLoanNotFound_ThrowsLoanDoesNotExistException() {
	    when(loanDaoMock.findById(1L)).thenReturn(null);

		Exception thrownException = assertThrows(LoanDoesNotExistException.class, ()->{
			loanService.registerReturn(1L);
			
		});
		assertEquals("Cannot return article! Loan not registered!", thrownException.getMessage());			
	}
	
	
	@Test
	public void testRegisterReturn_WhenLoanAlreadyReturned_ThrowsInvalidOperationEsception() {
		Loan mockLoan = mock(Loan.class);
		when(mockLoan.getState()).thenReturn(LoanState.RETURNED);
		when(loanDaoMock.findById(1L)).thenReturn(mockLoan);
		
		Exception thrownException = assertThrows(InvalidOperationException.class, ()->{
			loanService.registerReturn(1L);
			
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

	    loanService.registerReturn(1L);

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
	    when(bookingDaoMock.searchBookings(null, mockArticle, 0, 1)).thenReturn(List.of(mockBooking));

	    loanService.registerReturn(loanId);

	    verify(mockArticle).setState(ArticleState.BOOKED);
	    verify(mockBooking).setBookingEndDate(today.plusDays(3));
	    verify(mockLoan).setState(LoanState.RETURNED);
	}
	
	
	@Test
	public void testGetLoanInfo_WhenLoanDoesNotExist_ThrowsLoanDoesNotExistException() {
		when(loanDaoMock.findById(1L)).thenReturn(null);
		
		Exception thrownException = assertThrows(LoanDoesNotExistException.class, ()->{
			loanService.getLoanInfo(1L);
			
		});
		assertEquals("Specified Loan not registered in the system!", thrownException.getMessage());			

	}
	
	
	@ParameterizedTest
	@EnumSource(value = LoanState.class, names = {"RETURNED", "OVERDUE"})
	public void testGetLoanInfo_WhenLoanNotActive_DoesNotCallValidateState(LoanState state) {
	    Loan mockLoan = mock(Loan.class);
    	Article mockArticle = mock(Article.class);
    	User mockUser = mock(User.class);

    	when(mockArticle.getId()).thenReturn(3L);
    	when(mockUser.getId()).thenReturn(5L);

    	when(mockLoan.getArticleOnLoan()).thenReturn(mockArticle);
    	when(mockLoan.getLoaningUser()).thenReturn(mockUser);

	    when(mockLoan.getState()).thenReturn(state);
	    when(loanDaoMock.findById(1L)).thenReturn(mockLoan);

	    loanService.getLoanInfo(1L);
	    
	    verify(mockLoan, never()).validateState();
	}
	
	
	@Test
	public void testGetLoanInfo_WhenLoanActive_ThenCallsValidateState() {
		Loan mockLoan = mock(Loan.class);
    	Article mockArticle = mock(Article.class);
    	User mockUser = mock(User.class);

    	when(mockArticle.getId()).thenReturn(3L);
    	when(mockUser.getId()).thenReturn(5L);

    	when(mockLoan.getArticleOnLoan()).thenReturn(mockArticle);
    	when(mockLoan.getLoaningUser()).thenReturn(mockUser);

		when(mockLoan.getState()).thenReturn(LoanState.ACTIVE);
		when(loanDaoMock.findById(1L)).thenReturn(mockLoan);

		loanService.getLoanInfo(1L);
		
		verify(mockLoan).validateState();		
	}
	
	
	@Test
	public void testGetLoanInfo_ReturnsCorrectDTO() {
		Loan mockLoan = mock(Loan.class);
    	Article mockArticle = mock(Article.class);
    	User mockUser = mock(User.class);
		
    	when(mockArticle.getId()).thenReturn(3L);
    	when(mockArticle.getTitle()).thenReturn("a title");
    	
    	when(mockUser.getId()).thenReturn(5L);

    	when(mockLoan.getId()).thenReturn(1L);
    	when(mockLoan.getState()).thenReturn(LoanState.ACTIVE);
    	when(mockLoan.getArticleOnLoan()).thenReturn(mockArticle);
    	when(mockLoan.getLoaningUser()).thenReturn(mockUser);
    	when(mockLoan.getLoanDate()).thenReturn(today);
    	when(mockLoan.getDueDate()).thenReturn(today.plusMonths(1));
    	when(mockLoan.isRenewed()).thenReturn(true);
    	when(loanDaoMock.findById(1L)).thenReturn(mockLoan);

    	LoanDTO loanDTO = loanService.getLoanInfo(1L);
    	
    	assertNotNull(loanDTO);
    	assertEquals(loanDTO.getId(), mockLoan.getId());
    	assertEquals(loanDTO.getState(), mockLoan.getState());
    	assertEquals(loanDTO.getArticleId(), mockLoan.getArticleOnLoan().getId());
    	assertEquals(loanDTO.getArticleTitle(), mockLoan.getArticleOnLoan().getTitle());
    	assertEquals(loanDTO.getLoaningUserId(), mockLoan.getLoaningUser().getId());
    	assertEquals(loanDTO.getLoanDate(), mockLoan.getLoanDate());
    	assertEquals(loanDTO.getDueDate(), mockLoan.getDueDate());
    	assertEquals(loanDTO.isRenewed(), mockLoan.isRenewed());
	}
	
	@Test
	public void testGetLoansByUser_WhenUserDoesNotExist_ThrowsUserDoesNotExistException() {
		when(userDaoMock.findById(1L)).thenReturn(null);
		
		Exception thrownException = assertThrows(UserDoesNotExistException.class, ()->{
			loanService.getLoansByUser(1L, 0, 0);
			
		});
		assertEquals("Specified user is not registered in the system!", thrownException.getMessage());					
	}
	
	
	@Test
	public void testGetLoansByUser_WhenUserHasNoLoans_ThrowsLoanDoesNotExistException() {
		User mockUser = mock(User.class);
		when(userDaoMock.findById(1L)).thenReturn(mockUser);
		when(loanDaoMock.searchLoans(mockUser, null, 0, 0)).thenReturn(Collections.emptyList());
		
		Exception thrownException = assertThrows(LoanDoesNotExistException.class, ()->{
			loanService.getLoansByUser(1L, 0, 0);
			
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
	    when(loanDaoMock.searchLoans(mockUser, null, 0, 0)).thenReturn(List.of(activeLoan, overdueLoan, returnedLoan));
	    when(activeLoan.getState()).thenReturn(LoanState.ACTIVE);
	    when(overdueLoan.getState()).thenReturn(LoanState.OVERDUE);
	    when(returnedLoan.getState()).thenReturn(LoanState.RETURNED);

	    List<Loan> returnedLoans = loanService.getLoansByUser(1L, 0, 0);

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
			loanService.extendLoan(1L);
			
		});
		assertEquals("Cannot extend Loan! Loan does not exist!", thrownException.getMessage());					
	}

	@ParameterizedTest
	@EnumSource(value = LoanState.class, names = {"RETURNED", "OVERDUE"})
	public void testExtendLoan_WhenLoanExistsButNotActive_ThrowsInvalidOperationException(LoanState state) {
		Loan mockLoanToExtend = mock(Loan.class);
		
		when(loanDaoMock.findById(1L)).thenReturn(mockLoanToExtend);
		when(mockLoanToExtend.getState()).thenReturn(state);
		
		Exception thrownException = assertThrows(InvalidOperationException.class, ()->{
			loanService.extendLoan(1L);
			
		});
		assertEquals("Cannot extend loan, selected loan is not Active!", thrownException.getMessage());					
	}
	
	@Test
	public void testExtendLoan_WhenLoanExistsButBookedByAnotherUser_ThrowsInvalidOperationException() {
		Loan mockLoanToExtend = mock(Loan.class);
		Article mockArticle = mock(Article.class);
		
		when(loanDaoMock.findById(1L)).thenReturn(mockLoanToExtend);
		when(mockLoanToExtend.getState()).thenReturn(LoanState.ACTIVE);
		when(mockLoanToExtend.getArticleOnLoan()).thenReturn(mockArticle);
		when(mockArticle.getState()).thenReturn(ArticleState.ONLOANBOOKED);
		
		Exception thrownException = assertThrows(InvalidOperationException.class, ()->{
			loanService.extendLoan(1L);
			
		});
		assertEquals("Cannot extend loan, another User has booked the Article!", thrownException.getMessage());					
	}
	
	@Test
	public void testExtendLoan_WhenLoanAlreadyRenewed_ThrowsInvalidOperationException() {
		Loan mockLoanToExtend = mock(Loan.class);
		Article mockArticle = mock(Article.class);
		
		when(loanDaoMock.findById(1L)).thenReturn(mockLoanToExtend);
		when(mockLoanToExtend.getArticleOnLoan()).thenReturn(mockArticle);
		when(mockLoanToExtend.isRenewed()).thenReturn(true);
		when(mockLoanToExtend.getState()).thenReturn(LoanState.ACTIVE);
		when(mockArticle.getState()).thenReturn(ArticleState.ONLOAN);
		
		Exception thrownException = assertThrows(InvalidOperationException.class, ()->{
			loanService.extendLoan(1L);
			
		});
		assertEquals("Cannot extend loan, loan has already been renewed!", thrownException.getMessage());					
	}

	
	@Test
	public void testExtendLoan_WhenLoanExistsAndNotBookedByAnotherUser_UpdatesDueDateAndSetsRenewedToTrue() {
		Loan mockLoanToExtend = mock(Loan.class);
		Article mockArticle = mock(Article.class);
		
		when(loanDaoMock.findById(1L)).thenReturn(mockLoanToExtend);
		when(mockLoanToExtend.getArticleOnLoan()).thenReturn(mockArticle);
		when(mockLoanToExtend.getState()).thenReturn(LoanState.ACTIVE);
		when(mockArticle.getState()).thenReturn(ArticleState.ONLOAN);
		
		loanService.extendLoan(1L);
		
		verify(mockLoanToExtend).setDueDate(today.plusMonths(1));
		verify(mockLoanToExtend).setRenewed(true);
	}
	
	@Test
	public void testCountLoansByUser_WhenUserDoesNotExist_ThrowsUserDoesNotExistException() {
		when(userDaoMock.findById(1L)).thenReturn(null);
		
		Exception thrownException = assertThrows(UserDoesNotExistException.class, ()->{
			loanService.countLoansByUser(1L);
			
		});
		assertEquals("Specified user is not registered in the system!", thrownException.getMessage());					
	}
	
	@Test
	public void testCountLoansByUser_WhenUserExists() {
		User mockUser = mock(User.class);
		
		when(userDaoMock.findById(1L)).thenReturn(mockUser);
		
		loanService.countLoansByUser(1L);
		
		verify(loanDaoMock).countLoans(mockUser, null);
	}
}
