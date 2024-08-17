package io.codlibs.opensearch.containers;

import org.opensearch.testcontainers.OpensearchContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

import static java.util.Objects.nonNull;

public class OpenSearchTestContainer {

    private final static String OPENSEARCH_IMAGE = "opensearchproject/opensearch:2.11.0";

    private static OpensearchContainer<?> openSearchContainer;

    public final static Network SHARED_NETWORK = Network.builder().driver("bridge").build();

    private final static String MAX_CLAUSE_COUNT = "4096";

    public static OpensearchContainer<?> start() {
        if (nonNull(openSearchContainer) && openSearchContainer.isRunning()) {
            return openSearchContainer;
        }
        openSearchContainer = new OpensearchContainer<>(DockerImageName.parse(OPENSEARCH_IMAGE))
                    .withEnv("node.name", "opensearch")
                    .withEnv("cluster.name", "opensearch-docker-cluster")
                    .withEnv("discovery.type", "single-node")
                    .withEnv("OPENSEARCH_JAVA_OPTS", "-Xms512m -Xmx512m")
                    .withEnv("DISABLE_SECURITY_PLUGIN", "true")
                    .withEnv("indices.query.bool.max_clause_count", MAX_CLAUSE_COUNT)
                    .withNetworkAliases("opensearch")
                    // Allow only on shared network
                    .withNetwork(SHARED_NETWORK)
                    .withCreateContainerCmdModifier(cmd -> cmd.withName("opensearch"))
                    .withReuse(true);
        openSearchContainer.start();
        return openSearchContainer;
    }
}
