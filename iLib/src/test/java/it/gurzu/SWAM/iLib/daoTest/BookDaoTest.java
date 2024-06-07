package it.gurzu.SWAM.iLib.daoTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;

import it.gurzu.swam.iLib.dao.BookDao;
import it.gurzu.swam.iLib.model.Book;
import it.gurzu.swam.iLib.model.ModelFactory;

public class BookDaoTest extends JPATest {

	private Book book;
	private BookDao bookDao;
	
	@Override
	protected void init() throws IllegalAccessException {
		book = ModelFactory.book();
		book.setIsbn("1234567");
		
		em.persist(book);
		
		bookDao = new BookDao();
		FieldUtils.writeField(bookDao, "em", em, true); 
	}
	
	@Test
	public void testFindBooksByIsbn() {
		List<Book> retrievedBooks = bookDao.findBooksByIsbn("1234567");
		assertEquals(1, retrievedBooks.size());
		assertEquals(true, retrievedBooks.contains(book));
	}
	
	@Test
	public void testCountBooksByIsbn() {
		Book book2 = ModelFactory.book();
		book2.setIsbn("1234567");
		
		em.persist(book2);

		Book book3 = ModelFactory.book();
		book3.setIsbn("1234567");
		
		em.persist(book3);
		
		Long resultsNumber = bookDao.countBooksByIsbn("1234567");
		
		assertEquals(3, resultsNumber);

	}
}
