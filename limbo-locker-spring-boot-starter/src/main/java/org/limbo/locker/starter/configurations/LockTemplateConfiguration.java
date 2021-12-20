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
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Brozen
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
public class LockTemplateConfiguration {


    @Bean
    @Primary
    public LockTemplate lockTemplate(RedissonClient redisson) {
        LockTemplate template = new LockTemplate();
        template.setRedisson(redisson);
        return template;
    }


    @Bean
    public MultiLockTemplate multiLockTemplate(RedissonClient redisson) {
        MultiLockTemplate template = new MultiLockTemplate();
        template.setRedisson(redisson);
        return template;
    }

}
