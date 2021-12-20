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
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Brozen
 * @since 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MultiLockNameEvaluationContext extends NameEvaluationContext {

    /**
     * 锁名称
     */
    private List<String> names;

    /**
     * 锁名表达式
     */
    private List<String> expressions;

    /**
     * 锁名表达式计算后的值
     */
    private List<String> evaluatedNames;


    /**
     * 新增一个锁名计算结果。前面的表达式计算完成后，结果可以在后面的表达式中使用
     */
    public void addEvaluatedName(String evaluatedName) {
        if (this.evaluatedNames == null) {
            this.evaluatedNames = new ArrayList<>();
        }

        this.evaluatedNames.add(evaluatedName);
    }

}
