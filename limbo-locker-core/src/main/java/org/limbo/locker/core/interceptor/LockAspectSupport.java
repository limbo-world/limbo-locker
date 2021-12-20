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

package org.limbo.locker.core.interceptor;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.limbo.locker.core.LockTemplate;
import org.limbo.locker.core.MultiLockTemplate;
import org.limbo.locker.core.attribute.LockAttribute;
import org.limbo.locker.core.attribute.LockAttributeSource;
import org.limbo.locker.core.attribute.LockDefinition;
import org.limbo.locker.core.evaluation.NameEvaluateResult;
import org.limbo.locker.core.evaluation.NameEvaluationContext;
import org.limbo.locker.core.evaluation.NameEvaluationContextFactory;
import org.limbo.locker.core.evaluation.NameEvaluator;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁切面能力支持，提供锁名解析、加锁、释放等流程节点的能力，不区分具体的切面实现方式
 *
 * @author Brozen
 * @since 1.0
 */
@Slf4j
public abstract class LockAspectSupport implements BeanFactoryAware {

    /**
     * 默认的锁名称计算Bean的名称，会从Spring的{@link BeanFactory}中查找指定名称的Bean
     */
    @Setter
    private String nameEvaluatorBeanName;

    /**
     * 锁名称计算器
     */
    @Setter
    @Getter
    private NameEvaluator nameEvaluator;

    /**
     * 锁名计算上下文工厂类
     */
    @Setter
    private NameEvaluationContextFactory nameEvaluationContextFactory;

    /**
     * Spring BeanFactory，用于找到NameEvaluator
     */
    @Setter
    private BeanFactory beanFactory;

    /**
     * 加锁属性数据源，用于读取执行方法的加锁属性。
     */
    @Setter
    @Getter
    private LockAttributeSource lockAttributeSource;

    /**
     * 单锁加锁模板
     */
    @Getter
    @Setter
    private LockTemplate lockTemplate;

    /**
     * 联锁加锁模板
     */
    @Getter
    @Setter
    private MultiLockTemplate multiLockTemplate;


    /**
     * 加锁，并执行回调函数。
     * @param method 切面拦截的执行方法
     * @param targetClass 切面拦截的执行方法所在类
     * @param args 切面拦截的执行方法参数
     * @param invocation 回调函数
     * @return 切面拦截的方法执行结果
     */
    protected Object invokeInLock(Method method, Class<?> targetClass, Object[] args, LockInvocation invocation) throws Throwable {
        LockAttributeSource las = getLockAttributeSource();
        LockAttribute lockAttr = las == null ? null : las.getLockAttribute(method, targetClass);

        // 没有加锁配置，说明无需加锁，直接执行方法
        if (lockAttr == null) {
            return invocation.proceed();
        }

        // 计算锁名称
        NameEvaluator nameEvaluator = determineNameEvaluator(lockAttr);
        NameEvaluationContext context = nameEvaluationContextFactory
                .createNameEvaluationContext(method, targetClass, args, lockAttr);
        NameEvaluateResult nameEvaluateResult = nameEvaluator.evaluate(context);

        // 根据加锁类型，调用不同的模板加锁
        int lockType = lockAttr.getLockType();
        if (lockType == LockDefinition.LOCK_TYPE_SINGLE) {

            // 单锁计算结果直接返回锁名称
            return invokeInSingleLock(method, invocation, lockAttr, ((String) nameEvaluateResult.getEvaluatedName()));

        } else if (lockType == LockDefinition.LOCK_TYPE_MULTI) {

            // 联锁计算结果返回锁名称数组
            return invokeInMultiLock(method, invocation, lockAttr, ((String[]) nameEvaluateResult.getEvaluatedName()));

        } else {

            // 异常的加锁类型，给出警告日志，当成无锁处理
            String methodName = ClassUtils.getQualifiedMethodName(method, targetClass);
            log.warn("lock type error, cannot identify type {} on {}", lockType, methodName);
            return invocation.proceed();

        }

    }


