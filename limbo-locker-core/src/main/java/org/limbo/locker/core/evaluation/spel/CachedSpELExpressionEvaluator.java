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

package org.limbo.locker.core.evaluation.spel;

import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 带缓存的SpEL表达式执行器。会将编译后的SpEL {@link Expression}缓存下来。
 */
class CachedSpELExpressionEvaluator extends CachedExpressionEvaluator {

    private final Map<ExpressionKey, Expression> EXPRESSION_CACHE = new ConcurrentHashMap<>(64);

    /**
     * 执行SpEL表达式，并返回表达式执行结果
     *
     * @param express SpEL表达式
     * @param root    SpEL表达式环境中的this
     * @param method  切点方法
     * @param context SpEL执行上下文
     * @return 表达式执行结果
     */
    public Object evaluate(String express, Object root, Method method, EvaluationContext context) {
        return getExpression(EXPRESSION_CACHE, new AnnotatedElementKey(method, root.getClass()), express).getValue(context);
    }

}
