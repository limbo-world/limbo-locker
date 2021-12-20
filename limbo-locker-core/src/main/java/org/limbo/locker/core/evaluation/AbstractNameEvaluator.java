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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 抽象锁名计算器，提供了单锁、联锁的表达式计算抽象，具体的表达式执行交给子类处理。
 *
 * @author Brozen
 * @since 1.0
 */
public abstract class AbstractNameEvaluator implements NameEvaluator {


    /**
     * {@inheritDoc}
     * @param context 锁名计算上下文
     * @return
     */
    @Override
    public NameEvaluateResult evaluate(NameEvaluationContext context) {

        Class<?> contextClass = context.getClass();
        if (SingleLockNameEvaluationContext.class.isAssignableFrom(contextClass)) {
            return evaluateSingleLockName((SingleLockNameEvaluationContext) context);
        } else if (MultiLockNameEvaluationContext.class.isAssignableFrom(contextClass)) {
            return evaluateMultiLockName(((MultiLockNameEvaluationContext) context));
        }

        throw new IllegalArgumentException("Cannot evaluate name for context type " + contextClass.getName());
    }


    /**
     * 计算单锁名称
     * @param context 锁名计算上下文
     * @return 单锁名称计算结果
     */
    protected NameEvaluateResult evaluateSingleLockName(SingleLockNameEvaluationContext context) {
        // 指定了name，直接使用
        if (StringUtils.isNotBlank(context.getName())) {
            return new NameEvaluateResult(context, context.getName());
        }

        // 未指定，则需要计算
        String name = evaluateLockName(context, context.getExpression());
        return new NameEvaluateResult(context, name);
    }


    /**
     * 计算联锁名称
     * @param context 锁名计算上下文
     * @return 单锁名称计算结果
     */
    protected NameEvaluateResult evaluateMultiLockName(MultiLockNameEvaluationContext context) {
        // 指定了name，直接使用
        if (CollectionUtils.isNotEmpty(context.getNames())) {
            return new NameEvaluateResult(context, context.getNames());
        }

        // 未指定，则需要计算
        String[] names = context.getExpressions().stream()
                .map(exp -> {
                    String name = evaluateLockName(context, exp);
                    context.addEvaluatedName(name);
                    return name;
                })
                .toArray(String[]::new);
        return new NameEvaluateResult(context, names);
    }


    /**
     * 执行锁名表达式，并发挥锁名计算结果
     * @param context 上下文
     * @param expression 表达式
     * @return 表达式执行结果
     */
    protected abstract String evaluateLockName(NameEvaluationContext context, String expression);

}
