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

package org.limbo.locker.core.evaluation;

import org.limbo.locker.core.attribute.MultiLockAttributes;
import org.limbo.locker.core.attribute.SingleLockAttributes;

import java.lang.reflect.Method;

/**
 * @author Brozen
 * @since 1.0
 */
public class DefaultNameEvaluationContextFactory extends AbstractNameEvaluationContextFactory {

    /**
     * {@inheritDoc}
     * @param method 执行的方法
     * @param targetClass 执行方法所在类
     * @param args 方法执行参数
     * @param lockAttribute 加锁配置
     * @return
     */
    @Override
    protected SingleLockNameEvaluationContext createSingleLockNameEvaluationContext(
            Method method, Class<?> targetClass, Object[] args, SingleLockAttributes lockAttribute) {
        SingleLockNameEvaluationContext context = new SingleLockNameEvaluationContext();
        context.setMethod(method);
        context.setTargetClass(targetClass);
        context.setArgs(args);
        context.setLockAttribute(lockAttribute);
        context.setName(lockAttribute.getLockName());
        context.setExpression(lockAttribute.getLockNameExpression());
        return context;
    }


    /**
     * {@inheritDoc}
     * @param method 执行的方法
     * @param targetClass 执行方法所在类
     * @param args 方法执行参数
     * @param lockAttribute 加锁配置
     * @return
     */
    @Override
    protected MultiLockNameEvaluationContext createMultiLockNameEvaluationContext(
            Method method, Class<?> targetClass, Object[] args, MultiLockAttributes lockAttribute) {
        MultiLockNameEvaluationContext context = new MultiLockNameEvaluationContext();
        context.setMethod(method);
        context.setTargetClass(targetClass);
        context.setArgs(args);
        context.setLockAttribute(lockAttribute);
        context.setNames(lockAttribute.getLockNames());
        context.setExpressions(lockAttribute.getLockNameExpressions());
        return context;
    }


}
