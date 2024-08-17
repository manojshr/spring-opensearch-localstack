package io.codlibs.opensearch;

import io.codlibs.opensearch.testbase.OpenSearchTestBase;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SpringOpenSearchTest extends OpenSearchTestBase {

    @Test
    void contextLoads() throws Exception {
        Assertions.assertThat(openSearchClient().ping().value()).isTrue();
    }
}
