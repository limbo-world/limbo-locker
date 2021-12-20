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

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.locker.core.annotations.MultiLocked;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Brozen
 * @since 1.0
 */
@Getter
@Setter
@ToString(callSuper = true)
public class MultiLockAttributes extends LockAttribute {

    /**
     * 锁名称，此属性指定时，{@link #lockNameExpressions}将无效
     */
    private List<String> lockNames;

    /**
     * 锁名称表达式，SpEL表达式
     */
    private List<String> lockNameExpressions;

    /**
     * 是否自动为联锁名排序，联锁加锁时，保证按照一定的逻辑顺序加锁，能够防止死锁。<br/>
     * 关于死锁，请参考{@link MultiLocked}
     */
    private boolean autoSortNames;


    public MultiLockAttributes() {
    }


    /**
     * 设置联锁锁名称，同{@link #setLockNames(List)}
     */
    public void setLockNames(String[] names) {
        setLockNames(Arrays.asList(names));
    }


    /**
     * 设置联锁锁名称
     */
    public void setLockNames(List<String> lockNames) {
        this.lockNames = lockNames.stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(this.lockNames)) {
            this.lockNameExpressions = Lists.newArrayList();
        }
    }


    /**
     * 设置联锁名称表达式，同{@link #setLockNameExpressions(List)}
     */
    public void setLockNameExpressions(String[] expressions) {
        setLockNameExpressions(Arrays.asList(expressions));
    }


    /**
     * 设置联锁名称表达式
     */
    public void setLockNameExpressions(List<String> lockNameExpressions) {
        if (CollectionUtils.isEmpty(this.lockNames)) {
            this.lockNameExpressions = lockNameExpressions.stream()
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
        }
    }
}
