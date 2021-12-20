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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * @author Brozen
 * @since 1.0
 */
@Slf4j
public class LockTemplate {

    @Setter
    protected RedissonClient redisson;

    /**
     * 在分布式同步锁中执行回调，并返回onSuccess的返回值，失败时打印日志
     * @param lockName      分布式锁名称
     * @param onSuccess     加锁成功时回调
     */
    public <T> T invokeInLock(String lockName, ThrowableSupplier<T> onSuccess) {
        return invokeInLock(lockName, onSuccess, (lock, t) -> null);
    }

    /**
     * 在分布式同步锁中执行回调，并返回onSuccess的返回值
     * @param lockName      分布式锁名称
     * @param onSuccess     加锁成功时回调
     * @param onFailed      加锁失败，或加锁成功但回调抛出异常时，触发该回调；如果回调的第二个Throwable参数为null，说明是加锁失败；
     */
    public <T> T invokeInLock(String lockName, ThrowableSupplier<T> onSuccess, ThrowableBiFunction<RLock, Throwable, T> onFailed) {
        return invokeInLock(lockName, onSuccess, onFailed, 3);
    }

    /**
     * 在分布式同步锁中执行回调，并返回onSuccess的返回值，加锁失败时重试指定次数，onSuccess回调抛出异常时不会重试。
     * @param lockName      分布式锁名称
     * @param onSuccess     加锁成功时回调
     * @param onFailed      重试后仍加锁失败，或加锁成功但回调抛出异常时，触发该回调；如果回调的第二个Throwable参数为null，说明是加锁失败；
     * @param retryTimes    加锁失败重试次数
     */
    public <T> T invokeInLock(String lockName, ThrowableSupplier<T> onSuccess, ThrowableBiFunction<RLock, Throwable, T> onFailed, int retryTimes) {
        return invokeInLock(getLock(lockName), onSuccess, onFailed, retryTimes);
    }

    /**
     * 在分布式同步锁中执行回调，并返回onSuccess的返回值，加锁失败时重试指定次数，onSuccess回调抛出异常时不会重试。
     * @param lock      分布式锁
     * @param onSuccess     加锁成功时回调
     * @param onFailed      重试后仍加锁失败，或加锁成功但回调抛出异常时，触发该回调；如果回调的第二个Throwable参数为null，说明是加锁失败；
     * @param retryTimes    加锁失败重试次数
     */
    public <T> T invokeInLock(RLock lock, ThrowableSupplier<T> onSuccess, ThrowableBiFunction<RLock, Throwable, T> onFailed, int retryTimes) {
        return invokeInLock(lock, onSuccess, onFailed, retryTimes, 100L, TimeUnit.SECONDS.toMillis(100), TimeUnit.MILLISECONDS);
    }

    /**
     * 在分布式同步锁中执行回调，并返回onSuccess的返回值，加锁失败时重试指定次数，onSuccess回调抛出异常时不会重试。
     * @param lock      分布式锁
     * @param onSuccess     加锁成功时回调
     * @param onFailed      重试后仍加锁失败，或加锁成功但回调抛出异常时，触发该回调；如果回调的第二个Throwable参数为null，说明是加锁失败；
     * @param retryTimes    加锁失败重试次数
     * @param waitTime      加锁等待时间
     * @param leaseTime     加锁成功最大持有时间，超过释放
     * @param timeUnit      waitTime和leaseTime的时间单位
     */
    public <T> T invokeInLock(RLock lock, ThrowableSupplier<T> onSuccess, ThrowableBiFunction<RLock, Throwable, T> onFailed,
                              int retryTimes, Long waitTime, Long leaseTime, TimeUnit timeUnit) {
        Objects.requireNonNull(onSuccess, "onSuccess");
        Objects.requireNonNull(onFailed, "onFailed");

        if (tryLock(lock, retryTimes, waitTime, leaseTime, timeUnit)) {
            // 加锁成功，success
            try {
                return onSuccess.get();
            } catch (Throwable t) {
                return onFailed.apply(lock, t);
            } finally {
                String lockName = getLockName(lock);
                try {
                    unlock(lock);
                    log.info("[redisson.locker] 释放锁成功 name={}", lockName);
                } catch (Throwable e) {
                    log.warn("[redisson.locker] 释放锁失败 name={}", lockName, e);
                }
            }
        } else {
            // 加锁失败 failed
            return onFailed.apply(lock, new LockException("申请锁超时"));
        }
    }

