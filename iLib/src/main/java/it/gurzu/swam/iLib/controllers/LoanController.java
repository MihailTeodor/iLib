package it.gurzu.swam.iLib.controllers;

import java.util.List;
import java.util.stream.Collectors;

import it.gurzu.swam.iLib.dto.LoanDTO;
import it.gurzu.swam.iLib.dto.PaginationResponse;
import it.gurzu.swam.iLib.exceptions.ArticleDoesNotExistException;
import it.gurzu.swam.iLib.exceptions.InvalidOperationException;
import it.gurzu.swam.iLib.exceptions.LoanDoesNotExistException;
import it.gurzu.swam.iLib.exceptions.UserDoesNotExistException;
import it.gurzu.swam.iLib.rest.JWTUtil;
import it.gurzu.swam.iLib.services.LoanService;
import jakarta.annotation.security.RolesAllowed;
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

@Path("/loansEndpoint")
public class LoanController {

	@Inject
	private LoanService loanService;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("ADMINISTRATOR")
	public Response registerLoan(@QueryParam("userId") Long userId, @QueryParam("articleId") Long articleId) {
		if(userId == null)
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("{\"error\": \"Cannot register Loan, User not specified!\"}").build();
		if(articleId == null)
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("{\"error\": \"Cannot register Loan, Article not specified!\"}").build();
		try {
			Long loanId = loanService.registerLoan(userId, articleId);
			return Response.status(Response.Status.CREATED)
					.entity("{\"loanId\": " + loanId + "}").build();
		} catch (UserDoesNotExistException | ArticleDoesNotExistException e) {
			return Response.status(Response.Status.NOT_FOUND)
					.entity("{\"error\": \"" + e.getMessage() + "\"}").build();
		} catch (InvalidOperationException e) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("{\"error\": \"" + e.getMessage() + "\"}")
					.build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error\": \"An error occurred while registering the loan.\"}").build();
		}
	}

	@PATCH
	@Path("/{loanId}/return")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("ADMINISTRATOR")
	public Response registerReturn(@PathParam("loanId") Long loanId) {
		try {
			loanService.registerReturn(loanId);
			return Response.ok("{\"message\": \"Loan successfully returned.\"}").build();
		} catch (LoanDoesNotExistException e) {
			return Response.status(Response.Status.NOT_FOUND)
					.entity("{\"error\": \"" + e.getMessage() + "\"}").build();
		} catch (InvalidOperationException e) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("{\"error\": \"" + e.getMessage() + "\"}").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error\": \"An error occurred while returning the loan.\"}").build();
		}
	}

	@GET
	@Path("/{loanId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLoanInfo(@PathParam("loanId") Long loanId) {
		try {
			LoanDTO loanDTO = loanService.getLoanInfo(loanId);
			return Response.ok(loanDTO).build();
		} catch (LoanDoesNotExistException e) {
			return Response.status(Response.Status.NOT_FOUND)
					.entity("{\"error\": \"" + e.getMessage() + "\"}").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error\": \"An unexpected error occurred while retrieving the loan information.\"}").build();
		}
	}

	@GET
	@Path("/{userId}/loans")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLoansByUser(@Context SecurityContext securityContext, @PathParam("userId") Long userId, @QueryParam("pageNumber") @DefaultValue("1") int pageNumber,
			@QueryParam("resultsPerPage") @DefaultValue("10") int resultsPerPage) {
		
		if((pageNumber < 1) || (resultsPerPage < 0))
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("{\"error\": \"Pagination parameters incorrect!\"}").build();

		Long loggedUserId = JWTUtil.getUserIdFromToken(securityContext.getUserPrincipal().getName());
		if(!securityContext.isUserInRole("ADMINISTRATOR") && !loggedUserId.equals(userId)) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		try {
    		long totalResults = loanService.countLoansByUser(userId);
    		int totalPages = (int) Math.ceil((double) totalResults / resultsPerPage);
    		
    		if(pageNumber > totalPages) {
    			pageNumber = totalPages == 0 ? 1 : totalPages;
    		}
    		
    		int fromIndex = (pageNumber - 1) * resultsPerPage;

			List<LoanDTO> loansDTOs = loanService.getLoansByUser(userId, fromIndex, resultsPerPage)
												.stream()
												.map(LoanDTO::new)
												.collect(Collectors.toList());
			
            PaginationResponse<LoanDTO> response = new PaginationResponse<>(
            		loansDTOs,
                    pageNumber,
                    resultsPerPage,
                    totalResults,
                    totalPages
            );

			return Response.ok(response).build();
		} catch (UserDoesNotExistException | LoanDoesNotExistException e) {
			return Response.status(Response.Status.NOT_FOUND)
					.entity("{\"error\": \"" + e.getMessage() + "\"}").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error\": \"An unexpected error occurred while retrieving user loans.\"}").build();
		}
	}
	
    @PATCH
    @Path("/{loanId}/extend")
    @Produces(MediaType.APPLICATION_JSON)
    public Response extendLoan(@Context SecurityContext securityContext, @PathParam("loanId") Long loanId) {
		Long loggedUserId = JWTUtil.getUserIdFromToken(securityContext.getUserPrincipal().getName());

        try {
        	Long loaningUserId = loanService.getLoanInfo(loanId).getLoaningUserId();
        	if(!securityContext.isUserInRole("ADMINISTRATOR") && !loggedUserId.equals(loaningUserId)) {
        		return Response.status(Response.Status.UNAUTHORIZED).build();
        	}
            loanService.extendLoan(loanId);
            return Response.ok("{\"message\": \"Loan extended successfully.\"}").build();
        } catch (LoanDoesNotExistException e) {
            return Response.status(Response.Status.NOT_FOUND)
            		.entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        } catch (InvalidOperationException e) {
            return Response.status(Response.Status.BAD_REQUEST)
            		.entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            		.entity("{\"error\": \"An unexpected error occurred.\"}").build();
        }
    }
}
