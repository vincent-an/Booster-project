package fortuneCookie.booster.domain.user.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import fortuneCookie.booster.domain.user.dto.LoginRequest;
import fortuneCookie.booster.domain.user.entity.RefreshEntity;
import fortuneCookie.booster.domain.user.repository.RefreshRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public LoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil, RefreshRepository refreshRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
        setFilterProcessesUrl("/booster/login"); // 로그인 경로를 "/booster/login"으로 변경
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // 요청 본문에서 JSON 형식으로 데이터를 읽음
            ObjectMapper objectMapper = new ObjectMapper();
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
            // JSON에서 이메일과 비밀번호 추출
            String email = loginRequest.getEmail();
            String password = loginRequest.getPassword();

            // 이메일과 비밀번호를 콘솔에 출력하여 제대로 값이 들어왔는지 확인
            log.info("Received Email: {}", email);
            log.info("Received Password: {}", password);

            // 이메일과 비밀번호를 이용해 AuthenticationToken 생성
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password, null);
            // authenticationManager를 통해 인증을 시도하고 결과를 반환
            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            // 예외 발생 시 RuntimeException을 던짐
            throw new RuntimeException("Authentication failed", e);
        }
    }

    //로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException {
        //유저 정보
        String email = authentication.getName();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        //토큰 생성
        String access = jwtUtil.createJwt("access", email, role, 600000L);
        String refresh = jwtUtil.createJwt("refresh", email, role, 86400000L);

        //Refresh 토큰 저장
        addRefreshEntity(email, refresh, 86400000L);

        //응답 설정
        log.info("✅ access 토큰 생성 완료: {}", access);
        response.setHeader("access", access);
        response.addCookie(createCookie("refresh", refresh));
        response.setStatus(HttpStatus.OK.value());
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(401);
        log.warn("login failed: {}", failed.getMessage());
    }

    // refresh token을 쿠키로 보낼 시 사용
    private Cookie createCookie(String key, String value) { // key 값, jwt가 받을 value값

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60); // 위 refresh 토큰 생성과 동일한 값
        //cookie.setSecure(true); //https 통신을 할 경우 활성화시켜주면 됨
        //cookie.setPath("/"); // 쿠키가 적용될 범위도 설정 가능
        cookie.setHttpOnly(true); //클라이언트에서 js로 쿠키에 접근하는 걸 막아줘야함

        return cookie;
    }

    // refresh token을 repository에 넣어주는 메서드
    private void addRefreshEntity(String email, String refresh, Long expiredMs) { //만료일자

        Date date = new Date(System.currentTimeMillis() + expiredMs);

        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setEmail(email);
        refreshEntity.setRefresh(refresh);
        refreshEntity.setExpiration(date.toString());

        refreshRepository.save(refreshEntity);
    }
}
