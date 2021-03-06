/**
 * Copyright 2014 Flipkart Internet Pvt. Ltd.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flipkart.foxtrot.server.resources;

import com.codahale.metrics.annotation.Timed;
import com.flipkart.foxtrot.common.Document;
import com.flipkart.foxtrot.core.exception.FoxtrotException;
import com.flipkart.foxtrot.core.querystore.QueryStore;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

/**
 * User: Santanu Sinha (santanu.sinha@flipkart.com)
 * Date: 15/03/14
 * Time: 10:55 PM
 */
@Path("/v1/document/{table}")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/v1/document/{table}", description = "Document API")
public class DocumentResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentResource.class);
    private final QueryStore queryStore;

    public DocumentResource(QueryStore queryStore) {
        this.queryStore = queryStore;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @ApiOperation("Save Document")
    public Response saveDocument(@PathParam("table") final String table, @Valid final Document document) throws FoxtrotException {
        queryStore.save(table, document);
        return Response.created(URI.create("/" + document.getId()))
                .build();
    }

    @POST
    @Path("/bulk")
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @ApiOperation("Save list of documents")
    public Response saveDocuments(@PathParam("table") final String table, @Valid final List<Document> documents) throws FoxtrotException {
       try {
        queryStore.save(table, documents);
        } catch (Exception e) {
            LOGGER.error("Error occurred in savingDocuments : " + e);
            throw  e;
        }
        return Response.created(URI.create("/" + table))
                .build();
    }

    @GET
    @Path("/{id}")
    @Timed
    @ApiOperation("Get Document")
    public Response getDocument(@PathParam("table") final String table, @PathParam("id") @NotNull final String id) throws FoxtrotException {
        return Response.ok(queryStore.get(table, id))
                .build();
    }

    @GET
    @Timed
    @ApiOperation("Get Documents")
    public Response getDocuments(@PathParam("table") final String table, @QueryParam("id") @NotNull final List<String> ids)
            throws FoxtrotException {
        return Response.ok(queryStore.getAll(table, ids))
                .build();
    }
}
