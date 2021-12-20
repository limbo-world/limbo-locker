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
 * 分布式锁注解
 *
 * @author Brozen
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Locked {

    /**
     * 锁名，固定名，会覆盖{@link #expression()}配置
     */
    String name() default "";

    /**
     * 锁名，SpEL表达式计算
     */
    String expression() default "";

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

}
