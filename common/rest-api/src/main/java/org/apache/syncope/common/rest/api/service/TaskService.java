/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.common.rest.api.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.ResponseHeader;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.syncope.common.lib.to.TaskTO;
import org.apache.syncope.common.lib.to.BulkAction;
import org.apache.syncope.common.lib.to.BulkActionResult;
import org.apache.syncope.common.lib.to.PagedResult;
import org.apache.syncope.common.lib.to.SchedTaskTO;
import org.apache.syncope.common.lib.types.TaskType;
import org.apache.syncope.common.rest.api.RESTHeaders;
import org.apache.syncope.common.rest.api.beans.TaskQuery;

/**
 * REST operations for tasks.
 */
@Api(tags = "Tasks", authorizations = {
    @Authorization(value = "BasicAuthentication")
    , @Authorization(value = "Bearer") })
@Path("tasks")
public interface TaskService extends ExecutableService {

    /**
     * Returns the task matching the given key.
     *
     * @param type task type
     * @param key key of task to be read
     * @param details whether include executions or not, defaults to true
     * @param <T> type of taskTO
     * @return task with matching id
     */
    @GET
    @Path("{type}/{key}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    <T extends TaskTO> T read(
            @NotNull @PathParam("type") TaskType type,
            @NotNull @PathParam("key") String key,
            @QueryParam(JAXRSService.PARAM_DETAILS) @DefaultValue("true") boolean details);

    /**
     * Returns a paged list of existing tasks matching the given query.
     *
     * @param query query conditions
     * @param <T> type of taskTO
     * @return paged list of existing tasks matching the given query
     */
    @GET
    @Path("{type}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    <T extends TaskTO> PagedResult<T> list(@BeanParam TaskQuery query);

    /**
     * Creates a new task.
     *
     * @param type task type
     * @param taskTO task to be created
     * @return Response object featuring Location header of created task
     */
    @ApiResponses(
            @ApiResponse(code = 201,
                    message = "Task successfully created", responseHeaders = {
                @ResponseHeader(name = RESTHeaders.RESOURCE_KEY, response = String.class,
                        description = "UUID generated for the entity created")
                , @ResponseHeader(name = HttpHeaders.LOCATION, response = String.class,
                        description = "URL of the entity created") }))
    @POST
    @Path("{type}")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    Response create(@NotNull @PathParam("type") TaskType type, @NotNull SchedTaskTO taskTO);

    /**
     * Updates the task matching the provided key.
     *
     * @param type task type
     * @param taskTO updated task to be stored
     * @return an empty response if operation was successful
     */
    @PUT
    @Path("{type}/{key}")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    Response update(@NotNull @PathParam("type") TaskType type, @NotNull SchedTaskTO taskTO);

    /**
     * Deletes the task matching the provided key.
     *
     * @param type task type
     * @param key key of task to be deleted
     * @return an empty response if operation was successful
     */
    @DELETE
    @Path("{type}/{key}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    Response delete(@NotNull @PathParam("type") TaskType type, @NotNull @PathParam("key") String key);

    /**
     * Executes the provided bulk action.
     *
     * @param bulkAction list of task ids against which the bulk action will be performed.
     * @return Bulk action result
     */
    @POST
    @Path("bulk")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    BulkActionResult bulk(@NotNull BulkAction bulkAction);
}
