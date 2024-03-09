package it.gurzu.swam.iLib.model;

import java.util.UUID;

public class ModelFactory {

	private ModelFactory() {}

	public static Book book() {
		return new Book(UUID.randomUUID().toString());
	}

	public static Magazine magazine() {
		return new Magazine(UUID.randomUUID().toString());
	}
	
	public static MovieDVD movieDVD() {
		return new MovieDVD(UUID.randomUUID().toString());
	}
	
	public static User user() {
		return new User(UUID.randomUUID().toString());
	}
	
	public static Booking booking() {
		return new Booking(UUID.randomUUID().toString());
	}
	
	public static Loan loan() {
		return new Loan(UUID.randomUUID().toString());
	}
	
	public static Notification notification() {
		return new Notification(UUID.randomUUID().toString());
	}
	
	public static Subscription subscription() {
		return new Subscription(UUID.randomUUID().toString());
	}
}