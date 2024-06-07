package it.gurzu.swam.iLib.rest;

import java.security.Principal;

import it.gurzu.swam.iLib.dao.UserDao;
import it.gurzu.swam.iLib.model.User;
import it.gurzu.swam.iLib.model.UserRole;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.SecurityContext;

public class ILibSecurityContext implements SecurityContext {

	private String token;
	private String principalUserEmail;
	private ContainerRequestContext requestContext;
	private UserDao userDao;

	public ILibSecurityContext(String token, String principalUserEmail, ContainerRequestContext requestContext, UserDao userDao) {
		this.token = token;
		this.principalUserEmail = principalUserEmail;
		this.requestContext = requestContext;
		this.userDao = userDao;
	}
	
	@Override
	public boolean isUserInRole(String role) {
		User user = userDao.findUsersByEmail(principalUserEmail);
		if (user == null)
			return false;

		try {
			if (user.getRole() == UserRole.valueOf(role.toUpperCase()))
				return true;
		} catch (IllegalArgumentException | NullPointerException e) {
			return false;
		}
		return false;
	}

	@Override
	public boolean isSecure() {
		return requestContext.getSecurityContext().isSecure();
	}

	@Override
	public Principal getUserPrincipal() {
		return new Principal() {
			@Override
			public String getName() {
				return token;
			}
		};
	}

	@Override
	public String getAuthenticationScheme() {
		return SecurityContext.CLIENT_CERT_AUTH;
	}
}
