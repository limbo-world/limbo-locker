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

package org.limbo.locker.starter;

import org.limbo.locker.starter.annotations.EnableLocker;
import org.limbo.locker.starter.configurations.LockTemplateConfiguration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * @author Brozen
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public abstract class AbstractLockerConfiguration extends LockTemplateConfiguration implements ImportAware {

    protected AnnotationAttributes enableLocker;


    /**
     * 读取应用中 {@link EnableLocker} 注解的配置
     */
    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        Map<String, Object> attrMap = importMetadata
                .getAnnotationAttributes(EnableLocker.class.getName(), false);
        this.enableLocker = AnnotationAttributes.fromMap(attrMap);

        if (this.enableLocker == null) {
            throw new IllegalArgumentException("@EnableLocker is not present on importing class " + importMetadata.getClassName());
        }
    }

}
