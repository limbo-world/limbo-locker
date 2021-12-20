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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.limbo.locker.core.annotations.Locked;

import java.time.Duration;

/**
 * @author Brozen
 * @since 1.0
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SingleLockAttributes extends LockAttribute {

    /**
     * 锁名称，此属性指定时，{@link #lockNameExpression}将无效
     */
    private String lockName;

    /**
     * 锁名称表达式，SpEL表达式
     */
    private String lockNameExpression;


    public SingleLockAttributes() {
    }

    public SingleLockAttributes(Locked locked) {
        setBlock(locked.block());
        setWaitTime(Duration.ofMillis(locked.waitTime()));
        setRetryTimes(locked.retryTimes());
        setHoldTime(Duration.ofMillis(locked.holdTime()));

        String lockName = locked.name();
        if (StringUtils.isBlank(lockName)) {
            this.lockName = null;

            if (StringUtils.isNotBlank(locked.expression())) {
                this.lockNameExpression = locked.expression();
            } else {
                throw new IllegalStateException("lockName和expression均未指定！");
            }
        } else {
            this.lockName = lockName;
            this.lockNameExpression = null;
        }
    }


    /**
     * 设置锁名
     */
    public void setLockName(String lockName) {
        this.lockName = lockName;
        if (StringUtils.isNotBlank(this.lockName)) {
            this.lockNameExpression = null;
        }
    }

    /**
     * 设置锁名称表达式
     */
    public void setLockNameExpression(String lockNameExpression) {
        if (StringUtils.isBlank(this.lockName)) {
            this.lockNameExpression = lockNameExpression;
        }
    }
}
