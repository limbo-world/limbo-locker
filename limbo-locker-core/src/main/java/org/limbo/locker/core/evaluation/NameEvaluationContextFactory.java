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

import org.limbo.locker.core.attribute.LockAttribute;

import java.lang.reflect.Method;

/**
 * @author Brozen
 * @since 1.0
 */
public interface NameEvaluationContextFactory {

    /**
     * 生成锁名计算上下文
     * @param method 执行的方法
     * @param targetClass 执行方法所在类
     * @param args 方法执行参数
     * @param lockAttribute 加锁配置
     * @return 锁名计算上下文
     */
    NameEvaluationContext createNameEvaluationContext(Method method, Class<?> targetClass, Object[] args, LockAttribute lockAttribute);

}
