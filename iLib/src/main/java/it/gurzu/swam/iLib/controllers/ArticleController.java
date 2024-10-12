package it.gurzu.swam.iLib.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import it.gurzu.swam.iLib.dto.ArticleDTO;
import it.gurzu.swam.iLib.dto.ArticleMapper;
import it.gurzu.swam.iLib.dto.PaginationResponse;
import it.gurzu.swam.iLib.exceptions.ArticleDoesNotExistException;
import it.gurzu.swam.iLib.exceptions.InvalidOperationException;
import it.gurzu.swam.iLib.exceptions.InvalidStateTransitionException;
import it.gurzu.swam.iLib.exceptions.SearchHasGivenNoResultsException;
import it.gurzu.swam.iLib.services.ArticleService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/articlesEndpoint")
public class ArticleController {

	@Inject
	private ArticleService articleService;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("ADMINISTRATOR")
	public Response createArticle(@Valid ArticleDTO articleDTO) {
		try {
			Long id = articleService.addArticle(articleDTO);
			return Response.status(Response.Status.CREATED)
					.entity("{\"articleId\": " + id + "}").build();
		} catch (IllegalArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("{\"error\": \"" + e.getMessage() +"\"}").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error\": \"An error occurred while registering the article.\"}").build();
		} 
		
	}

	@PUT
	@Path("/{articleId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("ADMINISTRATOR")
	public Response updateArticle(@PathParam("articleId") Long articleId, @Valid ArticleDTO articleDTO) {
		try {
			articleService.updateArticle(articleId, articleDTO);
			return Response.ok("{\"message\": \"Article updated successfully.\"}").build();
		} catch (ArticleDoesNotExistException ex) {
			return Response.status(Response.Status.NOT_FOUND)
					.entity("{\"error\": \"" + ex.getMessage() +"\"}").build();
		} catch (InvalidStateTransitionException | IllegalArgumentException ex) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("{\"error\": \"" + ex.getMessage() +"\"}").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error\": \"An error occurred while updating the article.\"}").build();
		} 
	}

	@GET
	@Path("/{articleId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getArticleInfo(@PathParam("articleId") Long articleId) {
		try {
			ArticleDTO articleDTO = articleService.getArticleInfoExtended(articleId);
			return Response.ok(articleDTO).build();
		} catch (ArticleDoesNotExistException e) {
			return Response.status(Response.Status.NOT_FOUND)
					.entity("{\"error\": \"" + e.getMessage() +"\"}").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error\": \"An error occurred while retrieving the article information.\"}").build();
		} 
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchArticles(@QueryParam("isbn") String isbn, @QueryParam("issn") String issn,
			@QueryParam("isan") String isan, @QueryParam("title") String title, @QueryParam("genre") String genre,
			@QueryParam("publisher") String publisher, @QueryParam("yearEdition") String yearEdition,
			@QueryParam("author") String author, @QueryParam("issueNumber") Integer issueNumber,
			@QueryParam("director") String director, @DefaultValue("1") @QueryParam("pageNumber") int pageNumber,
			@DefaultValue("10") @QueryParam("resultsPerPage") int resultsPerPage) {

    	if((pageNumber < 1) || (resultsPerPage < 0))
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("{\"error\": \"Pagination parameters incorrect!\"}").build();

    	LocalDate _yearEdition = null;
    	
		if (yearEdition != null) {
			try {
				_yearEdition = LocalDate.parse(yearEdition);
			} catch (DateTimeParseException e) {
				return Response.status(Response.Status.BAD_REQUEST)
						.entity("{\"error\": \"Invalid date format for 'yearEdition', expected format YYYY-MM-DD.\"}").build();
			}
		}
		try {
    		long totalResults = articleService.countArticles(isbn, issn, isan, title, genre, publisher, _yearEdition, author, issueNumber, director);
    		int totalPages = (int) Math.ceil((double) totalResults / resultsPerPage);

    		if(pageNumber > totalPages) {
    			pageNumber = totalPages == 0 ? 1 : totalPages;
    		}
    		
    		
    		int fromIndex = (pageNumber - 1) * resultsPerPage;

    		if(fromIndex < 0)
    			return Response.status(Response.Status.BAD_REQUEST)
						.entity("{\"error\": \"For strange reasons the fromIndex parameter is negative!\"}").build();
    			
			List<ArticleDTO> articleDTOs = articleService.searchArticles(isbn, issn, isan, title, genre,
					publisher, _yearEdition, author, issueNumber, director, fromIndex, resultsPerPage)
					.stream()
					.map(article -> ArticleMapper.toDTO(article, null, null))
					.collect(Collectors.toList());
					
            PaginationResponse<ArticleDTO> response = new PaginationResponse<>(
            		articleDTOs,
                    pageNumber,
                    resultsPerPage,
                    totalResults,
                    totalPages
            );

			return Response.ok(response).build();
		} catch (SearchHasGivenNoResultsException e) {
			return Response.status(Response.Status.NOT_FOUND)
					.entity("{\"error\": \"" + e.getMessage() +"\"}").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error\": \"An error occurred during article search.\"" + e.getMessage() +"\"}").build();
		} 
	}
	
    @DELETE
    @Path("/{articleId}")
    @Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("ADMINISTRATOR")
    public Response deleteArticle(@PathParam("articleId") Long articleId) {
        try {
            articleService.removeArticle(articleId);
			return Response.ok("{\"message\": \"Article deleted successfully.\"}").build();
		} catch (ArticleDoesNotExistException ex) {
			return Response.status(Response.Status.NOT_FOUND)
					.entity("{\"error\": \"" + ex.getMessage() +"\"}").build();
        } catch (InvalidOperationException ex) {
            return Response.status(Response.Status.BAD_REQUEST)
					.entity("{\"error\": \"" + ex.getMessage() +"\"}").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error\": \"An error occurred during article deletion.\"}").build();
		} 
    }
}
