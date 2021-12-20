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

import lombok.Data;
import org.limbo.locker.core.attribute.LockAttribute;

import java.lang.reflect.Method;

/**
 * @author Brozen
 * @since 1.0
 */
@Data
public abstract class NameEvaluationContext {

    /**
     * 加锁配置
     */
    private LockAttribute lockAttribute;

    /**
     * 加锁的方法
     */
    private Method method;

    /**
     * 加锁方法所在注解
     */
    private Class<?> targetClass;

    /**
     * 加锁方法执行参数
     */
    private Object[] args;

    /**
     * 表达式执行的底层context
     */
    private Object nativeContext;

}
