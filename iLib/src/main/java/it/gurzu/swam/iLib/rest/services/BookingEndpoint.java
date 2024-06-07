package it.gurzu.swam.iLib.rest.services;

import java.util.List;
import java.util.stream.Collectors;

import it.gurzu.swam.iLib.controllers.BookingController;
import it.gurzu.swam.iLib.dto.BookingDTO;
import it.gurzu.swam.iLib.dto.PaginationResponse;
import it.gurzu.swam.iLib.exceptions.ArticleDoesNotExistException;
import it.gurzu.swam.iLib.exceptions.BookingDoesNotExistException;
import it.gurzu.swam.iLib.exceptions.InvalidOperationException;
import it.gurzu.swam.iLib.exceptions.SearchHasGivenNoResultsException;
import it.gurzu.swam.iLib.exceptions.UserDoesNotExistException;
import it.gurzu.swam.iLib.rest.JWTUtil;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/bookingsEndpoint")
public class BookingEndpoint {

	@Inject
	private BookingController bookingController;
	

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response registerBooking(@Context SecurityContext securityContext, @QueryParam("userId") Long userId, @QueryParam("articleId") Long articleId) {
		if(userId == null)
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("{\"error\": \"Cannot register Booking, User not specified!\"}").build();
		if(articleId == null)
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("{\"error\": \"Cannot register Booking, Article not specified!\"}").build();

		Long loggedUserId = JWTUtil.getUserIdFromToken(securityContext.getUserPrincipal().getName());
		
		if(!securityContext.isUserInRole("ADMINISTRATOR") && !loggedUserId.equals(userId)) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		
		try {
			Long bookingId = bookingController.registerBooking(userId, articleId);
			return Response.status(Response.Status.CREATED)
					.entity("{\"bookingId\": " + bookingId + "}").build();
		} catch (UserDoesNotExistException | ArticleDoesNotExistException e) {
			return Response.status(Response.Status.NOT_FOUND)
					.entity("{\"error\": \"" + e.getMessage() + "\"}").build();
		} catch (InvalidOperationException e) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("{\"error\": \"" + e.getMessage() + "\"}").build();
		} catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error\": \"An error occurred while registering the booking.\"}").build();
        }
	}

	@GET
	@Path("/{bookingId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBookingInfo(@PathParam("bookingId") Long bookingId) {
		try {
			BookingDTO bookingDTO = bookingController.getBookingInfo(bookingId);
			return Response.ok(bookingDTO).build();
		} catch (BookingDoesNotExistException e) {
			return Response.status(Response.Status.NOT_FOUND)
					.entity("{\"error\": \"" + e.getMessage() + "\"}").build();
		} catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error\": \"An error occurred while retrieving the booking information.\"}").build();
        }
	}

	@PATCH
	@Path("/{bookingId}/cancel")
	@Produces(MediaType.APPLICATION_JSON)
	public Response cancelBooking(@Context SecurityContext securityContext, @PathParam("bookingId") Long bookingId) {
		Long loggedUserId = JWTUtil.getUserIdFromToken(securityContext.getUserPrincipal().getName());
		
		try {
			Long bookingUserId = bookingController.getBookingInfo(bookingId).getBookingUserId();
			
			if(!securityContext.isUserInRole("ADMINISTRATOR") && !loggedUserId.equals(bookingUserId)) {
				return Response.status(Response.Status.UNAUTHORIZED).build();
			}

			bookingController.cancelBooking(bookingId);
			return Response.ok("{\"message\": \"Booking cancelled successfully.\"}").build();
		} catch (BookingDoesNotExistException e) {
			return Response.status(Response.Status.NOT_FOUND)
					.entity("{\"error\": \"" + e.getMessage() + "\"}").build();
		} catch (InvalidOperationException e) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("{\"error\": \"" + e.getMessage() + "\"}").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error\": \"An error occurred while attempting to cancel the booking.\"}").build();
		}
	}

	
	@GET
	@Path("/{userId}/bookings")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBookedArticlesByUser(@Context SecurityContext securityContext, @PathParam("userId") Long userId,
			@QueryParam("pageNumber") @DefaultValue("1") int pageNumber,
			@QueryParam("resultsPerPage") @DefaultValue("10") int resultsPerPage) {

		if((pageNumber < 1) || (resultsPerPage < 0))
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("{\"error\": \"Pagination parameters incorrect!\"}").build();

		Long loggedUserId = JWTUtil.getUserIdFromToken(securityContext.getUserPrincipal().getName());
		if(!securityContext.isUserInRole("ADMINISTRATOR") && !loggedUserId.equals(userId)) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		try {
    		long totalResults = bookingController.countBookingsByUser(userId);
    		int totalPages = (int) Math.ceil((double) totalResults / resultsPerPage);
    		
    		if(pageNumber > totalPages) {
    			pageNumber = totalPages == 0 ? 1 : totalPages;
    		}
    		
    		int fromIndex = (pageNumber - 1) * resultsPerPage;

			List<BookingDTO> bookingDTOs = bookingController.getBookingsByUser(userId, fromIndex, resultsPerPage)
															.stream()
															.map(BookingDTO::new)
															.collect(Collectors.toList());
			
            PaginationResponse<BookingDTO> response = new PaginationResponse<>(
            		bookingDTOs,
                    pageNumber,
                    resultsPerPage,
                    totalResults,
                    totalPages
            );

			return Response.ok(response).build();
		} catch (UserDoesNotExistException | SearchHasGivenNoResultsException e) {
			return Response.status(Response.Status.NOT_FOUND)
					.entity("{\"error\": \"" + e.getMessage() + "\"}").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error\": \"An error occurred while retrieving user bookings.\"}").build();
		}
	}
}
