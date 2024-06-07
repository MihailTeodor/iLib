package it.gurzu.swam.iLib.dto;

import java.time.LocalDate;

import it.gurzu.swam.iLib.model.Booking;
import it.gurzu.swam.iLib.model.BookingState;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

public class BookingDTO {

    private Long id;
    
    @NotNull(message = "Booked Article ID is required")
    private Long bookedArticleId;
    private String bookedArticleTitle;

    @NotNull(message = "Booking User ID is required")
    private Long bookingUserId;

    @NotNull(message = "Booking date cannot be null")
    @PastOrPresent(message = "Booking date cannot be in the future")
    private LocalDate bookingDate;

    @NotNull(message = "Booking end date cannot be null")
    private LocalDate bookingEndDate;
    private BookingState state;

    public BookingDTO() {}

    public BookingDTO(Booking booking) {
    	this.id = booking.getId();
    	this.bookedArticleId = booking.getBookedArticle().getId();
    	this.bookedArticleTitle = booking.getBookedArticle().getTitle();
    	this.bookingUserId = booking.getBookingUser().getId();
    	this.bookingDate = booking.getBookingDate();
    	this.bookingEndDate = booking.getBookingEndDate();
    	this.state = booking.getState();
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookedArticleId() {
        return bookedArticleId;
    }

    public void setBookedArticleId(Long bookedArticleId) {
        this.bookedArticleId = bookedArticleId;
    }

    public String getBookedArticleTitle() {
        return bookedArticleTitle;
    }

    public void setBookedArticleTitle(String bookedArticleTitle) {
        this.bookedArticleTitle = bookedArticleTitle;
    }

    public Long getBookingUserId() {
        return bookingUserId;
    }

    public void setBookingUserId(Long bookingUserId) {
        this.bookingUserId = bookingUserId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalDate getBookingEndDate() {
        return bookingEndDate;
    }

    public void setBookingEndDate(LocalDate bookingEndDate) {
        this.bookingEndDate = bookingEndDate;
    }

    public BookingState getState() {
        return state;
    }

    public void setState(BookingState state) {
        this.state = state;
    }
}
