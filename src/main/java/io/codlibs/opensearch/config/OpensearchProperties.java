package io.codlibs.opensearch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties("opensearch")
public record OpensearchProperties(String host,
                                   String region) {
}
