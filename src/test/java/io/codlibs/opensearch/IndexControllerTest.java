package io.codlibs.opensearch;

import io.codlibs.opensearch.testbase.OpenSearchTestBase;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch.indices.GetIndexRequest;
import org.opensearch.client.opensearch.indices.GetIndexResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;

@AutoConfigureWebTestClient
class IndexControllerTest extends OpenSearchTestBase {

    @Autowired
    protected WebTestClient webTestClient;

    @Test
    void shouldCreateIndex() throws IOException {
        String indexRequest = """
                {
                   "index": "users"
                }""";
        webTestClient.post()
                .uri("/indices")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(indexRequest)
                .exchange()
                .expectStatus()
                .isCreated();

        GetIndexRequest getIndexRequest = new GetIndexRequest.Builder().index("users").build();
        GetIndexResponse getIndexResponse = openSearchClient().indices().get(getIndexRequest);
        Assertions.assertThat(getIndexResponse.result().get("users")).isNotNull();
    }
}