/*
 * Copyright 2017, Regents of the University of Lancaster
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 * 
 *  * Neither the name of the University of Lancaster nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *
 * Author: Steven Simpson <s.simpson@lancaster.ac.uk>
 */
package uk.ac.lancs.rest;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;

/**
 * Performs basic REST operations to a specified service.
 * 
 * @author simpsons
 */
public class RESTClient {
    /**
     * The root service URI
     */
    protected final URI service;

    /**
     * An authorization string to send with each request
     */
    protected final String authz;

    /**
     * A source of fresh HTTP clients
     */
    protected final Supplier<? extends HttpClient> httpProvider;

    private final JsonReaderFactory readerFactory =
        Json.createReaderFactory(Collections.emptyMap());

    /**
     * Create a REST client for a given service, using the supplied HTTP
     * clients and authorization.
     * 
     * @param service the root URI (ending in a slash) of the REST API
     * 
     * @param httpProvider a source of fresh HTTP clients
     * 
     * @param authz an authorization string to send with each request,
     * or {@code null} if not required
     */
    protected RESTClient(URI service,
                         Supplier<? extends HttpClient> httpProvider,
                         String authz) {
        this.service = service;
        this.authz = authz;
        this.httpProvider = httpProvider;
    }

    private RESTResponse<JsonStructure> request(HttpUriRequest request)
        throws IOException {
        HttpClient client = httpProvider.get();
        if (authz != null) request.setHeader("Authorization", authz);
        HttpResponse rsp = client.execute(request);
        final int code = rsp.getStatusLine().getStatusCode();
        HttpEntity ent = rsp.getEntity();
        JsonReader reader = readerFactory
            .createReader(ent.getContent(), StandardCharsets.UTF_8);
        JsonStructure result = reader.read();
        return new RESTResponse<JsonStructure>(code, result);
    }

    /**
     * Perform a GET request on the service.
     * 
     * @param sub the resource within the service
     * 
     * @return the JSON response and code
     * 
     * @throws IOException if an I/O error occurred
     */
    protected RESTResponse<JsonStructure> get(String sub) throws IOException {
        URI location = service.resolve(sub);
        HttpGet request = new HttpGet(location);
        return request(request);
    }

    /**
     * Perform a DELETE request on the service.
     * 
     * @param sub the resource within the service
     * 
     * @return the JSON response and code
     * 
     * @throws IOException if an I/O error occurred
     */
    protected RESTResponse<JsonStructure> delete(String sub)
        throws IOException {
        URI location = service.resolve(sub);
        HttpDelete request = new HttpDelete(location);
        return request(request);
    }

    /**
     * Perform a POST request on the service.
     * 
     * @param sub the resource within the service
     * 
     * @param params named JSON objects that will form the request body
     * 
     * @return the JSON response and code
     * 
     * @throws IOException if an I/O error occurred
     */
    protected RESTResponse<JsonStructure> post(String sub, Map<?, ?> params)
        throws IOException {
        URI location = service.resolve(sub);
        HttpPost request = new HttpPost(location);
        request.setEntity(entityOf(params));
        return request(request);
    }

    /**
     * Perform a PATCH request on the service.
     * 
     * @param sub the resource within the service
     * 
     * @param params a list of patch commands that will form the request
     * body
     * 
     * @return the JSON response and code
     * 
     * @throws IOException if an I/O error occurred
     */
    protected RESTResponse<JsonStructure> patch(String sub, List<?> params)
        throws IOException {
        URI location = service.resolve(sub);
        HttpPatch request = new HttpPatch(location);
        request.setEntity(entityOf(params));
        return request(request);
    }

    private static HttpEntity entityOf(Map<?, ?> params) throws IOException {
        try (StringWriter out = new StringWriter();
            JsonGenerator gen = Json.createGenerator(out)) {
            for (Map.Entry<?, ?> entry : params.entrySet()) {
                String key = entry.getKey().toString();
                Object obj = entry.getValue();
                if (obj instanceof JsonValue)
                    gen.write(key, (JsonValue) obj);
                else if (obj instanceof BigDecimal)
                    gen.write(key, (BigDecimal) obj);
                else if (obj instanceof Boolean)
                    gen.write(key, (Boolean) obj);
                else if (obj instanceof BigInteger)
                    gen.write(key, (BigInteger) obj);
                else if (obj instanceof Long || obj instanceof Integer)
                    gen.write(key, ((Number) obj).longValue());
                else if (obj instanceof Float || obj instanceof Double)
                    gen.write(key, ((Number) obj).doubleValue());
                else
                    gen.write(key, obj.toString());
            }
            gen.flush();
            String text = out.toString();
            return EntityBuilder.create()
                .setContentType(ContentType.APPLICATION_JSON).setText(text)
                .build();
        }
    }

    private static HttpEntity entityOf(List<?> params) throws IOException {
        try (StringWriter out = new StringWriter();
            JsonGenerator gen = Json.createGenerator(out)) {
            for (Object obj : params) {
                if (obj instanceof JsonValue)
                    gen.write((JsonValue) obj);
                else if (obj instanceof BigDecimal)
                    gen.write((BigDecimal) obj);
                else if (obj instanceof Boolean)
                    gen.write((Boolean) obj);
                else if (obj instanceof BigInteger)
                    gen.write((BigInteger) obj);
                else if (obj instanceof Long || obj instanceof Integer)
                    gen.write(((Number) obj).longValue());
                else if (obj instanceof Float || obj instanceof Double)
                    gen.write(((Number) obj).doubleValue());
                else
                    gen.write(obj.toString());
            }
            gen.flush();
            String text = out.toString();
            return EntityBuilder.create()
                .setContentType(ContentType.APPLICATION_JSON).setText(text)
                .build();
        }
    }
}
