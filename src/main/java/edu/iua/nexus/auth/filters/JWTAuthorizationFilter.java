package edu.iua.nexus.auth.filters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;

import edu.iua.nexus.auth.model.Role;
import edu.iua.nexus.auth.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtro que se ejecuta en cada request protegido para validar el JWT entrante.
 * Extrae el token desde header o query param, lo verifica con la clave compartida
 * y, si es válido, arma un {@link UsernamePasswordAuthenticationToken} con los roles del usuario.
 */
@Slf4j
public class JWTAuthorizationFilter extends BasicAuthenticationFilter {
	public JWTAuthorizationFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
	}

	/**
	 * Intercepta la petición y decide si debe intentar autenticar en base a los datos recibidos.
	 * Si no se provee token, la cadena de filtros continúa sin alterar el contexto de seguridad.
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		//defino x dónde voy a estar recibiendo mi token. SI es por header se precede del Bearer
		String header = req.getHeader(AuthConstants.AUTH_HEADER_NAME);
		String param = req.getParameter(AuthConstants.AUTH_PARAM_NAME);
		boolean byHeader = !(header == null || !header.startsWith(AuthConstants.TOKEN_PREFIX));
		boolean byParam = !(param == null || param.trim().length() < 10);
		
		// Si no se envía o es correcto el inicio de la cabecera o bien no se envía un
		// parámetro correcto, se continúa con el resto de los filtros
		if (!byHeader && !byParam) {
			chain.doFilter(req, res);
			return;
		}
		// Le damos prioridad al header.
		UsernamePasswordAuthenticationToken authentication = getAuthentication(req, byHeader);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(req, res);

	}

	// Extraer el token JWT de la cabecera y lo intenta validar
	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request, boolean byHeader) {
		// Recordar que el header inicia con alguna cadena, por ejemplo: 'Bearer '
		String token = byHeader
				? request.getHeader(AuthConstants.AUTH_HEADER_NAME).replace(AuthConstants.TOKEN_PREFIX, "")
				: request.getParameter(AuthConstants.AUTH_PARAM_NAME);

		if (token != null) {
			// Parseamos el token usando la librería
			DecodedJWT jwt=null;
			try {
				jwt = JWT.require(Algorithm.HMAC512(AuthConstants.SECRET.getBytes())).build().verify(token);
				log.trace("Token recibido por '{}'", byHeader ? "header" : "query param");
				log.trace("Usuario logueado: " + jwt.getSubject());
				log.trace("Roles: " + jwt.getClaim("roles"));
				log.trace("Custom JWT Version: " + jwt.getClaim("version").asString());
				
				
				Set<Role> roles=new HashSet<Role>();
				//authorities=roles para nosotros
				List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
				@SuppressWarnings("unchecked")
				List<String> rolesStr = (List<String>) jwt.getClaim("roles").as(List.class);
				authorities = rolesStr.stream().map(role -> new SimpleGrantedAuthority(role))
						.collect(Collectors.toList());
				// Creamos instancias Role mínimas para mantener compatibilidad con el modelo del dominio
				roles=rolesStr.stream().map(role-> new Role(role,0,role)).collect(Collectors.toSet());
				String username = jwt.getSubject();

				if (username != null) {
					User user = new User();
					user.setIdUser(jwt.getClaim("internalId").asLong());
					user.setUsername(username);
					user.setRoles(roles);
					user.setEmail(jwt.getClaim("email").asString());
					//el principal es el user
					return new UsernamePasswordAuthenticationToken(user, null, authorities);
				}
			} catch (TokenExpiredException e) {
				log.info("Token expirado: {}", e.getMessage());
			} catch (Exception e) {
				log.error("Error al validar token", e);
			}
			
			
			return null;
		}
		return null;
	}

}
