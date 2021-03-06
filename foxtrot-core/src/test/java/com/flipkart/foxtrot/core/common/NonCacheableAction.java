/**
 * Copyright 2014 Flipkart Internet Pvt. Ltd.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flipkart.foxtrot.core.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.foxtrot.common.ActionResponse;
import com.flipkart.foxtrot.core.alerts.EmailConfig;
import com.flipkart.foxtrot.core.cache.CacheManager;
import com.flipkart.foxtrot.core.datastore.DataStore;
import com.flipkart.foxtrot.core.exception.FoxtrotException;
import com.flipkart.foxtrot.core.exception.MalformedQueryException;
import com.flipkart.foxtrot.core.querystore.QueryStore;
import com.flipkart.foxtrot.core.querystore.actions.spi.AnalyticsLoader;
import com.flipkart.foxtrot.core.querystore.actions.spi.AnalyticsProvider;
import com.flipkart.foxtrot.core.querystore.impl.ElasticsearchConnection;
import com.flipkart.foxtrot.core.table.TableMetadataManager;
import com.google.common.annotations.VisibleForTesting;
import org.elasticsearch.action.search.SearchRequestBuilder;

/**
 * Created by rishabh.goyal on 02/05/14.
 */
@VisibleForTesting
@AnalyticsProvider(opcode = "testCacheableFalse", request = NonCacheableActionRequest.class, response = NonCacheableActionResponse.class,
                   cacheable = false)
public class NonCacheableAction extends Action<NonCacheableActionRequest> {

    public NonCacheableAction(NonCacheableActionRequest parameter, TableMetadataManager tableMetadataManager, DataStore dataStore,
                              QueryStore queryStore, ElasticsearchConnection connection, String cacheToken, CacheManager cacheManager,
                              ObjectMapper objectMapper, EmailConfig emailConfig, AnalyticsLoader analyticsLoader) {
        super(parameter, tableMetadataManager, dataStore, queryStore, connection, cacheToken, cacheManager, objectMapper, emailConfig);
    }

    @Override
    public void preprocess() {

    }

    @Override
    public String getMetricKey() {
        return "TEST";
    }

    @Override
    public String getRequestCacheKey() {
        return "TEST_CACHE_KEY";
    }

    @Override
    public void validateImpl(NonCacheableActionRequest parameter) throws MalformedQueryException {

    }

    @Override
    public SearchRequestBuilder getRequestBuilder(NonCacheableActionRequest parameter) throws FoxtrotException {
        return null;
    }

    @Override
    public ActionResponse getResponse(org.elasticsearch.action.ActionResponse response, NonCacheableActionRequest parameter)
            throws FoxtrotException {
        return null;
    }

    @Override
    public ActionResponse execute(NonCacheableActionRequest parameter) throws FoxtrotException {
        return null;//new NonCacheableActionResponse();
    }
}
