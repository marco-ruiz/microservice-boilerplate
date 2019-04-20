/*
 * Copyright 2002-2018 the original author or authors.
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

package microservice.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Marco Ruiz
 */
@Repository
public class LuceneRepositoryImpl<ENTITY_T> implements LuceneRepositoryCustom<ENTITY_T> {

    @PersistenceContext
    private EntityManager em;

    //===============
    // LUCENE SEARCH
    //===============

	@Override
	@Transactional
    public void buildTextIndex() throws InterruptedException {
    	getFullTextEntityManager().createIndexer().startAndWait();
    }

	@SuppressWarnings("unchecked")
	public List<ENTITY_T> find(String field, String matchingText, Class<ENTITY_T> entityClass) {
    	Query query = getQueryBuilder(entityClass)
    			.simpleQueryString()
    			.onFields(field)
    			.matching(matchingText)
    			.createQuery();
    	
    	return getJpaQuery(query, entityClass).getResultList();
    }
    
    private FullTextQuery getJpaQuery(Query luceneQuery, Class<?> entityClass) {
        FullTextQuery result = getFullTextEntityManager().createFullTextQuery(luceneQuery, entityClass);
/*        
    	result.setProjection(
    			FullTextQuery.SCORE, 
    			FullTextQuery.EXPLANATION, 
    			FullTextQuery.THIS, 
    			FullTextQuery.DOCUMENT);
*/
		return result;
    }

    private QueryBuilder getQueryBuilder(Class<?> entityClass) {
        return getFullTextEntityManager().getSearchFactory()
            .buildQueryBuilder()
            .forEntity(entityClass)
            .get();
    }
    
	private FullTextEntityManager getFullTextEntityManager() {
		return Search.getFullTextEntityManager(em);
	}
}
