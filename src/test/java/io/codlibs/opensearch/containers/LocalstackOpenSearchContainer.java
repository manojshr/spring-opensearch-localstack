package io.codlibs.opensearch.containers;

import org.opensearch.testcontainers.OpensearchContainer;
import org.testcontainers.containers.ExecConfig;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.util.List;

import static io.codlibs.opensearch.containers.OpenSearchTestContainer.SHARED_NETWORK;

public class LocalstackOpenSearchContainer {
    private final static DockerImageName LOCALSTACK_DOCKER_IMAGE = DockerImageName.parse("localstack/localstack:3.6.0");

    private static GenericContainer<?> localstackContainer;

    private final static String OPENSEARCH_CUSTOM_BACKEND = "http://opensearch:9200";

    private final static String USE_SSL = "1";

    public static void start(String opensearchDomainName,
                             String opensearchServiceRegion,
                             String accessKeyId,
                             String secretAccessKey,
                             String certificateFilePath) throws IOException, InterruptedException {
        if (alreadyRunning()) {
            return;
        }
        OpensearchContainer<?> opensearchContainer = OpenSearchTestContainer.start();
        final var execCommand = new String[]{"awslocal" ,"opensearch", "create-domain", "--domain-name", opensearchDomainName};

        localstackContainer = new GenericContainer<>(LOCALSTACK_DOCKER_IMAGE)
                .withEnv("SERVICES", "opensearch")
                .withEnv("USE_SSL", USE_SSL)
                .withEnv("OPENSEARCH_CUSTOM_BACKEND", OPENSEARCH_CUSTOM_BACKEND)
                .withEnv("OPENSEARCH_ENDPOINT_STRATEGY","path")
                .withEnv("AWS_ACCESS_KEY_ID", accessKeyId)
                .withEnv("AWS_SECRET_ACCESS_KEY", secretAccessKey)
                .withEnv("LOCALSTACK_HOST", "localhost")
                .withEnv("AWS_DEFAULT_REGION", opensearchServiceRegion)
                .withCopyFileToContainer(MountableFile.forHostPath(certificateFilePath),
                        "/var/lib/localstack/cache/server.test.pem")
                .dependsOn(opensearchContainer)
                .withNetwork(SHARED_NETWORK)
                .withReuse(true);
        localstackContainer.setPortBindings(List.of("127.0.0.1:4566:4566"));
        localstackContainer.start();
        localstackContainer.execInContainer(ExecConfig.builder().command(execCommand).build());
        Thread.sleep(5000); // Wait for domain creation
    }

    private static boolean alreadyRunning() {
        return localstackContainer != null && localstackContainer.isRunning();
    }
}
