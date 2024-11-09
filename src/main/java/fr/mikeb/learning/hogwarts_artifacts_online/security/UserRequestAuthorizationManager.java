package fr.mikeb.learning.hogwarts_artifacts_online.security;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;

import java.util.function.Supplier;

/**
 * This class is responsible for checking if the user has the authority to access the requested user resource.
 * It checks if the user has the role "ROLE_user" and if the userId in the request URI matches the userId in the JWT.
 * If both conditions are met, the user is authorized to access the resource.
 * If not, the user is not authorized to access the resource.
 * This class is used in the SecurityConfiguration class to configure the authorization for /users/{userId}.
 */
@Component
public class UserRequestAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    private static final UriTemplate USER_URI_TEMPLATE = new UriTemplate("/users/{userId}");

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
        // Extract the userId from the request URI: /users/{userId}
        var uriVariables = USER_URI_TEMPLATE.match(context.getRequest().getRequestURI());
        var userIdFromRequestUri = uriVariables.get("userId");

        // Extract the userId from the Authentication object, which is a Jwt object
        var authentication = authenticationSupplier.get();
        var userIdFromJwt = ((Jwt) authentication.getPrincipal()).getClaim("userId").toString();

        // Check if the user has the role "ROLE_user"
        boolean hasUserRole = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_user"));

        // Check if the user has the role "ROLE_admin"
        boolean hasAdminRole = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_admin"));

        // Compare the two userIds.
        boolean userIdsMatch = userIdFromRequestUri != null && userIdFromRequestUri.equals(userIdFromJwt);

        return new AuthorizationDecision(hasAdminRole || (hasUserRole && userIdsMatch));
    }

}