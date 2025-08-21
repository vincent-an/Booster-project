package fortuneCookie.booster.global.config;

import fortuneCookie.booster.domain.user.jwt.CustomLogoutFilter;
import fortuneCookie.booster.domain.user.jwt.JwtFilter;
import fortuneCookie.booster.domain.user.jwt.JwtUtil;
import fortuneCookie.booster.domain.user.jwt.LoginFilter;
import fortuneCookie.booster.domain.user.repository.RefreshRepository;
import fortuneCookie.booster.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UserRepository userRepository) throws Exception {
        // CORS
        http.cors(cors -> cors.configurationSource(new CorsConfigurationSource() {
            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOriginPatterns(List.of("http://127.0.0.1:3000", "http://localhost:3000"));
                configuration.setAllowedMethods(Collections.singletonList("*"));
                configuration.setAllowCredentials(true);
                configuration.setAllowedHeaders(Collections.singletonList("*"));
                configuration.setMaxAge(7200L);
                // 프론트에서 읽을 수 있게 노출할 헤더
                configuration.setExposedHeaders(Arrays.asList("Authorization", "access"));
                return configuration;
            }
        }));

        // 기본 보안 옵션
        http.csrf(csrf -> csrf.disable());
        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());

        // 인가 규칙 (단일 블록로 유지: 중복 선언 금지)
        http.authorizeHttpRequests(auth -> auth
                // 공개 경로
                .requestMatchers("/", "/booster/login", "/booster/join", "/booster/reissue").permitAll()
                // 게시판(기존 동작 유지: 하위 경로 포함해 전부 허용)
                .requestMatchers("/booster/tmiBoard/**",
                        "/booster/infoBoard/**",
                        "/booster/promoBoard/**",
                        "/booster/freeBoard/**",
                        "/booster/generalBoard/**").permitAll()
                // 프리플라이트 허용(개발 편의)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 게시글 작성은 로그인 유저만(B 방식)
                .requestMatchers(HttpMethod.POST, "/booster/posts").hasAnyRole("USER","ADMIN")
                // 그 외는 인증 필요
                .anyRequest().authenticated()
        );

        // 필터 체인 순서
        http
                .addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshRepository), LogoutFilter.class)
                .addFilterBefore(new JwtFilter(jwtUtil, userRepository), UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, refreshRepository),
                        UsernamePasswordAuthenticationFilter.class);

        // 세션: 무상태
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
