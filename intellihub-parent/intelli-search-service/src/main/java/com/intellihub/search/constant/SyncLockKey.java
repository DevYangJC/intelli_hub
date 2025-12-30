package com.intellihub.search.constant;

/**
 * 同步任务锁 Key 常量
 *
 * @author IntelliHub
 */
public final class SyncLockKey {

    private SyncLockKey() {}

    /**
     * API 全量同步锁
     */
    public static final String API_FULL_SYNC = "api:full:sync";

    /**
     * API 增量同步锁
     */
    public static final String API_INCREMENTAL_SYNC = "api:incremental:sync";

    /**
     * 应用全量同步锁
     */
    public static final String APP_FULL_SYNC = "app:full:sync";

    /**
     * 应用增量同步锁
     */
    public static final String APP_INCREMENTAL_SYNC = "app:incremental:sync";

    /**
     * 用户全量同步锁
     */
    public static final String USER_FULL_SYNC = "user:full:sync";

    /**
     * 用户增量同步锁
     */
    public static final String USER_INCREMENTAL_SYNC = "user:incremental:sync";
}
