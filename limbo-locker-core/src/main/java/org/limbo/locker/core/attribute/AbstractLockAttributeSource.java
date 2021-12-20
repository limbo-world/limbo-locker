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

package org.limbo.locker.core.attribute;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.MethodClassKey;
import org.springframework.util.ClassUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Brozen
 * @since 1.0
 */
@Slf4j
public abstract class AbstractLockAttributeSource implements LockAttributeSource {

    private static final LockAttribute NULL_ATTR = new LockAttribute() {
        @Override
        public String toString() {
            return "null";
        }
    };

    /**
     * 缓存下来属性
     */
    private final Map<Object, LockAttribute> attrCache = new ConcurrentHashMap<>(1024);


    /**
     * {@inheritDoc}
     * @param method 被代理的方法
     * @param targetClass 被代理方法所在类，可能为null
     * @return
     */
    @Nullable
    @Override
    public LockAttribute getLockAttribute(Method method, Class<?> targetClass) {
        if (method.getDeclaringClass() == Object.class) {
            return null;
        }

        // 检查缓存中是否存在，不存在则生成，存在直接返回
        MethodClassKey cacheKey = new MethodClassKey(method, targetClass);
        LockAttribute foundAttr = attrCache.computeIfAbsent(cacheKey, _k -> {
            LockAttribute attr = computeLockAttribute(method, targetClass);

            // 防止Map不支持null value
            if (attr == null) {
                attr = NULL_ATTR;
            }

            if (log.isTraceEnabled()) {
                String methodIdentification = ClassUtils.getQualifiedMethodName(method, targetClass);
                log.trace("Adding locked method '" + methodIdentification + "' with attribute: " + attr);
            }

            return attr;
        });

        // 转换null值
        return foundAttr == NULL_ATTR ? null : foundAttr;
    }


    /**
     * 生成方法对应的加锁属性，如果方法未指定锁应返回null。此方法无需处理缓存
     * @param method 被代理的方法
     * @param targetClass 被代理方法所在类，可能为null
     * @return 匹配的加锁属性，如未找到返回null
     */
    protected LockAttribute computeLockAttribute(Method method, Class<?> targetClass) {
        // 注解可能加在接口方法上，此时对实现类方法也生效
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);

        // 首先从 接口/父类 方法上找到加锁属性
        LockAttribute attr = findLockAttribute(specificMethod);
        if (attr != null) {
            return attr;
        }

        // 或从 接口/父类 上找到加锁属性
        attr = findLockAttribute(specificMethod.getDeclaringClass());
        if (attr != null && ClassUtils.isUserLevelMethod(method)) {
            return attr;
        }

        if (specificMethod != method) {
            // 从实现类方法上找到加锁属性
            attr = findLockAttribute(method);
            if (attr != null) {
                return attr;
            }

            // 从实现类上找到加锁属性
            attr = findLockAttribute(method.getDeclaringClass());
            if (attr != null && ClassUtils.isUserLevelMethod(method)) {
                return attr;
            }
        }

        return null;
    }


    /**
     * 从类上解析出加锁属性
     * @param targetClass 类类型
     * @return 对应的加锁属性，如果不存在返回null
     */
    protected abstract LockAttribute findLockAttribute(Class<?> targetClass);


    /**
     * 从方法上解析出加锁属性
     * @param method 方法
     * @return 对应的加锁属性，如果不存在返回null
     */
    protected abstract LockAttribute findLockAttribute(Method method);

}
