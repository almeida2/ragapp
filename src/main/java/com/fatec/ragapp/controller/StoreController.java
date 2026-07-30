package com.fatec.ragapp.controller;

import static io.qdrant.client.PointIdFactory.id;

import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;

import io.qdrant.client.QdrantClient;

import io.qdrant.client.grpc.Points.PointStruct;

import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.SearchPoints;
import io.qdrant.client.grpc.Points.UpdateResult;

import io.qdrant.client.grpc.Collections.Distance;

import io.qdrant.client.grpc.Collections.VectorParams;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fatec.ragapp.model.CollectionRequest;

@RestController
@RequestMapping("/api/rag")
public class StoreController {

    @Autowired
    private QdrantClient qdrantClient;

    // --- GERAÇÃO DE VETORES (ENTREGA 1) ---

    /**
     * Simulador de ingestão de documentos.
     * Cria uma nova coleção no Qdrant com base nos dados fornecidos na requisição e
     * adiciona um índice de payload.
     * A coleção é configurada com vetores de 4 dimensões e utiliza a métrica de
     * similaridade de Cosseno.
     * Após a criação, um índice do tipo Keyword é adicionado ao campo "city" do
     * payload.
     * 
     * 
     * @param request O objeto contendo o nome da coleção a ser criada.
     * @return Uma mensagem de confirmação indicando que a coleção e o índice foram
     *         criados com sucesso.
     */
    @PostMapping("/generate")
    public String generate(@RequestBody CollectionRequest request) {
        String collectionName = request.collectionName();

        // 2. Create a collection (1536-dimensional vectors using Cosine similarity)
        try {
            qdrantClient.createCollectionAsync(collectionName,
                    VectorParams.newBuilder().setDistance(Distance.Cosine).setSize(1536).build())
                    .get();

            // 3. Create a payload index for the "city" field

            return "Coleção '" + collectionName + "' criada e payload index para 'city' adicionado com sucesso!";

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Erro: Operação interrompida!";
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof io.grpc.StatusRuntimeException) {
                return "Erro: " + cause.getMessage();
            }
            return "Erro ao criar coleção: " + e.getMessage();
        } catch (Exception e) {
            return "Erro inesperado: " + e.getMessage();
        }
    }

    @PostMapping("/insert")
    public String insert() {
        // 3. Upsert points (Vector + Metadata/Payload)
        String collectionName = "colecao3";
        PointStruct point = PointStruct.newBuilder()
                .setId(id(1))
                .setVectors(vectors(0.05f, 0.61f, 0.76f, 0.74f))
                .putAllPayload(java.util.Map.of("city", value("Berlin")))
                .build();

        UpdateResult result;
        try {
            result = qdrantClient.upsertAsync(collectionName, List.of(point)).get();
            return "Upsert status: " + result.getStatus();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Erro: Operação interrompida!";
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            return "Erro no Upsert: " + (cause != null ? cause.getMessage() : e.getMessage());
        } catch (Exception e) {
            return "Erro inesperado: " + e.getMessage();
        }
    }
    // --- BUSCA SEMÂNTICA (ENTREGA 2) ---

    @PostMapping("/search")
    public List<Map<String, Object>> search() {
        String collectionName = "colecao3";
        // 4. Search for similar vectors
        List<ScoredPoint> searchResults;
        try {
            searchResults = qdrantClient.searchAsync(
                    SearchPoints.newBuilder()
                            .setCollectionName(collectionName)
                            .addAllVector(List.of(0.19f, 0.81f, 0.75f, 0.11f))
                            .setLimit(3)
                            .setWithPayload(io.qdrant.client.WithPayloadSelectorFactory.enable(true))
                            .build())
                    .get();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        List<Map<String, Object>> response = new ArrayList<>();
        for (ScoredPoint hit : searchResults) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("id", hit.getId().hasUuid() ? hit.getId().getUuid() : hit.getId().getNum());
            result.put("score", hit.getScore());
            result.put("payload", hit.getPayloadMap().toString()); // Convertendo para String para evitar erro do
                                                                   // Jackson com Protobuf Value
            response.add(result);
        }

        return response;
    }

    // --- LISTAR COLEÇÕES (ENTREGA 3) ---

    @GetMapping("/collections")
    public List<String> getCollections() throws Exception {
        try {
            return qdrantClient.listCollectionsAsync().get();
        } catch (Exception e) {
            return List.of("Erro ao conectar com o Qdrant: " + e.getMessage());
        }
    }
}
