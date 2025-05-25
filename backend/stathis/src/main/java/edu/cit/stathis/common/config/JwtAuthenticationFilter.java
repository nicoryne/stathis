package edu.cit.stathis.common.config;

import edu.cit.stathis.auth.service.CustomUserDetailsService;
import edu.cit.stathis.common.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

  @Autowired private JwtUtil jwtUtil;

  @Autowired private CustomUserDetailsService userDetailsService;

  public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
    this.jwtUtil = jwtUtil;
    this.userDetailsService = userDetailsService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
        try {
    String authHeader = request.getHeader("Authorization");
            logger.debug("Authorization header: {}", authHeader);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.debug("No valid Authorization header found");
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7).trim();
            logger.debug("Extracted token: {}", token);

            if (token.isEmpty()) {
                logger.debug("Empty token after trimming");
                filterChain.doFilter(request, response);
                return;
            }

            String username = jwtUtil.extractUsername(token);
            logger.debug("Extracted username: {}", username);

    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);
      if (jwtUtil.validateToken(token, username)) {
                    String role = jwtUtil.extractClaim(token, claims -> claims.get("role", String.class));
                    if (!role.startsWith("ROLE_")) {
                        role = "ROLE_" + role;
                    }
                    logger.debug("Role from token: {}", role);
                    logger.debug("User authorities: {}", userDetails.getAuthorities());

        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority(role)));
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Authentication set in context: {}", 
                        SecurityContextHolder.getContext().getAuthentication());
      }
            }
        } catch (Exception e) {
            logger.error("Error processing JWT token: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
    }

    filterChain.doFilter(request, response);
  }
}
