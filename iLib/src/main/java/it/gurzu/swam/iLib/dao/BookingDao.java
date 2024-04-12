package it.gurzu.swam.iLib.dao;

import java.util.List;

import it.gurzu.swam.iLib.model.Article;
import it.gurzu.swam.iLib.model.Booking;
import it.gurzu.swam.iLib.model.User;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class BookingDao extends BaseDao<Booking> {
	
	public BookingDao() {
		super(Booking.class);
	}
	
	public User getUserFromBooking(Booking booking) {
		return this.em.createQuery("SELECT b.bookingUser FROM Booking b WHERE b.id = :bookingId", User.class)
				.setParameter("bookingId", booking.getId())
				.getSingleResult();
	}

	public Article getArticleFromBooking(Booking booking) {
		return this.em.createQuery("SELECT b.bookedArticle FROM Booking b WHERE b.id = :bookingId", Article.class)
				.setParameter("bookingId", booking.getId())
				.getSingleResult();
	}
	    
    public List<Booking> searchBookings(User bookingUser, Article bookedArticle) {
    	return this.em.createQuery("SELECT b FROM Booking b WHERE"
    			+ "(:bookedArticle is null or bookedArticle = :bookedArticle) and"
    			+ "(:bookingUser is null or bookingUser = :bookingUser)"
    			+ "ORDER BY b.state, b.bookingEndDate DESC", Booking.class)
    			.setParameter("bookedArticle", bookedArticle)
    			.setParameter("bookingUser", bookingUser)
    			.getResultList();
    }
}
