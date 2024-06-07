package it.gurzu.swam.iLib.rest.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import it.gurzu.swam.iLib.controllers.UserController;
import it.gurzu.swam.iLib.dto.LoginDTO;
import it.gurzu.swam.iLib.exceptions.SearchHasGivenNoResultsException;
import it.gurzu.swam.iLib.model.User;
import it.gurzu.swam.iLib.rest.JWTUtil;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
public class AuthenticationEndpoint {

	@Inject
	UserController userController;

	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response loginUser(@Valid LoginDTO credentials) {
		try {
			User user = userController.searchUsers(credentials.getEmail(), null, null, null, 0, 1).get(0);
			if (!new BCryptPasswordEncoder().matches(credentials.getPassword(), user.getPassword()))
				return Response.status(Response.Status.UNAUTHORIZED)
						.entity("{\"error\": \"" + "Credentials are invalid." + "\"}").build();
			
			String token = JWTUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
			
			return Response.ok("{\"token\": \"" + token + "\"}").build();
		} catch (SearchHasGivenNoResultsException e) {
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity("{\"error\": \"" + "Credentials are invalid." + "\"}").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error\": \"An error occurred during user search.\"}").build();
		} 
	}
}
