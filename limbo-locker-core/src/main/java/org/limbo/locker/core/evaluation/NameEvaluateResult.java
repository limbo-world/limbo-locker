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

/**
 * @author Brozen
 * @since 1.0
 */
public class NameEvaluateResult {

    private final NameEvaluationContext context;

    private final Object name;

    public NameEvaluateResult(NameEvaluationContext context, Object name) {
        this.context = context;
        this.name = name;
    }

    /**
     * 获取锁名称解析上下文
     */
    public NameEvaluationContext getContext() {
        return context;
    }


    /**
     * 获取计算后的锁名称
     */
    public Object getEvaluatedName() {
        return name;
    }

}
