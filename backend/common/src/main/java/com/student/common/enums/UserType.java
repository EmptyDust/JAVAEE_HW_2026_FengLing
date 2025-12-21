package com.student.common.enums;

/**
 * 用户类型枚举
 */
public enum UserType {
    /**
     * 管理员
     */
    ADMIN("admin", "管理员"),

    /**
     * 教师
     */
    TEACHER("teacher", "教师"),

    /**
     * 学生
     */
    STUDENT("student", "学生");

    private final String code;
    private final String desc;

    UserType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据 code 获取枚举
     */
    public static UserType fromCode(String code) {
        for (UserType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid user type code: " + code);
    }
}
