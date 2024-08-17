package io.codlibs.opensearch.testbase;

import io.codlibs.opensearch.containers.LocalstackOpenSearchContainer;
import io.codlibs.opensearch.utils.CertificateUtil;
import org.junit.jupiter.api.BeforeAll;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class OpenSearchTestBase {

    @Autowired
    private OpenSearchClient openSearchClient;

    private static final Path OPENSEARCH_SSL_PATH = Paths.get("src","test","resources", "opensearch", "ssl");

    @BeforeAll
    public static void beforeAll(
            @Value("${opensearch-test-domain}") String opensearchTestDomain,
            @Value("${opensearch.region}") String opensearchTestRegion,
            @Value("${aws.accessKeyId}") String accessKeyId,
            @Value("${aws.secretAccessKey}") String secretAccessKey
    ) throws Exception {
        ensureOpenSearchSslPath();
        String trustStoreFilePath = Paths.get(OPENSEARCH_SSL_PATH.toString(), "keystore.jks").toFile().getAbsolutePath();
        String certificateFilePath = Paths.get(OPENSEARCH_SSL_PATH.toString(), "server.test.pem").toFile().getAbsolutePath();

        CertificateUtil.generateCertificateAndTrustStore(certificateFilePath, trustStoreFilePath, "changeit");
        Thread.sleep(1000);
        System.setProperty("javax.net.ssl.trustStore", trustStoreFilePath);
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        System.setProperty("aws.accessKeyId", accessKeyId);
        System.setProperty("aws.secretAccessKey", secretAccessKey);

        LocalstackOpenSearchContainer.start(opensearchTestDomain, opensearchTestRegion, accessKeyId, secretAccessKey, certificateFilePath);
    }

    private static void ensureOpenSearchSslPath() throws IOException {
        FileUtils.deleteQuietly(OPENSEARCH_SSL_PATH.toAbsolutePath().toFile());
        FileUtils.forceMkdir(OPENSEARCH_SSL_PATH.toAbsolutePath().toFile());
    }

    public OpenSearchClient openSearchClient() {
        return openSearchClient;
    }
}
