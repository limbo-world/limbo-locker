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

package org.limbo.locker.demo;

import lombok.extern.slf4j.Slf4j;
import org.limbo.locker.core.annotations.Locked;
import org.limbo.locker.core.annotations.MultiLocked;
import org.limbo.utils.UUIDUtils;
import org.springframework.stereotype.Service;

/**
 * @author Brozen
 * @since 2021-12-20
 */
@Slf4j
@Service
public class TestLockService {


    @Locked(name = "thisIsALock")
    public String lock() {
        log.info("在方法内");
        return UUIDUtils.randomID();
    }


    @Locked(expression = "'thisIsALock:' + #id ", waitTime = 4000)
    public String lock(String id) {
        log.info("在方法内 id={}", id);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return UUIDUtils.randomID();
    }


    @Locked(expression = "'thisIsALock:' + #id1 + ':' + #id2 ", waitTime = 4000)
    public String lock(String id1, String id2) {
        log.info("在方法内 id1={} id2={}", id1, id2);
        return UUIDUtils.randomID();
    }

    @MultiLocked(expressions = {
            "'thisIsALock:' + #id1", "'thisIsALock:' + #id2"
    }, waitTime = 4000, holdTime = 20000)
    public String multiLock(String id1, String id2) {
        log.info("在联锁方法内 id1={} id2={}", id1, id2);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return UUIDUtils.randomID();
    }





}
