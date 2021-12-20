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

import org.limbo.locker.core.evaluation.AbstractNameEvaluator;
import org.limbo.locker.core.evaluation.NameEvaluationContext;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 基于SpEL脚本实现的锁名计算器。
 *
 * @author Brozen
 * @since 1.0
 */
public class SpELNameEvaluator extends AbstractNameEvaluator {


    private final CachedSpELExpressionEvaluator evaluator = new CachedSpELExpressionEvaluator();


    /**
     * {@inheritDoc}
     * @param context 上下文
     * @param expression 表达式
     * @return
     */
    @Override
    protected String evaluateLockName(NameEvaluationContext context, String expression) {
        Method method = context.getMethod();
        MethodBasedEvaluationContext spelContext = createEvaluationContext(context, method, context.getArgs());
        Object returnValue = evaluator.evaluate(expression, context, method, spelContext);
        Objects.requireNonNull(returnValue, () -> "SpEL [" + expression + "] returns null value on method " + method.getName());

        return returnValue.toString();

    }


    /**
     * 创建一个基于调用方法的表达式执行上下文，并在上下文中设置 args 变量，表示参数数组。
     * 设置变量 arg0 arg1 arg2 表示前三个参数。参数不足三个时，超过的变量不设置。
     *
     * @param context 表达式计算上下文
     * @param method 被调用的方法
     * @param args 方法参数
     */
    private MethodBasedEvaluationContext createEvaluationContext(NameEvaluationContext context, Method method, Object[] args) {
        // 先读取缓存下来的context
        Object nativeContext = context.getNativeContext();
        if (nativeContext != null) {
            return ((MethodBasedEvaluationContext) nativeContext);
        }

        // 创建新的context
        MethodBasedEvaluationContext evaluationContext
                = new MethodBasedEvaluationContext(context, method, args, new DefaultParameterNameDiscoverer());
        evaluationContext.setVariable("args", args);

        // 为参数生成别名
        for (int i = 0; i < args.length; i++) {
            if (i > 3) {
                break;
            }
            evaluationContext.setVariable("arg" + i, args[i]);
        }

        // 缓存context
        context.setNativeContext(evaluationContext);
        return evaluationContext;
    }


}