    /**
     * 在联锁中执行方法
     * @param method 待执行的方法
     * @param invocation 方法执行回调
     * @param lockAttr 加锁配置
     * @param lockNames 锁名称
     * @return 方法执行结果
     */
    private Object invokeInMultiLock(Method method, LockInvocation invocation, LockAttribute lockAttr, String[] lockNames) {
        MultiLockTemplate lockTemplate = getMultiLockTemplate();
        return lockTemplate.invokeInMultiLock(lockNames,
                () -> {
                    if (log.isTraceEnabled()) {
                        log.info("lock acquired lock={}", StringUtils.join(lockNames, ","));
                    }

                    return invocation.proceed();
                },
                (lock, throwable) -> whenLockFailed(throwable, StringUtils.join(lockNames, ","), method),
                lockAttr.getRetryTimes(),
                lockAttr.getWaitTime().toMillis(),
                lockAttr.getHoldTime().toMillis(),
                TimeUnit.MILLISECONDS
        );
    }


    /**
     * 在单锁中执行方法
     * @param method 待执行的方法
     * @param invocation 方法执行回调
     * @param lockAttr 加锁配置
     * @param lockName 锁名称
     * @return 方法执行结果
     */
    private Object invokeInSingleLock(Method method, LockInvocation invocation, LockAttribute lockAttr, String lockName) {
        LockTemplate lockTemplate = getLockTemplate();
        return lockTemplate.invokeInLock(lockTemplate.getLock(lockName),
                () -> {
                    if (log.isTraceEnabled()) {
                        log.info("lock acquired lock={}", lockName);
                    }

                    return invocation.proceed();
                },
                (lock, throwable) -> whenLockFailed(throwable, lockName, method),
                lockAttr.getRetryTimes(),
                lockAttr.getWaitTime().toMillis(),
                lockAttr.getHoldTime().toMillis(),
                TimeUnit.MILLISECONDS
        );
    }


    /**
     * 加锁失败时的回调。
     * @param throwable 导致失败的异常。
     * @param lockName 锁名称，联锁则是锁名称数组拼接结果
     * @param method 加锁的方法
     * @return 如果返回了值，会阻止加锁失败异常的冒泡，导致逻辑降级
     */
    protected Object whenLockFailed(Throwable throwable, String lockName, Method method) {
        log.error("分布式锁加锁失败 lock={} method={}", lockName, method.getName());

        if (throwable instanceof RuntimeException){
            throw ((RuntimeException) throwable);
        } else {
            throw new IllegalStateException("lock acquire failed", throwable);
        }
    }


    /**
     * 执行加锁前，判断使用的锁名称计算器。
     * @param attribute 加锁设置
     * @return 锁名称计算器
     */
    protected NameEvaluator determineNameEvaluator(LockAttribute attribute) {
        if (attribute == null || this.beanFactory == null) {
            return getNameEvaluator();
        }

        // 优先使用注解上指定的evaluator
        String evaluatorName = attribute.getEvaluatorBeanName();
        if (StringUtils.isNotBlank(evaluatorName) && beanFactory.containsBean(evaluatorName)) {
            return beanFactory.getBean(evaluatorName, NameEvaluator.class);
        }

        // 然后考虑使用默认命名的evaluator
        if (StringUtils.isNotBlank((evaluatorName = this.nameEvaluatorBeanName))
                && beanFactory.containsBean(evaluatorName)) {
            return beanFactory.getBean(evaluatorName, NameEvaluator.class);
        }

        // 最后使用直接指定的evaluator，如果仍不存在，则从BeanFactory中找到NameEvaluator的实现
        NameEvaluator nameEvaluator = getNameEvaluator();
        if (nameEvaluator == null) {
            this.nameEvaluator = nameEvaluator = beanFactory.getBean(NameEvaluator.class);
        }

        return nameEvaluator;
    }


    /**
     * 用于执行方法的回调函数
     */
    @FunctionalInterface
    interface LockInvocation {
        Object proceed() throws Throwable;
    }

}
