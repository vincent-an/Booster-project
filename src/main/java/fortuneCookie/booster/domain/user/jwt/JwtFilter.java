package fortuneCookie.booster.domain.user.jwt;

import fortuneCookie.booster.domain.user.dto.CustomUserDetails;
import fortuneCookie.booster.domain.user.dto.LoginUserDTO;
import fortuneCookie.booster.domain.user.entity.User;
import fortuneCookie.booster.domain.user.entity.enums.Role;
import fortuneCookie.booster.domain.user.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // 1. 필터 제외할 경로 지정 (로그인, 회원가입 등)
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return Objects.equals("/booster/login", path) ||
                Objects.equals("/booster/join", path) ||
                Objects.equals("/booster/generalBoard", path) ||
                Objects.equals("/booster/freeBoard", path) ||
                Objects.equals("/booster/promoBoard", path) ||
                Objects.equals("/booster/infoBoard", path) ||
                Objects.equals("/booster/tmiBoard", path) ||
                Objects.equals("/", path);
    }


    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 헤더에서 access키에 담긴 토큰을 꺼냄
        String accessToken = request.getHeader("access");

        // 토큰이 없다면 다음 필터로 넘김
        if (accessToken == null) {

            filterChain.doFilter(request, response);

            return;
        }

        // 토큰 만료 여부 확인, 만료시 다음 필터로 넘기지 않음
        try {
            jwtUtil.isExpired(accessToken); // 토큰 만료여부 확인
        } catch (ExpiredJwtException e) { //만료 되면 에러

            //response body
            PrintWriter writer = response.getWriter();
            writer.print("access token expired");

            //response status code (프론트 측과 특정한 상태코드를 맞춰야함 401이면 401 400이면 400, 현재는 401)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return; // 만료되면 다음 코드로 넘기면 절대 안됨!
        }

        // 토큰이 access인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(accessToken);

        if (!category.equals("access")) {

            //response body
            PrintWriter writer = response.getWriter();
            writer.print("invalid access token");

            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // username, role 값을 획득
        String email = jwtUtil.getEmail(accessToken);
        String role = jwtUtil.getRole(accessToken);

        LoginUserDTO dto = LoginUserDTO.of(email, role);
// dto로 변경하면서 굳이 세팅 x
//        User user = new User();
//        user.setEmail(email);
//        user.setRole(Role.fromValue(role));

        CustomUserDetails customUserDetails = new CustomUserDetails(dto);
        // 로그인 진행
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        //세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}

