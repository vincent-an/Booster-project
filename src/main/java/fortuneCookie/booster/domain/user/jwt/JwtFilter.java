package fortuneCookie.booster.domain.user.jwt;

import fortuneCookie.booster.domain.user.dto.CustomUserDetails;
import fortuneCookie.booster.domain.user.dto.LoginUserDTO;
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

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    /**
     * 인증 없이 통과할 경로만 화이트리스트.
     * - OPTIONS(CORS preflight)
     * - "/", "/booster/login", "/booster/join", "/booster/reissue"
     * - 게시판 조회(GET) 경로들
     * ❗ POST /booster/posts 는 인증 필수이므로 화이트리스트에 포함하지 않는다.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        final String path = request.getRequestURI();
        final String method = request.getMethod();

        // CORS preflight
        if ("OPTIONS".equalsIgnoreCase(method)) return true;

        // 공개 엔드포인트
        if ("/".equals(path)
                || "/booster/login".equals(path)
                || "/booster/join".equals(path)
                || "/booster/reissue".equals(path)) {
            return true;
        }

        // 게시판 조회는 GET만 공개
        if ("GET".equalsIgnoreCase(method) && (
                path.startsWith("/booster/freeBoard")
                        || path.startsWith("/booster/promoBoard")
                        || path.startsWith("/booster/infoBoard")
                        || path.startsWith("/booster/tmiBoard")
                        || path.startsWith("/booster/generalBoard"))) {
            return true;
        }

        // 그 외는 인증 필수 → 필터 적용
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1) 토큰 추출: Authorization(Bearer ...) 우선, 없으면 access 헤더도 지원
        String token = resolveToken(request);

        // 2) 토큰이 없으면 다음 필터로 (인증 필수 경로는 최종적으로 401/403 처리됨)
        if (token == null || token.isEmpty()) {
            log.debug("[JWT] no token found. uri={}, method={}", request.getRequestURI(), request.getMethod());
            filterChain.doFilter(request, response);
            return;
        }

        // 3) 만료/유효성/카테고리(access) 검사
        try {
            jwtUtil.isExpired(token);
        } catch (ExpiredJwtException e) {
            log.debug("[JWT] access token expired. uri={}, method={}", request.getRequestURI(), request.getMethod());
            writeUnauthorized(response, "access token expired");
            return;
        } catch (Exception e) {
            log.debug("[JWT] invalid token. reason={}, uri={}, method={}", e.getMessage(), request.getRequestURI(), request.getMethod());
            writeUnauthorized(response, "invalid access token");
            return;
        }

        String category = jwtUtil.getCategory(token);
        if (!"access".equals(category)) {
            log.debug("[JWT] invalid category: {} (expected access). uri={}, method={}", category, request.getRequestURI(), request.getMethod());
            writeUnauthorized(response, "invalid access token");
            return;
        }

        // 4) 이메일/권한 추출 후 Authentication 세팅
        String email = jwtUtil.getEmail(token);
        String role = jwtUtil.getRole(token); // 예: "ROLE_USER" 또는 "ROLE_ADMIN"

        LoginUserDTO dto = LoginUserDTO.of(email, role);
        CustomUserDetails customUserDetails = new CustomUserDetails(dto);

        Authentication authToken =
                new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        log.debug("[JWT] authentication set. email={}, role={}", email, role);

        // 5) 다음 필터 진행
        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더(Bearer) 우선, 없으면 기존 access 헤더도 지원.
     */
    private String resolveToken(HttpServletRequest request) {
        String token = null;

        // 표준: Authorization: Bearer <JWT>
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            if (authHeader.regionMatches(true, 0, "Bearer ", 0, 7) && authHeader.length() > 7) {
                token = authHeader.substring(7).trim();
            } else {
                token = authHeader.trim();
            }
        }

        // 호환: access 헤더
        if (token == null || token.isEmpty()) {
            String accessHeader = request.getHeader("access");
            if (accessHeader != null) {
                token = accessHeader.startsWith("Bearer ") ? accessHeader.substring(7).trim() : accessHeader.trim();
            }
        }

        return token;
    }

    private void writeUnauthorized(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        try (PrintWriter writer = response.getWriter()) {
            writer.print(msg);
            writer.flush();
        }
    }
}
