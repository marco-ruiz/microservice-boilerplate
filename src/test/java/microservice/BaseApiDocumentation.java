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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.servlet.RequestDispatcher;

import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

/**
 * @author Marco Ruiz
 * @since Jul 13, 2017
 */
@SpringBootTest
@TestPropertySource(properties = { "application.add-test-data = false" })
public class BaseApiDocumentation {

	@Value("${server.contextPath}")
	protected String contextPath;

	@Rule
	public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

	@Autowired
	protected WebApplicationContext context;

	@Autowired
	protected ObjectMapper objectMapper;

	protected RestDocumentationResultHandler doc;
	protected MockMvc mockMvc;

	//===========================
	// SETUP UTILITIES
	//===========================
	public void setup() {
		this.doc = document("{method-name}",
			preprocessRequest(prettyPrint()),
			preprocessResponse(prettyPrint()));

		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
			.apply(documentationConfiguration(this.restDocumentation))
			.alwaysDo(this.doc)
			.build();
	}

	public void wipeRepositories(CrudRepository<?, ?>... repositories) {
		Stream.of(repositories).forEach(repo -> repo.deleteAll());
	}

	//===========================
	// COMMON DOCUMENTS
	//===========================
	public void headersExample() throws Exception {
		ResultHandler docHandler = this.doc.document(
			responseHeaders(
				headerWithName("Content-Type").description("The Content-Type of the payload, e.g. `application/hal+json`")));

		this.mockMvc
			.perform(get("/"))
			.andExpect(status().isOk())
			.andDo(docHandler);
	}

	public void errorExample() throws Exception {
		ResultHandler docHandler = this.doc.document(
				responseFields(
						fieldWithPath("error").description("The HTTP error that occurred, e.g. `Method Not Allowed`"),
						fieldWithPath("message").description("A description of the cause of the error"),
						fieldWithPath("path").description("The path to which the request was made"),
						fieldWithPath("status").description("The HTTP status code, e.g. `405`"),
						fieldWithPath("timestamp").description("The time, in milliseconds, at which the error occurred"))
		);

		this.mockMvc.perform(get("/error")
				.requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 405)
				.requestAttr(RequestDispatcher.ERROR_REQUEST_URI, contextPath)
				.requestAttr(RequestDispatcher.ERROR_MESSAGE, "Request method 'DELETE' not supported")
				.requestAttr(RequestDispatcher.ERROR_EXCEPTION_TYPE, HttpRequestMethodNotSupportedException.class)
				)
			.andDo(print())
			.andExpect(status().isMethodNotAllowed())
			.andExpect(jsonPath("error", is("Method Not Allowed")))
			.andExpect(jsonPath("timestamp", is(notNullValue())))
			.andExpect(jsonPath("status", is(405)))
			.andExpect(jsonPath("path", is(notNullValue())))
			.andDo(docHandler);
	}

	//===========================
	// DOCUMENTATION UTILITIES
	//===========================
	public ResultActions testOkAndDocumentRequest(MockHttpServletRequestBuilder requestBuilder, ResultHandler docHandler) throws Exception {
		return this.mockMvc.perform(requestBuilder)
			.andExpect(status().isOk())
			.andDo(docHandler);
	}

	public ResultActions submitPayload(HttpMethod method, String uri, Map<String, Object> payload) throws Exception, JsonProcessingException {
		return this.mockMvc
				.perform(
						request(method, getRelativePath(uri))
							.contentType(MediaType.APPLICATION_JSON_VALUE)
							.content(this.objectMapper.writeValueAsString(payload))
						)
				.andExpect(status().isOk());
	}

	//===========================
	// ASSERTION UTILITIES
	//===========================
	public ResultActions assertExpectedResponse(String uri, Map<String, Object> expectedResponse) throws Exception {
		String relPath = getRelativePath(uri);
		ResultActions resultActions = this.mockMvc.perform(get(relPath))
				.andExpect(status().isOk())
				.andExpect(jsonPath("_links.self.href", is(uri)));

		return assertExpectedResponse(resultActions, expectedResponse);
	}

	public ResultActions assertExpectedResponse(ResultActions resultActions, Map<String, Object> expectedResponse) throws Exception {
		resultActions.andExpect(status().isOk());

		if (expectedResponse != null) {
			Optional<Exception> exception = expectedResponse.entrySet().stream()
				.map(entry -> assertExpectedResponseField(resultActions, entry))
				.filter(Objects::nonNull)
				.findFirst();

			if (exception.isPresent())
				throw exception.get();
		}

		return resultActions;
	}

	public Exception assertExpectedResponseField(ResultActions responseActions, Map.Entry<String, Object> entry) {
		try {
			responseActions.andExpect(jsonPath(entry.getKey(), is(entry.getValue())));
		} catch (Exception e) {
			return e;
		}
		return null;
	}

	//===========================
	// DATA MASSAGING UTILITIES
	//===========================
	public String getLocation(ResultActions resultActions, boolean relative) throws UnsupportedEncodingException {
		MockHttpServletResponse response = resultActions.andReturn().getResponse();
		String location = JsonPath.parse(response.getContentAsString()).read("_links.self.href");
		location = (location != null && !location.equals("")) ? location : response.getHeader("Location");
		return relative ? getRelativePath(location) : location;
	}

	public String getRelativePath(String uri) {
		return uri.contains(contextPath) ? uri.split(contextPath, 2)[1] : uri;
	}

	public Map<String, Object> createPayloadModel(String[] keys, Object[] values) {
		Map<String, Object> result = new LinkedHashMap<>();
		IntStream.range(0, keys.length)
			.filter(idx -> values[idx] != null)
			.forEach(idx -> result.put(keys[idx], values[idx]));
		return result;
	}


	//===========================
	// REQUEST BUILDER UTILITIES
	//===========================
	public MockHttpServletRequestBuilder get(String relPath) {
    	return request(HttpMethod.GET, relPath);
    }

	public MockHttpServletRequestBuilder post(String relPath) {
    	return request(HttpMethod.POST, relPath);
    }

	public MockHttpServletRequestBuilder patch(String relPath) {
    	return request(HttpMethod.PATCH, relPath);
    }

	public MockHttpServletRequestBuilder put(String relPath) {
    	return request(HttpMethod.PUT, relPath);
    }

	public MockHttpServletRequestBuilder delete(String relPath) {
    	return request(HttpMethod.DELETE, relPath);
    }

    public MockHttpServletRequestBuilder request(HttpMethod method, String relPath) {
    	return RestDocumentationRequestBuilders.request(method, contextPath + relPath).contextPath(contextPath);
    }
}
