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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Brozen
 * @since 2021-12-20
 */
@RestController
@RequestMapping("/test")
public class TestLockController {

    @Autowired
    private TestLockService lockService;


    @GetMapping("/lock")
    public String lock() {
        return lockService.lock();
    }

    @GetMapping("/lock/{id}")
    public String lock(@PathVariable("id") String id) {
        return lockService.lock(id);
    }

    @GetMapping("/lock/{id1}/{id2}")
    public String lock(@PathVariable("id1") String id1, @PathVariable("id2") String id2) {
        return lockService.lock(id1, id2);
    }

    @GetMapping("/multi-lock/{id1}/{id2}")
    public String multiLock(@PathVariable("id1") String id1, @PathVariable("id2") String id2) {
        return lockService.multiLock(id1, id2);
    }

}
