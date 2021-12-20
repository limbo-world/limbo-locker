/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.locker.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.redisson.RedissonLock;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * 基于Redisson MultiLock的联锁加锁器
 *
 * @author Brozen
 * @since 1.0
 */
@Slf4j
public class MultiLockTemplate extends LockTemplate {

    /**
     * locks字段，反射读取locks
     */
    static final Field MLOCK_LOCKS_FIELD;
    static {
        try {
            MLOCK_LOCKS_FIELD = RedissonMultiLock.class.getDeclaredField("locks");
        } catch (NoSuchFieldException e) {
            throw new Error("RedissonMultiLock中无法找到locks字段，请检查Redisson版本。");
        }
    }

    /**
     * 在分布式同步锁中执行回调，并返回onSuccess的返回值，失败时打印日志
     * @param lockNames      分布式锁名称
     * @param onSuccess     加锁成功时回调
     */
    public <T> T invokeInMultiLock(String[] lockNames, ThrowableSupplier<T> onSuccess) {
        return invokeInMultiLock(lockNames, onSuccess, (lock, t) -> null);
    }

    /**
     * 在分布式同步锁中执行回调，并返回onSuccess的返回值
     * @param lockNames      分布式锁名称
     * @param onSuccess     加锁成功时回调
     * @param onFailed      加锁失败，或加锁成功但回调抛出异常时，触发该回调；如果回调的第二个Throwable参数为null，说明是加锁失败；
     */
    public <T> T invokeInMultiLock(String[] lockNames, ThrowableSupplier<T> onSuccess, ThrowableBiFunction<RLock, Throwable, T> onFailed) {
        return invokeInMultiLock(lockNames, onSuccess, onFailed, 3);
    }

    /**
     * 在分布式同步锁中执行回调，并返回onSuccess的返回值，加锁失败时重试指定次数，onSuccess回调抛出异常时不会重试。
     * @param lockNames     分布式锁名称
     * @param onSuccess     加锁成功时回调
     * @param onFailed      重试后仍加锁失败，或加锁成功但回调抛出异常时，触发该回调；如果回调的第二个Throwable参数为null，说明是加锁失败；
     * @param retryTimes    加锁失败重试次数
     */
    public <T> T invokeInMultiLock(String[] lockNames, ThrowableSupplier<T> onSuccess, ThrowableBiFunction<RLock, Throwable, T> onFailed, int retryTimes) {
        RLock lock = getMultiLock(lockNames);
        return invokeInLock(lock, onSuccess, onFailed, retryTimes);
    }

    /**
     * 在分布式同步锁中执行回调，并返回onSuccess的返回值，加锁失败时重试指定次数，onSuccess回调抛出异常时不会重试。
     * @param lockNames     分布式锁名称
     * @param onSuccess     加锁成功时回调
     * @param onFailed      重试后仍加锁失败，或加锁成功但回调抛出异常时，触发该回调；如果回调的第二个Throwable参数为null，说明是加锁失败；
     * @param retryTimes    加锁失败重试次数
     * @param waitTime      加锁等待时间
     * @param leaseTime     加锁成功最大持有时间，超过释放
     * @param timeUnit      waitTime和leaseTime的时间单位
     */
    public <T> T invokeInMultiLock(String[] lockNames, ThrowableSupplier<T> onSuccess, ThrowableBiFunction<RLock, Throwable, T> onFailed,
                                   int retryTimes, Long waitTime, Long leaseTime, TimeUnit timeUnit) {
        RLock lock = getMultiLock(lockNames);
        return invokeInLock(lock, onSuccess, onFailed, retryTimes, waitTime, leaseTime, timeUnit);
    }

    /**
     * 在分布式同步锁中执行回调，失败时打印日志
     * @param lockNames     分布式锁名称
     * @param onSuccess     加锁成功时回调
     */
    public void doInMultiLock(String[] lockNames, ThrowableRunner onSuccess) {
        doInMultiLock(lockNames, onSuccess, (lock, t) -> { });
    }

