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

package org.limbo.locker.starter.configurations;

import org.limbo.locker.core.LockTemplate;
import org.limbo.locker.core.MultiLockTemplate;
import org.limbo.locker.core.attribute.AnnotationLockAttributeSource;
import org.limbo.locker.core.attribute.LockAttributeSource;
import org.limbo.locker.core.evaluation.DefaultNameEvaluationContextFactory;
import org.limbo.locker.core.evaluation.NameEvaluationContextFactory;
import org.limbo.locker.core.evaluation.NameEvaluator;
import org.limbo.locker.core.evaluation.spel.SpELNameEvaluator;
import org.limbo.locker.core.interceptor.LockAdvisor;
import org.limbo.locker.core.interceptor.LockInterceptor;
import org.limbo.locker.starter.AbstractLockerConfiguration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * @author Brozen
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class ProxyLockerConfiguration extends AbstractLockerConfiguration {


    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LockAdvisor lockAdvisor(LockAttributeSource lockAttributeSource, LockInterceptor lockInterceptor) {
        LockAdvisor advisor = new LockAdvisor();
        advisor.setLockAttributeSource(lockAttributeSource);
        advisor.setAdvice(lockInterceptor);

        if (enableLocker != null) {
            advisor.setOrder(enableLocker.getNumber("aspectOrder"));
        }

        return advisor;
    }


    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LockAttributeSource lockAttributeSource() {
        return new AnnotationLockAttributeSource();
    }


    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public NameEvaluator nameEvaluator() {
        return new SpELNameEvaluator();
    }


    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public NameEvaluationContextFactory nameEvaluationContextFactory() {
        return new DefaultNameEvaluationContextFactory();
    }


    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LockInterceptor lockInterceptor(NameEvaluator nameEvaluator, LockAttributeSource lockAttributeSource,
                                           LockTemplate lockTemplate, MultiLockTemplate multiLockTemplate,
                                           NameEvaluationContextFactory nameEvaluationContextFactory) {
        LockInterceptor interceptor = new LockInterceptor();
        interceptor.setNameEvaluator(nameEvaluator);
        interceptor.setLockAttributeSource(lockAttributeSource);
        interceptor.setLockTemplate(lockTemplate);
        interceptor.setMultiLockTemplate(multiLockTemplate);
        interceptor.setNameEvaluationContextFactory(nameEvaluationContextFactory);
        return interceptor;
    }


}
