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

package microservice.web.resource;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import microservice.model.ThingEntity;
import microservice.web.ThingController;

@Component
public class ThingResourceAssembler extends ResourceAssemblerSupport<ThingEntity, ResourceSupport> {

	public ThingResourceAssembler() {
		super(ThingController.class, ResourceSupport.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.hateoas.ResourceAssembler#toResource(java.lang.Object)
	 */
	@Override
	public ResourceSupport toResource(ThingEntity entity) {
		if (entity == null) return null;
		ControllerLinkBuilder selfLinkBuilder = linkTo(methodOn(ThingController.class).find(entity.getId()));
		ControllerLinkBuilder allLinkBuilder = linkTo(methodOn(ThingController.class).findMatching(null, null));
		return new ThingResource(entity,
				selfLinkBuilder.withSelfRel(),
				selfLinkBuilder.withRel(ThingResource.REL_ITEM),
				allLinkBuilder.withRel(ThingResource.REL_COL));
	}
}
