package it.gurzu.swam.iLib.dao;

import java.util.List;

import it.gurzu.swam.iLib.model.Book;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class BookDao extends BaseDao<Book> {
	
	public BookDao() {
		super(Book.class);
	}
	
	public List<Book> findBooksByAuthor(String author) {
		return this.em.createQuery("FROM Book where author = :author", Book.class)
				.setParameter("author", author)
				.getResultList();		
	}

	public List<Book> findBooksByIsbn(String isbn) {
		return this.em.createQuery("FROM Book where isbn = :isbn", Book.class)
				.setParameter("isbn", isbn)
				.getResultList();		
	}

}
