package edu.cit.stathis.common.config;

import edu.cit.stathis.auth.service.CustomUserDetailsService;
import edu.cit.stathis.common.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfigurer implements WebSocketMessageBrokerConfigurer {

  @Autowired private JwtUtil jwtUtil;

  @Autowired private CustomUserDetailsService userDetailsService;

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new JwtChannelInterceptor(jwtUtil, userDetailsService));
  }
}
