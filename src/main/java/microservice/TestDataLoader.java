/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package microservice;

import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import microservice.model.ThingEntity;
import microservice.repository.ThingRepository;

@ConditionalOnProperty("microservice.add-test-data")
@Component
public class TestDataLoader implements ApplicationListener<ContextRefreshedEvent> {
	
    private final static Logger LOG = LoggerFactory.getLogger(TestDataLoader.class);

    @Autowired
    private ConfigProperties config;
    
    @Autowired
    private ThingRepository repo;

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        LOG.info("Loading test data");

        IntStream.rangeClosed(1, config.getTestRecordsQuantity()).forEach(idx -> repo.save(new ThingEntity("THING_" + idx, "John Doe")));
    }
}
