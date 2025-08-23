package fortuneCookie.booster.domain.user.jwt;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey secretKey;

    public JwtUtil(@Value("${spring.jwt.secret}")String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    // username 토큰 확인
    public String getEmail(String token) {
        //토큰을 전달 받아, parser를 이용하여 암호화된 토큰을 검증함 verifyWith(secretKey) 이걸로, 이 토큰이 우리 서버의 토큰이 맞는지 확인
        //build로 확인할거임, 클레임을 확인하고, 페이로드 부분에서 특정한 데이터를 get으로 email이라는 키를 획득함, 타입은 String
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("email", String.class);
    }
    // role 토큰 확인
    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public String getCategory(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category", String.class);
    }
    // 토큰의 만료 여부 확인 boolean / 소멸되면 true, 안되면 false
    public Boolean isExpired(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }
    // Jwt token 생성
    public String createJwt(String category, String email, String role, Long expiredMs) {

        return Jwts.builder()
                .claim("category", category)
                .claim("email", email) //claim을 이용해 만들 매개변수와 이름 데이터를 넣음
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis())) //언제 발행했는지 발행시간 넣음
                .expiration(new Date(System.currentTimeMillis() + expiredMs)) //언제 소멸될 것인지 넣음
                .signWith(secretKey) //secreKey를 통해서 암호화를 진행하는 코드
                .compact(); // compact를 통해 리턴함
    }
}
