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

package microservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import microservice.model.ThingEntity;
import microservice.repository.ThingRepository;

@Service
public class AppServiceImpl implements AppService {

	private static <ENTITY_T> ENTITY_T findEntity(JpaRepository<ENTITY_T, Long> repo, Class<ENTITY_T> entityClass, Long id) {
		if (id == null)
			throw new EntityNotSpecifiedException(entityClass);
		ENTITY_T result = repo.findOne(id);
		if (result == null)
			throw new EntityNotFoundException(entityClass, id);
		return result;
	}

	@Autowired
	private ThingRepository repo;

	@Override
	public ThingEntity updateThing(Long id, String name, String user) {
		ThingEntity entity = findEntity(repo, ThingEntity.class, id);
		entity.setName(name);
		return repo.saveAndFlush(entity);
	}
}

