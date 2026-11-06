package com.ruoyi.common.utils.uuid;

import java.lang.management.ManagementFactory;

/**
 * 雪花算法（Twitter Snowflake）ID 生成工具类。
 *
 * <p>64 位 long 结构：1 位符号位（恒为 0）+ 41 位时间戳 + 5 位数据中心 + 5 位机器 + 12 位序列号，
 * 生成形如 2033559054479278083 的 19 位正整数，趋势递增、线程安全、同一实例内不重复。</p>
 *
 * <pre>
 * long id = SnowflakeIdUtils.nextId();
 * </pre>
 *
 * <p>默认按「主机名 + 进程 PID」推导 dataCenterId / workerId，同一机器多进程通常可区分；
 * 多实例部署建议用 JVM 参数显式指定，避免取模碰撞：
 * -Dsnowflake.dataCenterId=1 -Dsnowflake.workerId=1</p>
 *
 * @author ruoyi
 */
public final class SnowflakeIdUtils {

    /** 起始时间戳 2010-11-04 01:42:54.657 UTC，决定 ID 的量级与可用年限（约 69 年） */
    private static final long START_TIMESTAMP = 1288834974657L;

    private static final long DATA_CENTER_ID_BITS = 5L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    private static final long MAX_TIMESTAMP = ~(-1L << 41L);

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

    /** 允许等待的时钟回拨毫秒数，超过则拒绝生成，避免产生重复 ID */
    private static final long MAX_BACKWARD_MS = 5L;

    private static final SnowflakeIdUtils INSTANCE = new SnowflakeIdUtils();

    private final long dataCenterId;
    private final long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    private SnowflakeIdUtils() {
        long[] machineIds = resolveMachineIds();
        this.dataCenterId = machineIds[0];
        this.workerId = machineIds[1];
    }

    /**
     * 生成下一个雪花 ID。
     *
     * @return 19 位左右的递增 long 型 ID
     */
    public static long nextId() {
        return INSTANCE.next();
    }

    private synchronized long next() {
        long current = System.currentTimeMillis();

        if (current < lastTimestamp) {
            long backward = lastTimestamp - current;
            if (backward > MAX_BACKWARD_MS) {
                throw new IllegalStateException("检测到时钟回拨 " + backward + "ms，拒绝生成雪花 ID");
            }
            spinWait(lastTimestamp);
            current = System.currentTimeMillis();
            if (current < lastTimestamp) {
                throw new IllegalStateException("等待时钟追上失败，拒绝生成雪花 ID");
            }
        }

        if (current == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0L) {
                current = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = current;
        long delta = current - START_TIMESTAMP;
        if (delta > MAX_TIMESTAMP) {
            throw new IllegalStateException("时间戳超出 41 位上限，请更换 START_TIMESTAMP");
        }
        return (delta << TIMESTAMP_SHIFT)
                | (dataCenterId << DATA_CENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long waitNextMillis(long last) {
        long current = System.currentTimeMillis();
        while (current <= last) {
            Thread.yield();
            current = System.currentTimeMillis();
        }
        return current;
    }

    private void spinWait(long target) {
        while (System.currentTimeMillis() < target) {
            Thread.yield();
        }
    }

    private static long[] resolveMachineIds() {
        String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
        int at = runtimeName.indexOf('@');
        int pid = 0;
        String host = runtimeName;
        if (at > 0) {
            try {
                pid = Integer.parseInt(runtimeName.substring(0, at));
            } catch (NumberFormatException ignored) {
                pid = 0;
            }
            host = runtimeName.substring(at + 1);
        }

        int hostHash = host.hashCode() & 0x7fffffff;
        long defaultDataCenterId = (hostHash >>> 8) & MAX_DATA_CENTER_ID;
        long defaultWorkerId = (hostHash ^ pid) & MAX_WORKER_ID;

        Long dataCenterId = Long.getLong("snowflake.dataCenterId", defaultDataCenterId);
        Long workerId = Long.getLong("snowflake.workerId", defaultWorkerId);
        if (dataCenterId < 0 || dataCenterId > MAX_DATA_CENTER_ID) {
            throw new IllegalArgumentException("snowflake.dataCenterId 必须在 0~" + MAX_DATA_CENTER_ID + " 之间");
        }
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException("snowflake.workerId 必须在 0~" + MAX_WORKER_ID + " 之间");
        }
        return new long[]{dataCenterId, workerId};
    }
}
