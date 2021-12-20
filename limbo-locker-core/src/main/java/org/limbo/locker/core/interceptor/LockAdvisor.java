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

import lombok.Setter;
import org.limbo.locker.core.attribute.LockAttributeSource;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;

import javax.annotation.Nonnull;

/**
 * 锁切面定义。
 *
 * @author Brozen
 * @since 1.0
 */
public class LockAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    @Setter
    private LockAttributeSource lockAttributeSource;

    /**
     * 切点
     */
    private final LockPointcut pointcut = new LockPointcut() {
        @Override
        protected LockAttributeSource getLockAttributeSource() {
            return lockAttributeSource;
        }
    };


    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    @Nonnull
    public Pointcut getPointcut() {
        return pointcut;
    }

}
