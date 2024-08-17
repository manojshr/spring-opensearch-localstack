package io.codlibs.opensearch.controller;

import io.codlibs.opensearch.model.IndexRequest;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/indices")
public class IndexController {

    private final OpenSearchClient openSearchClient;

    public IndexController(OpenSearchClient openSearchClient) {
        this.openSearchClient = openSearchClient;
    }

    @PostMapping
    public ResponseEntity<String> createIndex(@RequestBody IndexRequest indexRequest) throws IOException, ExecutionException, InterruptedException {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder()
                .index(indexRequest.index()).build();
        openSearchClient.indices().create(createIndexRequest);
        return ResponseEntity.status(201)
                .body("""
                  {
                    "status": "created"
                  }""");
    }
}
