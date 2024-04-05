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
	public void testFindBookByIsbn() {
		List<Book> retrievedBooks = bookDao.findBooksByIsbn("1234567");
		assertEquals(1, retrievedBooks.size());
		assertEquals(true, retrievedBooks.contains(book));
	}
}