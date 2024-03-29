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
	
	public List<Booking> findBookingsByUser(User user) {
		return this.em.createQuery("SELECT b FROM Booking b WHERE b.bookingUser = :user", Booking.class)
				.setParameter("user", user)
				.getResultList();
	}

    public List<Booking> findBookingsByArticle(Article article) {
        return this.em.createQuery("SELECT b FROM Booking b WHERE b.bookedArticle = :article", Booking.class)
                .setParameter("article", article)
                .getResultList();
    }
}
