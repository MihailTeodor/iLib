package it.gurzu.SWAM.iLib.controllerTest;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import org.apache.commons.lang3.reflect.FieldUtils;

import it.gurzu.swam.iLib.model.Article;
import it.gurzu.swam.iLib.model.ArticleState;
import it.gurzu.swam.iLib.model.Book;
import it.gurzu.swam.iLib.model.Booking;
import it.gurzu.swam.iLib.model.BookingState;
import it.gurzu.swam.iLib.model.Loan;
import it.gurzu.swam.iLib.model.LoanState;
import it.gurzu.swam.iLib.model.Magazine;
import it.gurzu.swam.iLib.model.ModelFactory;
import it.gurzu.swam.iLib.model.MovieDVD;
import it.gurzu.swam.iLib.model.User;
import it.gurzu.swam.iLib.model.UserRole;
import it.gurzu.swam.iLib.utils.PasswordUtils;

public class QueryUtils {

	public static void queryTruncateAll(Connection connection) throws SQLException {
		Statement statement = connection.createStatement();
		
		String query1 = "SET FOREIGN_KEY_CHECKS = 0";
		String query2 = "TRUNCATE users";
		String query3 = "TRUNCATE bookings";
		String query4 = "TRUNCATE loans";
		String query5 = "TRUNCATE books";
		String query6 = "TRUNCATE magazines";
		String query7 = "TRUNCATE movies_DVD";
		String query8 = "SET FOREIGN_KEY_CHECKS = 1";
		
		statement.addBatch(query1);
		statement.addBatch(query2);
		statement.addBatch(query3);
		statement.addBatch(query4);
		statement.addBatch(query5);
		statement.addBatch(query6);
		statement.addBatch(query7);
		statement.addBatch(query8);
		
		statement.executeBatch();
		statement.close();

	}
	
	
	
	public static User queryCreateUser(Connection connection, Long id, String email, String password, String name, String surname, String address, String telephoneNumber, UserRole role) throws SQLException, IllegalAccessException {
		User user = ModelFactory.user();
		
		user.setEmail(email);
		user.setPassword(PasswordUtils.hashPassword(password));
		user.setName(name);
		user.setSurname(surname);
		user.setAddress(address);
		user.setTelephoneNumber(telephoneNumber);
		user.setRole(role);
		
		FieldUtils.writeField(user, "id", id, true);

		
		String query = "INSERT INTO users(id, uuid, email, password, name, surname, address, telephoneNumber, role) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setLong(1, user.getId());
		statement.setString(2, user.getUuid());
		statement.setString(3, user.getEmail());
		statement.setString(4, user.getPassword());
		statement.setString(5, user.getName());
		statement.setString(6, user.getSurname());
		statement.setString(7, user.getAddress());
		statement.setString(8, user.getTelephoneNumber());
		statement.setString(9, user.getRole().toString());
		
		statement.executeUpdate();
		statement.close();

		return user;
	}
	
	
	
	public static Book queryCreateBook(Connection connection, Long id, String location, String title, LocalDate yearEdition, String publisher, String genre, String description, ArticleState state, String author, String isbn) throws SQLException, IllegalAccessException {
		Book book = ModelFactory.book();
		
		book.setLocation(location);
		book.setTitle(title);
		book.setYearEdition(yearEdition);
		book.setPublisher(publisher);
		book.setGenre(genre);
		book.setDescription(description);
		book.setState(state);
		book.setAuthor(author);
		book.setIsbn(isbn);
		
		FieldUtils.writeField(book, "id", id, true);

		
		String query = "INSERT INTO books(id, uuid, location, title, yearEdition, publisher, genre, description, state, author, isbn) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setLong(1, book.getId());
		statement.setString(2, book.getUuid());
		statement.setString(3, book.getLocation());
		statement.setString(4, book.getTitle());
		statement.setDate(5, Date.valueOf(book.getYearEdition()));
		statement.setString(6, book.getPublisher());
		statement.setString(7, book.getGenre());
		statement.setString(8, book.getDescription());
		statement.setString(9, book.getState().toString());
		statement.setString(10, book.getAuthor());
		statement.setString(11, book.getIsbn());
		
		statement.executeUpdate();
		statement.close();
		
		return book;
	}
	
	
	
