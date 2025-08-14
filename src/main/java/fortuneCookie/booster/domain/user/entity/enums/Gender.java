package fortuneCookie.booster.domain.user.entity.enums;

public enum Gender {
    MALE("남성"),
    FEMALE("여성"),
    OTHER("기타");

    private final String value;

    Gender(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