    /**
     * 在分布式同步锁中执行回调，失败时打印日志
     * @param lockName      分布式锁名称
     * @param onSuccess     加锁成功时回调
     */
    public void doInLock(String lockName, ThrowableRunner onSuccess) {
        doInLock(lockName, onSuccess, (lock, t) -> { });
    }

    /**
     * 在分布式同步锁中执行回调
     * @param lockName      分布式锁名称
     * @param onSuccess     加锁成功时回调
     * @param onFailed      加锁失败，或加锁成功但回调抛出异常时，触发该回调；如果回调的第二个Throwable参数为null，说明是加锁失败；
     */
    public void doInLock(String lockName, ThrowableRunner onSuccess, BiConsumer<RLock, Throwable> onFailed) {
        doInLock(lockName, onSuccess, onFailed, 3);
    }

    /**
     * 在分布式同步锁中执行回调，加锁失败时重试指定次数，onSuccess回调抛出异常时不会重试。
     * @param lockName      分布式锁名称
     * @param onSuccess     加锁成功时回调
     * @param onFailed      重试后仍加锁失败，或加锁成功但回调抛出异常时，触发该回调；如果回调的第二个Throwable参数为null，说明是加锁失败；
     * @param retryTimes    加锁失败重试次数
     */
    public void doInLock(String lockName, ThrowableRunner onSuccess, BiConsumer<RLock, Throwable> onFailed, int retryTimes) {
        doInLock(getLock(lockName), onSuccess, onFailed, retryTimes);
    }

    /**
     * 在分布式同步锁中执行回调，加锁失败时重试指定次数，onSuccess回调抛出异常时不会重试。
     * @param lock      分布式锁
     * @param onSuccess     加锁成功时回调
     * @param onFailed      重试后仍加锁失败，或加锁成功但回调抛出异常时，触发该回调；如果回调的第二个Throwable参数为null，说明是加锁失败；
     * @param retryTimes    加锁失败重试次数
     */
    public void doInLock(RLock lock, ThrowableRunner onSuccess, BiConsumer<RLock, Throwable> onFailed, int retryTimes) {
        doInLock(lock, onSuccess, onFailed, retryTimes, 100L, TimeUnit.SECONDS.toMillis(100), TimeUnit.MILLISECONDS);
    }

    /**
     * 在分布式同步锁中执行回调，加锁失败时重试指定次数，onSuccess回调抛出异常时不会重试。
     * @param lock      分布式锁
     * @param onSuccess     加锁成功时回调
     * @param onFailed      重试后仍加锁失败，或加锁成功但回调抛出异常时，触发该回调；如果回调的第二个Throwable参数为null，说明是加锁失败；
     * @param retryTimes    加锁失败重试次数
     * @param waitTime      加锁等待时间
     * @param leaseTime     加锁成功最大持有时间，超过释放
     * @param timeUnit      waitTime和leaseTime的时间单位
     */
    public void doInLock(RLock lock, ThrowableRunner onSuccess, BiConsumer<RLock, Throwable> onFailed,
                         int retryTimes, Long waitTime, Long leaseTime, TimeUnit timeUnit) {
        if (tryLock(lock, retryTimes, waitTime, leaseTime, timeUnit)) {
            // 加锁成功，success
            try {
                onSuccess.run();
            } catch (Throwable t) {
                onFailed.accept(lock, t);
            } finally {
                String lockName = getLockName(lock);
                try {
                    unlock(lock);
                    log.info("[redisson.locker] 释放锁成功 name={}", lockName);
                } catch (Throwable e) {
                    log.warn("[redisson.locker] 释放锁失败 name={}", lockName, e);
                }
            }
        } else {
            // 加锁失败 failed
            onFailed.accept(lock, new LockException("申请锁超时"));
        }
    }

