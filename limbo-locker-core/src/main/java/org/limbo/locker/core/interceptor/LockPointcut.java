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

import org.limbo.locker.core.attribute.LockAttribute;
import org.limbo.locker.core.attribute.LockAttributeSource;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

/**
 * 需要进行锁代理的切点定义。
 *
 * @author Brozen
 * @since 1.0
 */
abstract class LockPointcut extends StaticMethodMatcherPointcut {


    protected abstract LockAttributeSource getLockAttributeSource();


    /**
     * 能解析出{@link LockAttribute}的方法需要被代理。
     * @param method 方法
     * @param targetClass 方法所在类
     * @return 方法是否需要当做切点处理
     */
    @Override
    public boolean matches(@Nonnull Method method, @Nonnull Class<?> targetClass) {
        LockAttributeSource lockAttributeSource = getLockAttributeSource();
        return lockAttributeSource != null && lockAttributeSource.getLockAttribute(method, targetClass) != null;
    }

}