    /**
     * 在分布式同步锁中执行回调
     * @param lockNames      分布式锁名称
     * @param onSuccess     加锁成功时回调
     * @param onFailed      加锁失败，或加锁成功但回调抛出异常时，触发该回调；如果回调的第二个Throwable参数为null，说明是加锁失败；
     */
    public void doInMultiLock(String[] lockNames, ThrowableRunner onSuccess, BiConsumer<RLock, Throwable> onFailed) {
        doInMultiLock(lockNames, onSuccess, onFailed, 3);
    }

    /**
     * 在分布式同步锁中执行回调，加锁失败时重试指定次数，onSuccess回调抛出异常时不会重试。
     * @param lockNames      分布式锁名称
     * @param onSuccess     加锁成功时回调
     * @param onFailed      重试后仍加锁失败，或加锁成功但回调抛出异常时，触发该回调；如果回调的第二个Throwable参数为null，说明是加锁失败；
     * @param retryTimes    加锁失败重试次数
     */
    public void doInMultiLock(String[] lockNames, ThrowableRunner onSuccess, BiConsumer<RLock, Throwable> onFailed, int retryTimes) {
        RLock lock = getMultiLock(lockNames);
        doInLock(lock, onSuccess, onFailed, retryTimes);
    }

    /**
     * 在分布式同步锁中执行回调，加锁失败时重试指定次数，onSuccess回调抛出异常时不会重试。
     * @param lockNames      分布式锁名称
     * @param onSuccess     加锁成功时回调
     * @param onFailed      重试后仍加锁失败，或加锁成功但回调抛出异常时，触发该回调；如果回调的第二个Throwable参数为null，说明是加锁失败；
     * @param retryTimes    加锁失败重试次数
     * @param waitTime      加锁等待时间
     * @param leaseTime     加锁成功最大持有时间，超过释放
     * @param timeUnit      waitTime和leaseTime的时间单位
     */
    public void doInMultiLock(String[] lockNames, ThrowableRunner onSuccess, BiConsumer<RLock, Throwable> onFailed,
                              int retryTimes, Long waitTime, Long leaseTime, TimeUnit timeUnit) {
        RLock lock = getMultiLock(lockNames);
        doInLock(lock, onSuccess, onFailed, retryTimes, waitTime, leaseTime, timeUnit);
    }

    /**
     * 获取联锁
     */
    public RLock getMultiLock(String[] lockNames) {
        RLock[] locks = new RLock[lockNames.length];
        for (int i = 0; i < lockNames.length; i++) {
            String lockName = lockNames[i];
            locks[i] = redisson.getLock(lockName);
        }

        return redisson.getMultiLock(locks);
    }

    /**
     * 联锁不支持部分方法，因此重写方法，直接释放
     */
    @Override
    public void unlock(RLock lock) {
        lock.unlock();
    }

    /**
     * 联锁不支持获取名称，因此重写方法，反射得到联锁的所有锁，并依次获取名称
     */
    @Override
    protected String getLockName(RLock lock) {
        if (lock instanceof RedissonLock) {

            return super.getLockName(lock);

        } else if (lock instanceof RedissonMultiLock) {

            try {
                @SuppressWarnings("unchecked")
                List<RLock> locks = (List<RLock>) FieldUtils.readField(MLOCK_LOCKS_FIELD, lock, true);
                List<String> lockNames = locks.stream()
                        .map(this::getLockName)
                        .collect(Collectors.toList());
                return "[" + StringUtils.join(lockNames, ",") + "]";
            } catch (IllegalAccessException e) {
                log.warn("[redisson.multi.locker] 读取锁名称失败，无法反射获取lockers");
                return "UNKNOWN";
            }

        } else {
            log.error("[redisson.multi.locker] 获取锁名称失败，未知的锁类型 {}", lock.getClass());
            return "UNKNOWN";
        }
    }
}