    /**
     * 获取RLock锁
     */
    public RLock getLock(String lockName) {
        return redisson.getLock(lockName);
    }

    /**
     * 尝试加锁，默认重试3次，等待100ms，持有锁100s自动释放
     * @param lock  分布式锁
     * @return 是否加锁成功
     */
    public boolean tryLock(RLock lock) {
        return tryLock(lock, 3);
    }

    /**
     * 尝试加锁，默认等待100ms，持有锁100s自动是否
     * @param lock          分布式锁
     * @param retryTimes    获取锁失败时的重试次数
     * @return 是否加锁成功
     */
    public boolean tryLock(RLock lock, int retryTimes) {
        return tryLock(lock, retryTimes, 100, TimeUnit.SECONDS.toMillis(100));
    }

    /**
     * 尝试加锁
     * @param lock          分布式锁
     * @param retryTimes    加锁失败重试次数
     * @param waitTime      加锁等待时间，单位ms
     * @param leaseTime     加锁成功最大持有时间，超过释放，单位ms
     * @return 是否加锁成功
     */
    public boolean tryLock(RLock lock, int retryTimes, long waitTime, long leaseTime) {
        return tryLock(lock, retryTimes, waitTime, leaseTime, TimeUnit.MILLISECONDS);
    }

    /**
     * 尝试加锁
     * @param lock          分布式锁
     * @param retryTimes    加锁失败重试次数
     * @param waitTime      加锁等待时间
     * @param leaseTime     加锁成功最大持有时间，超过释放
     * @param timeUnit      waitTime和leaseTime的时间单位
     * @return 是否加锁成功
     */
    public boolean tryLock(RLock lock, int retryTimes, long waitTime, long leaseTime, TimeUnit timeUnit) {
        waitTime = waitTime <= 0 ? 0 : waitTime;
        leaseTime = leaseTime <= 0 ? -1 : leaseTime;

        String lockName = getLockName(lock);
        log.info("[redisson.locker] 尝试申请锁 name={}", lockName);

        // 加锁重试3次
        int triedTimes;
        try {
            for (triedTimes = retryTimes; triedTimes > 0; triedTimes--) {
                if (lock.tryLock(waitTime, leaseTime, timeUnit)) {
                    log.info("[redisson.locker] 申请锁成功 name={}", lockName);
                    break;
                }

                // 失败重试时，让出一下CPU，防止持续重试失败
                Thread.yield();
            }

            boolean succeed = triedTimes > 0;
            if (!succeed) {
                log.info("[redisson.locker] 申请锁失败 重试{}次 name={} ", retryTimes, lockName);
            }
            return succeed;
        } catch (InterruptedException e) {
            // 线程被中断时，可能锁竞争失败
            log.warn("[redisson.locker] 申请锁失败，线程被中断 name={}", lockName, e);
            return false;
        }
    }


    /**
     * 解锁，因锁持有问题解锁失败时会打印warn日志记录，但不会抛出异常；因redis访问解锁失败时会打印error日志，但不会抛出异常。
     */
    public void unlock(RLock lock) {

        try {
            lock.unlock();
        } catch (IllegalMonitorStateException e) {
            log.warn("[redisson.locker] 解锁抛出非法监视器异常，可能锁不被当前线程持有 name={} thread={} message={}",
                    getLockName(lock), Thread.currentThread().getId(), e.getMessage());
        } catch (RedisException e) {
            log.error("[redisson.locker] 解锁抛出异常，redis访问失败！", e);
        }
    }

    /**
     * 获取锁名称
     */
    protected String getLockName(RLock lock) {
        return lock.getName();
    }

}
