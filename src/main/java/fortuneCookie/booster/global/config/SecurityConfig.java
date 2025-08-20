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

    //비밀번호 캐시로 암호화하여 검증하고 진행하기 때문에 비밀번호를 암호화 하는 코드
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UserRepository userRepository) throws Exception {
        //로그인필터 관련 Cors문제를 해결하기 위한 configuration코드
        http
                .cors((cors) -> cors
                        .configurationSource(new CorsConfigurationSource() {
                            @Override
                            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                                CorsConfiguration configuration =  new CorsConfiguration();
                                //프론트에서 보낼 3000번 포트 허용
                                configuration.setAllowedOriginPatterns(List.of("http://127.0.0.1:3000", "http://localhost:3000"));
                                //허용할 메서드는 get, post, 등등 다 허용
                                configuration.setAllowedMethods(Collections.singletonList("*"));
                                //프론트에서 Credentials 설정을 하면 true로 바꿔줘야 함
                                configuration.setAllowCredentials(true);
                                //허용할 헤더 * = 모두 허용
                                configuration.setAllowedHeaders(Collections.singletonList("*"));
                                //허용을 물고 있을 시간
                                configuration.setMaxAge(7200L);
                                // 우리 -> 프론트(클라이언트)쪽으로 헤더를 보내줄때 Authorization에 jwt를 넣어 보내줄 것이기 때문에
                                // Authorization도 허용해줘야한다.
//                                configuration.setExposedHeaders(Collections.singletonList("Authorization"));
                                // 프론트에서 JS로 응답 헤더를 읽을 수 있도록 추가
                                configuration.setExposedHeaders(Arrays.asList("Authorization", "access"));
                                //우리가 만든 configuration을 리턴
                                return configuration;
                            }
                        }));
        //csrf disable
        http
                .csrf((auth) -> auth.disable());
        //From 로그인 방식 disable
        http
                .formLogin((auth) -> auth.disable());
        //http basic 인증 방식 disable
        http
                .httpBasic((auth) -> auth.disable());
        //경로별 인가 작업
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/booster/login", "/booster/join", "/", "/booster/chatbot/**").permitAll()
//                        .requestMatchers("/admin").hasRole("ADMIN")
                        .requestMatchers("/booster/reissue").permitAll() //reissue는 전체 접근 가능
                        .anyRequest().authenticated());
        //LoginFilter 추가 + 로그인 필터와 jwt 검증 충돌로 인한 변경
        http
                .addFilterBefore(new JwtFilter(jwtUtil, userRepository), UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, refreshRepository), UsernamePasswordAuthenticationFilter.class);
//                .addFilterBefore(new JwtFilter(jwtUtil, userRepository), LoginFilter.class)
//                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, refreshRepository), UsernamePasswordAuthenticationFilter.class);

        // 로그아웃 필터 추가 (기존 로그아웃 필터 앞에)
        http
                .addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshRepository), LogoutFilter.class);

        //세션 설정
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
