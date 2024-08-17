package io.codlibs.opensearch.config;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;

@Configuration
@EnableConfigurationProperties(OpensearchProperties.class)
public class OpenSearchConfiguration {

    private final OpensearchProperties opensearchProperties;

    public OpenSearchConfiguration(OpensearchProperties opensearchProperties) {
        this.opensearchProperties = opensearchProperties;
    }

    @Bean
    public OpenSearchClient openSearchClient() {
        return new OpenSearchClient(
                new AwsSdk2Transport(
                        NettyNioAsyncHttpClient.builder().build(),
                        opensearchProperties.host(),
                        software.amazon.awssdk.regions.Region.of(opensearchProperties.region()),
                        AwsSdk2TransportOptions.builder().setCredentials(DefaultCredentialsProvider.create()).build()
                )
        );
    }
}
