package fortuneCookie.booster.domain.borad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Postman 테스트용: CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/booster/**",      // 게시판 APIs 모두 허용
                                "/error", "/actuator/**"
                        ).permitAll()
                        .anyRequest().permitAll()
                )
                .httpBasic(basic -> {}); // 필요시 기본 인증 활성화(미사용)
        return http.build();
    }
}
