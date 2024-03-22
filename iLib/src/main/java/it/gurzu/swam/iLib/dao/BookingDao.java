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
	
	public List<Booking> findBookingsByUser(User user){
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
