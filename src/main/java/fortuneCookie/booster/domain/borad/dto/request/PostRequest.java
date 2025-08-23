package fortuneCookie.booster.domain.borad.dto.request;

import fortuneCookie.booster.domain.borad.entity.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {
    private List<String> imgUrls = new ArrayList<>();
    private String introImgUrl;
    private Category category;
    private String title;
    private String content;
    private Boolean isAnonymous = false;
}
