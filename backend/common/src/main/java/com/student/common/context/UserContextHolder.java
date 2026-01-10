package com.student.common.context;

/**
 * 用户上下文持有者
 * 使用ThreadLocal存储当前线程的用户上下文信息
 */
public class UserContextHolder {

    /**
     * 用户上下文ThreadLocal
     */
    private static final ThreadLocal<UserContext> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 忽略数据权限标志ThreadLocal
     */
    private static final ThreadLocal<Boolean> IGNORE_FLAG = new ThreadLocal<>();

    /**
     * 设置用户上下文
     *
     * @param context 用户上下文
     */
    public static void setContext(UserContext context) {
        CONTEXT_HOLDER.set(context);
    }

    /**
     * 获取用户上下文
     *
     * @return 用户上下文
     */
    public static UserContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清理ThreadLocal
     * 必须在请求结束时调用，防止内存泄漏
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
        IGNORE_FLAG.remove();
    }

    /**
     * 设置是否忽略数据权限
     *
     * @param ignore true-忽略数据权限，false-不忽略
     */
    public static void setIgnoreDataPermission(boolean ignore) {
        IGNORE_FLAG.set(ignore);
    }

    /**
     * 判断是否忽略数据权限
     *
     * @return true-忽略，false-不忽略
     */
    public static boolean isIgnoreDataPermission() {
        return Boolean.TRUE.equals(IGNORE_FLAG.get());
    }
}
