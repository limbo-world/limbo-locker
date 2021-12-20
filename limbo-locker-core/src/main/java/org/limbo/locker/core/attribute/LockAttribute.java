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

import java.time.Duration;

/**
 * @author Brozen
 * @since 1.0
 */
@Getter
@Setter
@ToString
public abstract class LockAttribute implements LockDefinition {

    /**
     * 加锁类型，{@link #LOCK_TYPE_SINGLE 单锁}或{@link #LOCK_TYPE_MULTI 联锁}
     */
    protected int lockType;

    /**
     * 成功获取到锁之前是否阻塞等待。为true时将阻塞等待获取到锁，此时重试失效。为false则快速失败，会重试一定次数，且在超出最大重试次数后抛出异常。、
     * 不建议配置为true，默认为false
     */
    protected boolean block;

    /**
     * 在尝试获取到锁之前等待多久，单位毫秒。小于等于0时，将快速失败，不等待。block=false时生效。
     */
    protected Duration waitTime;

    /**
     * 尝试加锁失败后的重试次数。小于等于0时不重试，直接抛出异常。block=false时生效。
     */
    protected int retryTimes;

    /**
     * 在获取到锁之后，多久自动释放(即使没有执行完业务)，单位毫秒。小于等于0时，在业务方法执行完成前将不自动释放锁。
     */
    protected Duration holdTime;

    /**
     * 锁名计算器的Bean名称
     */
    protected String evaluatorBeanName;

}
