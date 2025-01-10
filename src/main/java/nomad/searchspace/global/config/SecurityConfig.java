package nomad.searchspace.global.config;

import lombok.RequiredArgsConstructor;
import nomad.searchspace.domain.member.service.CustomOAuth2UserService;
import nomad.searchspace.domain.member.service.RedisService;
import nomad.searchspace.global.auth.OAuth2SuccessHandler;
import nomad.searchspace.global.filter.JwtFilter;
import nomad.searchspace.global.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;


@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Value("${server.url}")
    private String serverUrl;

    private final JwtUtil jwtUtil;

    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    private final CustomOAuth2UserService customOAuth2UserService;

    private final RedisService redisService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(corsCustomizer -> corsCustomizer.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(new JwtFilter(jwtUtil, redisService), UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(customOAuth2UserService)));

        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(new AntPathRequestMatcher("/member/update")).hasRole("USER")
                        .requestMatchers(new AntPathRequestMatcher("/api/member/reissue")).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/scraps/").hasRole("USER")
                        .requestMatchers(new AntPathRequestMatcher("/post/create")).hasRole("USER")
                        .requestMatchers(new AntPathRequestMatcher("/post/get/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/like/**")).hasRole("USER")
                        .requestMatchers(new AntPathRequestMatcher("/review/create")).hasRole("USER")
                        .requestMatchers(new AntPathRequestMatcher("/review/get/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/post/delete/**")).hasRole("ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/review/delete/**")).hasRole("ADMIN")
                        .anyRequest().authenticated()
                );

        http
                .headers(header -> header
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();

    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("/**"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        configuration.setExposedHeaders(Arrays.asList("Set-Cookie", "Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return web -> web.ignoring()
                .requestMatchers( "/favicon.ico")
                .requestMatchers( "/error")
                .requestMatchers( "/api/docs")
                .requestMatchers( "/api/swagger-ui/**")
                .requestMatchers( "/swagger-resources/**")
                .requestMatchers( "/v3/api-docs/**");
    }

}
