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

package org.limbo.locker.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 联锁注解。<br/>
 * 需注意，使用联锁时，应当将多个锁的锁定范围从大到小排列，防止死锁，例如先锁会议ID，再锁参会人UID。<br/>
 *
 * 锁范围相同时，不建议使用联锁，除非调用者清楚死锁原理，并保证联锁的多个锁使用某种固定的排序方式排列。<br/>
 * 死锁原理解释：
 * 假设线程1，需依次锁定资源A、C、B；假设线程2，需依次锁定资源C、A、B；两个线程并发执行时，
 * 可能会出现线程1锁定资源A后，CPU调度给线程2执行；线程2锁定资源C后，尝试获取资源A，发现A被锁定，阻塞等待；
 * 线程1再次获取到CPU后，尝试锁定资源C，发现C被锁定，阻塞等待，此时出现等待环，发生死锁。
 *
 * 解决办法，联锁锁定资源时，将资源按某种方式排序，如上例，线程1、线程2均按照 A、B、C的顺序加锁的话，则不会出现死锁。
 *
 * @author Brozen
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface MultiLocked {

    /**
     * 锁名，固定名，会覆盖{@link #expressions()}配置
     */
    String[] names() default {};

    /**
     * 锁名，SpEL表达式计算
     */
    String[] expressions() default {};

    /**
     * 是否在成功获取到锁之前都阻塞。为true时将阻塞等待获取到锁，此时重试失效。为false则快速失败，会重试一定次数，且在超出最大重试次数后抛出异常。不建议配置为true，默认为false
     */
    boolean block() default false;

    /**
     * 在尝试获取到锁之前等待多久，单位毫秒。小于等于0时，将快速失败，不等待。block=false时生效。默认-1。
     */
    long waitTime() default -1;

    /**
     * 在获取到锁之后，多久自动释放(即使没有执行完业务)，单位毫秒。小于等于0时，在业务方法执行完成前将不自动释放锁。默认10000ms。
     */
    long holdTime() default 10000;

    /**
     * 尝试加锁失败后的重试次数。小于等于0时不重试，直接抛出异常。block=false时生效。默认3。
     */
    int retryTimes() default 3;

    /**
     * 锁名计算器的Bean名称
     */
    String evaluator() default "";

    /**
     * 是否自动为联锁名排序，联锁加锁时，保证按照一定的逻辑顺序加锁，能够防止死锁
     */
    boolean autoSortNames() default false;

}
