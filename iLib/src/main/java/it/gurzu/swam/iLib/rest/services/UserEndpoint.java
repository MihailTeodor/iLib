package it.gurzu.swam.iLib.rest.services;


import java.util.List;
import java.util.stream.Collectors;

import it.gurzu.swam.iLib.controllers.UserController;
import it.gurzu.swam.iLib.dto.PaginationResponse;
import it.gurzu.swam.iLib.dto.UserDTO;
import it.gurzu.swam.iLib.dto.UserDashboardDTO;
import it.gurzu.swam.iLib.exceptions.SearchHasGivenNoResultsException;
import it.gurzu.swam.iLib.exceptions.UserDoesNotExistException;
import it.gurzu.swam.iLib.rest.JWTUtil;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/usersEndpoint")
public class UserEndpoint {

	@Inject
	private UserController userController;
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserInfo(@Context SecurityContext securityContext, @PathParam("id") Long userId) {
		Long loggedUserId = JWTUtil.getUserIdFromToken(securityContext.getUserPrincipal().getName());
		if(!securityContext.isUserInRole("ADMINISTRATOR") && !loggedUserId.equals(userId)) {
			return Response.status(Response.Status.FORBIDDEN).build();
		}
		
		try {
			UserDashboardDTO userDto = userController.getUserInfoExtended(userId);
			return Response.ok(userDto).build();
		} catch (UserDoesNotExistException ex) {
			return Response.status(Response.Status.NOT_FOUND)
					.entity("{\"error\": \"" + ex.getMessage() +"\"}").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error\": \"An error occurred while retrieving the user information.\"}").build();
		}
	}

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("ADMINISTRATOR")
    public Response createUser(@Valid UserDTO userDTO) {
    	try {
    		Long id = userController.addUser(userDTO);
    		return Response.status(Response.Status.CREATED)
    				.entity("{\"userId\": " + id + "}").build();
		} catch (IllegalArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("{\"error\": \"" + e.getMessage() +"\"}").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error\": \"An error occurred while registering the user.\"}").build();
		} 
    }
    
    
    @PUT
    @Path("/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@Context SecurityContext securityContext, @PathParam("userId") Long userId, @Valid UserDTO userDTO) {
		Long loggedUserId = JWTUtil.getUserIdFromToken(securityContext.getUserPrincipal().getName());
		if(!securityContext.isUserInRole("ADMINISTRATOR") && !loggedUserId.equals(userId)) {
			return Response.status(Response.Status.FORBIDDEN).build();
		}

    	try {
            userController.updateUser(userId, userDTO);
			return Response.ok("{\"message\": \"User updated successfully.\"}").build();
        } catch (UserDoesNotExistException e) {
			return Response.status(Response.Status.NOT_FOUND)
					.entity("{\"error\": \"" + e.getMessage() +"\"}").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error\": \"An error occurred while updating the user.\"}").build();
		} 
    }    
    

    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("ADMINISTRATOR")
    public Response searchUsers(@QueryParam("email") String email,
                                @QueryParam("name") String name,
                                @QueryParam("surname") String surname,
                                @QueryParam("telephoneNumber") String telephoneNumber,
                                @DefaultValue("1") @QueryParam("pageNumber") int pageNumber,
                                @DefaultValue("10") @QueryParam("resultsPerPage") int resultsPerPage) {

    	if((pageNumber < 1) || (resultsPerPage < 0))
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("{\"error\": \"Pagination parameters incorrect!\"}").build();

    	try {
    		long totalResults = userController.countUsers(email, name, surname, telephoneNumber);
    		int totalPages = (int) Math.ceil((double) totalResults / resultsPerPage);
    		
    		if(pageNumber > totalPages) {
    			pageNumber = totalPages == 0 ? 1 : totalPages;
    		}
    		
    		int fromIndex = (pageNumber - 1) * resultsPerPage;
    		List<UserDTO> userDTOs = userController.searchUsers(email, name, surname, telephoneNumber, fromIndex, resultsPerPage)
            									   .stream()
            									   .map(UserDTO::new)
            									   .collect(Collectors.toList());

            PaginationResponse<UserDTO> response = new PaginationResponse<>(
                    userDTOs,
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
					.entity("{\"error\": \"An error occurred during user search.\"}").build();
		} 
    }

}
