package com.flipkart.foxtrot.core.querystore.actions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.foxtrot.common.ActionResponse;
import com.flipkart.foxtrot.common.count.CountRequest;
import com.flipkart.foxtrot.common.count.CountResponse;
import com.flipkart.foxtrot.common.query.Filter;
import com.flipkart.foxtrot.common.query.general.ExistsFilter;
import com.flipkart.foxtrot.common.util.CollectionUtils;
import com.flipkart.foxtrot.core.alerts.EmailConfig;
import com.flipkart.foxtrot.core.cache.CacheManager;
import com.flipkart.foxtrot.core.common.Action;
import com.flipkart.foxtrot.core.datastore.DataStore;
import com.flipkart.foxtrot.core.exception.FoxtrotException;
import com.flipkart.foxtrot.core.exception.FoxtrotExceptions;
import com.flipkart.foxtrot.core.exception.MalformedQueryException;
import com.flipkart.foxtrot.core.querystore.QueryStore;
import com.flipkart.foxtrot.core.querystore.actions.spi.AnalyticsLoader;
import com.flipkart.foxtrot.core.querystore.actions.spi.AnalyticsProvider;
import com.flipkart.foxtrot.core.querystore.impl.ElasticsearchConnection;
import com.flipkart.foxtrot.core.querystore.impl.ElasticsearchUtils;
import com.flipkart.foxtrot.core.querystore.query.ElasticSearchQueryGenerator;
import com.flipkart.foxtrot.core.table.TableMetadataManager;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;

import java.util.ArrayList;
import java.util.List;

import static com.flipkart.foxtrot.core.util.ElasticsearchQueryUtils.QUERY_SIZE;

/**
 * Created by rishabh.goyal on 02/11/14.
 */

@AnalyticsProvider(opcode = "count", request = CountRequest.class, response = CountResponse.class, cacheable = false)
public class CountAction extends Action<CountRequest> {

    public CountAction(CountRequest parameter, TableMetadataManager tableMetadataManager, DataStore dataStore, QueryStore queryStore,
                       ElasticsearchConnection connection, String cacheToken, CacheManager cacheManager, ObjectMapper objectMapper,
                       EmailConfig emailConfig, AnalyticsLoader analyticsLoader) {
        super(parameter, tableMetadataManager, dataStore, queryStore, connection, cacheToken, cacheManager, objectMapper, emailConfig);

    }

    @Override
    public void preprocess() {
        getParameter().setTable(ElasticsearchUtils.getValidTableName(getParameter().getTable()));
        // Null field implies complete doc count
        if(getParameter().getField() != null) {
            getParameter().getFilters()
                    .add(new ExistsFilter(getParameter().getField()));
        }
    }

    @Override
    public String getMetricKey() {
        return getParameter().getTable();
    }

    @Override
    public String getRequestCacheKey() {
        long filterHashKey = 0L;
        CountRequest request = getParameter();
        if(null != request.getFilters()) {
            for(Filter filter : request.getFilters()) {
                filterHashKey += 31 * filter.hashCode();
            }
        }

        filterHashKey += 31 * (request.isDistinct() ? "TRUE".hashCode() : "FALSE".hashCode());
        filterHashKey += 31 * (request.getField() != null ? request.getField()
                .hashCode() : "COLUMN".hashCode());
        return String.format("count-%s-%d", request.getTable(), filterHashKey);
    }

    @Override
    public void validateImpl(CountRequest parameter) throws MalformedQueryException {
        List<String> validationErrors = new ArrayList<>();
        if(CollectionUtils.isNullOrEmpty(parameter.getTable())) {
            validationErrors.add("table name cannot be null or empty");
        }
        if(parameter.isDistinct() && CollectionUtils.isNullOrEmpty(parameter.getField())) {
            validationErrors.add("field name cannot be null or empty");
        }
        if(!CollectionUtils.isNullOrEmpty(validationErrors)) {
            throw FoxtrotExceptions.createMalformedQueryException(parameter, validationErrors);
        }
    }

    @Override
    public ActionResponse execute(CountRequest parameter) throws FoxtrotException {
        SearchRequestBuilder query = getRequestBuilder(parameter);

        try {
            SearchResponse response = query.execute()
                    .actionGet(getGetQueryTimeout());
            return getResponse(response, parameter);
        } catch (ElasticsearchException e) {
            throw FoxtrotExceptions.createQueryExecutionException(parameter, e);

        }
    }

    @Override
    public SearchRequestBuilder getRequestBuilder(CountRequest parameter) throws FoxtrotException {
        if(parameter.isDistinct()) {
            SearchRequestBuilder query;
            try {
                query = getConnection().getClient()
                        .prepareSearch(ElasticsearchUtils.getIndices(parameter.getTable(), parameter))
                        .setIndicesOptions(Utils.indicesOptions())
                        .setSize(QUERY_SIZE)
                        .setQuery(new ElasticSearchQueryGenerator().genFilter(parameter.getFilters()))
                        .addAggregation(Utils.buildCardinalityAggregation(parameter.getField()));
                return query;
            } catch (Exception e) {
                throw FoxtrotExceptions.queryCreationException(parameter, e);
            }
        } else {
            SearchRequestBuilder requestBuilder;
            try {
                requestBuilder = getConnection().getClient()
                        .prepareSearch(ElasticsearchUtils.getIndices(parameter.getTable(), parameter))
                        .setIndicesOptions(Utils.indicesOptions())
                        .setSize(QUERY_SIZE)
                        .setQuery(new ElasticSearchQueryGenerator().genFilter(parameter.getFilters()));
            } catch (Exception e) {
                throw FoxtrotExceptions.queryCreationException(parameter, e);
            }
            return requestBuilder;
        }
    }

    @Override
    public ActionResponse getResponse(org.elasticsearch.action.ActionResponse response, CountRequest parameter) throws FoxtrotException {
        if(parameter.isDistinct()) {
            Aggregations aggregations = ((SearchResponse)response).getAggregations();
            Cardinality cardinality = aggregations.get(Utils.sanitizeFieldForAggregation(parameter.getField()));
            if(cardinality == null) {
                return new CountResponse(0);
            } else {
                return new CountResponse(cardinality.getValue());
            }
        } else {
            return new CountResponse(((SearchResponse)response).getHits()
                                             .getTotalHits());
        }

    }
}
