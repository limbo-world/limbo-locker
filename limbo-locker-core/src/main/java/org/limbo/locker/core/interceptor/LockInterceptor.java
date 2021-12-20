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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.limbo.locker.core.attribute.LockAttributeSource;
import org.limbo.locker.core.evaluation.NameEvaluator;
import org.springframework.aop.support.AopUtils;

/**
 * @author Brozen
 * @since 1.0
 */
public class LockInterceptor extends LockAspectSupport implements MethodInterceptor {

    public LockInterceptor() {
    }

    public LockInterceptor(NameEvaluator nameEvaluator, LockAttributeSource lockAttributeSource) {
        setNameEvaluator(nameEvaluator);
        setLockAttributeSource(lockAttributeSource);
    }


    /**
     * {@inheritDoc}
     * @param invocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        // 当注解打在静态方法上时，invocation.getThis()可能返回null
        Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);

        return invokeInLock(invocation.getMethod(), targetClass, invocation.getArguments(), invocation::proceed);
    }


}
