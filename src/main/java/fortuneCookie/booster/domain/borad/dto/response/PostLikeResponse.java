package fortuneCookie.booster.domain.borad.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PostLikeResponse {

    private Long postId;
    private Boolean isLiked;
    private Long likeCount;
    private String message;

    public static PostLikeResponse of(Long postId, boolean isLiked, long likeCount) {
        String message = isLiked ? "좋아요를 눌렀습니다." : "좋아요를 취소했습니다.";

        return PostLikeResponse.builder()
                .postId(postId)
                .isLiked(isLiked)
                .likeCount(likeCount)
                .message(message)
                .build();
    }
}
