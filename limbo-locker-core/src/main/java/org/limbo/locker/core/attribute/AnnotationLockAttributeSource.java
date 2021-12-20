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

import org.limbo.locker.core.annotations.Locked;
import org.limbo.locker.core.annotations.MultiLocked;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.time.Duration;

/**
 * @author Brozen
 * @since 1.0
 */
public class AnnotationLockAttributeSource extends AbstractLockAttributeSource {

    /**
     * {@inheritDoc}
     * @param targetClass 类类型
     * @return
     */
    @Override
    protected LockAttribute findLockAttribute(Class<?> targetClass) {
        return determineLockAttribute(targetClass);
    }


    /**
     * {@inheritDoc}
     * @param method 方法
     * @return
     */
    @Override
    protected LockAttribute findLockAttribute(Method method) {
        return determineLockAttribute(method);
    }


    /**
     * 解析加锁注解，生成加锁属性。如果不存在加锁注解，则返回null。
     * @param element 注解所在的属性，可能是方法或类型
     * @return 注解对应的加锁属性，不对应加锁属性则返回null
     */
    private LockAttribute determineLockAttribute(AnnotatedElement element) {

        // 首先解析单锁，也就是 Locked 注解
        AnnotationAttributes attributes = AnnotatedElementUtils.findMergedAnnotationAttributes(
                element, Locked.class, false, false);
        if (attributes != null) {
            return wrapSingleLockAttribute(attributes);
        }

        // 不存在单锁，则解析联锁，也就是 MultiLocked 注解
        attributes = AnnotatedElementUtils.findMergedAnnotationAttributes(
                element, MultiLocked.class, false, false);
        if (attributes != null) {
            return wrapMultiLockAttribute(attributes);
        }

        return null;
    }


    /**
     * 封装联锁的加锁属性
     * @param attributes {@link MultiLocked}注解属性
     * @return 加锁属性
     */
    private LockAttribute wrapMultiLockAttribute(AnnotationAttributes attributes) {
        MultiLockAttributes mla = new MultiLockAttributes();
        mla.setLockNames(attributes.getStringArray("names"));
        mla.setLockNameExpressions(attributes.getStringArray("expressions"));
        mla.setLockType(LockDefinition.LOCK_TYPE_MULTI);

        extractLockAttribute(attributes, mla);
        return mla;
    }


    /**
     * 封装单锁的加锁属性
     * @param attributes {@link Locked}注解属性
     * @return 加锁属性
     */
    private LockAttribute wrapSingleLockAttribute(AnnotationAttributes attributes) {
        SingleLockAttributes sla = new SingleLockAttributes();
        sla.setLockName(attributes.getString("name"));
        sla.setLockNameExpression(attributes.getString("expression"));
        sla.setLockType(LockDefinition.LOCK_TYPE_SINGLE);

        extractLockAttribute(attributes, sla);
        return sla;
    }


    /**
     * 抽取加锁配置中，联锁和单锁的相同部分
     */
    private void extractLockAttribute(AnnotationAttributes attributes, LockAttribute lockAttr) {
        lockAttr.setBlock(attributes.getBoolean("block"));
        lockAttr.setWaitTime(Duration.ofMillis(attributes.getNumber("waitTime")));
        lockAttr.setHoldTime(Duration.ofMillis(attributes.getNumber("holdTime")));
        lockAttr.setRetryTimes(attributes.getNumber("retryTimes"));
        lockAttr.setEvaluatorBeanName(attributes.getString("evaluator"));
    }

}