	public static Magazine queryCreateMagazine(Connection connection, Long id, String location, String title, LocalDate yearEdition, String publisher, String genre, String description, ArticleState state, int issueNumber, String issn) throws SQLException, IllegalAccessException {
		Magazine magazine = ModelFactory.magazine();

		magazine.setLocation(location);
		magazine.setTitle(title);
		magazine.setYearEdition(yearEdition);
		magazine.setPublisher(publisher);
		magazine.setGenre(genre);
		magazine.setDescription(description);
		magazine.setState(state);
		magazine.setIssueNumber(issueNumber);
		magazine.setIssn(issn);
		
		FieldUtils.writeField(magazine, "id", id, true);


		String query = "INSERT INTO magazines(id, uuid, location, title, yearEdition, publisher, genre, description, state, issueNumber, issn) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement statement = connection.prepareStatement(query);
		statement.setLong(1, magazine.getId());
		statement.setString(2, magazine.getUuid());
		statement.setString(3, magazine.getLocation());
		statement.setString(4, magazine.getTitle());
		statement.setDate(5, Date.valueOf(magazine.getYearEdition()));
		statement.setString(6, magazine.getPublisher());
		statement.setString(7, magazine.getGenre());
		statement.setString(8, magazine.getDescription());
		statement.setString(9, magazine.getState().toString());
		statement.setLong(10, magazine.getIssueNumber());
		statement.setString(11, magazine.getIssn());
		
		statement.executeUpdate();
		statement.close();
		
		return magazine;
	}

	
	
	public static MovieDVD queryCreateMovieDVD(Connection connection, Long id, String location, String title, LocalDate yearEdition, String publisher, String genre, String description, ArticleState state, String director, String isan) throws SQLException, IllegalAccessException {
		MovieDVD movieDVD = ModelFactory.movieDVD();
		
		movieDVD.setLocation(location);
		movieDVD.setTitle(title);
		movieDVD.setYearEdition(yearEdition);
		movieDVD.setPublisher(publisher);
		movieDVD.setGenre(genre);
		movieDVD.setDescription(description);
		movieDVD.setState(state);
		movieDVD.setDirector(director);
		movieDVD.setIsan(isan);
		
		FieldUtils.writeField(movieDVD, "id", id, true);


		String query = "INSERT INTO movies_DVD(id, uuid, location, title, yearEdition, publisher, genre, description, state, director, isan) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement statement = connection.prepareStatement(query);
		statement.setLong(1, movieDVD.getId());
		statement.setString(2, movieDVD.getUuid());
		statement.setString(3, movieDVD.getLocation());
		statement.setString(4, movieDVD.getTitle());
		statement.setDate(5, Date.valueOf(movieDVD.getYearEdition()));
		statement.setString(6, movieDVD.getPublisher());
		statement.setString(7, movieDVD.getGenre());
		statement.setString(8, movieDVD.getDescription());
		statement.setString(9, movieDVD.getState().toString());
		statement.setString(10, movieDVD.getDirector());
		statement.setString(11, movieDVD.getIsan());
		
		statement.executeUpdate();
		statement.close();
		
		return movieDVD;
	}
	
	
	
	public static Booking queryCreateBooking(Connection connection, Long id, LocalDate bookingDate, LocalDate bookingEndDate, BookingState state, Article bookedArtile, User bookingUser) throws SQLException, IllegalAccessException {
		Booking booking = ModelFactory.booking();
		
		booking.setBookingDate(bookingDate);
		booking.setBookingEndDate(bookingEndDate);
		booking.setState(state);
		booking.setBookedArticle(bookedArtile);
		booking.setBookingUser(bookingUser);
		
		FieldUtils.writeField(booking, "id", id, true);

		
		String query = "INSERT INTO bookings(id, uuid, bookingDate, bookingEndDate, state, bookedArticle_id, bookingUser_id) VALUES(?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement statement = connection.prepareStatement(query);
		statement.setLong(1, booking.getId());
		statement.setString(2, booking.getUuid());
		statement.setDate(3, Date.valueOf(booking.getBookingDate()));
		statement.setDate(4, Date.valueOf(booking.getBookingEndDate()));
		statement.setString(5, booking.getState().toString());
		statement.setLong(6, booking.getBookedArticle().getId());
		statement.setLong(7, booking.getBookingUser().getId());
		
		statement.executeUpdate();
		statement.close();
		
		return booking;
		
	}

	public static Loan queryCreateLoan(Connection connection, Long id, LocalDate loanDate, LocalDate dueDate, LoanState state, boolean renewed, Article articleOnLoan, User loaningUser) throws SQLException, IllegalAccessException {
		Loan loan = ModelFactory.loan();
		
		loan.setLoanDate(loanDate);
		loan.setDueDate(dueDate);
		loan.setState(state);
		loan.setRenewed(renewed);
		loan.setArticleOnLoan(articleOnLoan);
		loan.setLoaningUser(loaningUser);
		
		FieldUtils.writeField(loan, "id", id, true);

		
		String query = "INSERT INTO loans(id, uuid, loanDate, dueDate, state, renewed, articleOnLoan_id, loaningUser_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setLong(1, loan.getId());
		statement.setString(2, loan.getUuid());
		statement.setDate(3, Date.valueOf(loan.getLoanDate()));
		statement.setDate(4, Date.valueOf(loan.getDueDate()));
		statement.setString(5, loan.getState().toString());
		statement.setBoolean(6, loan.isRenewed());
		statement.setLong(7, loan.getArticleOnLoan().getId());
		statement.setLong(8, loan.getLoaningUser().getId());
		
		statement.executeUpdate();
		statement.close();

		return loan;
	}
}
