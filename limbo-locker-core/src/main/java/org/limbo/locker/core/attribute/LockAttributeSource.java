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

import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * @author Brozen
 * @since 1.0
 */
public interface LockAttributeSource {

    /**
     * 返回方法对应的加锁属性，如果方法未指定加锁，则返回null
     * @param method 被代理的方法
     * @param targetClass 被代理方法所在类，可能为null(为甚吗?抄的org.springframework.transaction.attribute.TransactionAttributeSource#getTransactionAttribute)
     * @return 匹配的加锁属性，如未找到返回null
     */
    @Nullable
    LockAttribute getLockAttribute(Method method, Class<?> targetClass);


}
