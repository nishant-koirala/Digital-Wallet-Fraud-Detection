package dev.nishanta.wallet.config;

import dev.nishanta.wallet.security.JwtUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Collections;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;

    public WebSocketConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null) {
                    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                        String token = null;
                        
                        // First check cookies for accessToken
                        String cookieHeader = accessor.getFirstNativeHeader("cookie");
                        if (cookieHeader != null) {
                            String[] cookies = cookieHeader.split(";");
                            for (String cookie : cookies) {
                                if (cookie.trim().startsWith("accessToken=")) {
                                    token = cookie.trim().substring("accessToken=".length());
                                    break;
                                }
                            }
                        }
                        
                        // Fallback to Authorization header
                        String authHeader = accessor.getFirstNativeHeader("Authorization");
                        if (token == null && authHeader != null && authHeader.startsWith("Bearer ")) {
                            token = authHeader.substring(7);
                        }

                        if (token != null) {
                            try {
                                String username = jwtUtil.extractUsername(token);
                                String role = jwtUtil.extractRole(token);
                                String walletId = jwtUtil.extractWalletId(token);
                                if (username != null && jwtUtil.validateToken(token, username)) {
                                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                            username, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
                                    accessor.setUser(auth);
                                    if (accessor.getSessionAttributes() == null) {
                                        accessor.setSessionAttributes(new java.util.concurrent.ConcurrentHashMap<>());
                                    }
                                    accessor.getSessionAttributes().put("walletId", walletId);
                                }
                            } catch (Exception e) {
                                System.err.println("WebSocket JWT Validation failed: " + e.getMessage());
                            }
                        }
                    } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                        String destination = accessor.getDestination();
                        if (destination != null && destination.startsWith("/topic/notifications/")) {
                            String requestedWalletId = destination.substring("/topic/notifications/".length());
                            Object sessionWalletId = accessor.getSessionAttributes() != null ? 
                                                     accessor.getSessionAttributes().get("walletId") : null;
                            if (sessionWalletId == null || !sessionWalletId.toString().equals(requestedWalletId)) {
                                throw new IllegalArgumentException("Unauthorized subscription destination");
                            }
                        }
                    }
                }
                return message;
            }
        });
    }
}
