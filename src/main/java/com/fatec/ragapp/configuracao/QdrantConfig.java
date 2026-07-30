package com.fatec.ragapp.configuracao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;

@Configuration
public class QdrantConfig {

    @Value("${qdrant.url}")
    private String url;

    @Value("${qdrant.api-key}")
    private String apiKey;

    @Bean
    public QdrantClient qdrantClient() {
        String host = url.replace("https://", "").replace("http://", "");
        return new QdrantClient(
                QdrantGrpcClient.newBuilder(host, 6334, true).withApiKey(apiKey).build());
    }
}