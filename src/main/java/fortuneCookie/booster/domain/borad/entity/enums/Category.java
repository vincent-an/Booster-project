package fortuneCookie.booster.domain.borad.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Category {
    FREE("자유 게시판"),
    PROMO("홍보 게시판"),
    INFO("정보 게시판"),
    TMI("tmi 게시판");

    private final String value;

    Category(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
