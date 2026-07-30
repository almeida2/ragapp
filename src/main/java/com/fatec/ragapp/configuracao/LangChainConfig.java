package com.fatec.ragapp.configuracao;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fatec.ragapp.service.IRagAssistante;

@Configuration
public class LangChainConfig {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name}")
    private String chatModelName;

    @Value("${langchain4j.open-ai.embedding-model.model-name}")
    private String embeddingModelName;

    @Value("${qdrant.url}")
    private String qdrantUrl;

    @Value("${qdrant.api-key}")
    private String qdrantApiKey;

    // 1. Configuração do Modelo de Chat
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        logger.info(">>>>>> Configurando o modelo de chat: {}", chatModelName);
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(chatModelName)
                .temperature(0.0) // Essencial para RAG: evita que a IA invente factos
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    // 2. Configuração do Modelo de Embedding
    @Bean
    public EmbeddingModel embeddingModel() {
        logger.info(">>>>>> Configurando o modelo de embedding: {}", embeddingModelName);
        return OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(embeddingModelName)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    // 3. Store em Qdrant collection ragapp- o vetor de armazenamento (collection)
    // deve ser
    // criado manualmente no Qdrant
    // O modelo text-embedding-3-small produz, por padrão, vetores de 1536
    // dimensões. A coleção do Qdrant deve ter exatamente a mesma dimensão dos
    // vetores gerados pelo modelo de embedding.
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        String host = qdrantUrl.replace("https://", "").replace("http://", "");
        return QdrantEmbeddingStore.builder()
                .host(host)
                .port(6334)
                .useTls(true)
                .apiKey(qdrantApiKey)
                .collectionName("ragapp")
                .build();
    }

    // 4. Ingestor (Leva os documentos para a Store)
    // armazena os documentos em um repositorio de embeddings para que possam ser
    // recuperados posteriormente.

    @Bean
    public EmbeddingStoreIngestor embeddingStoreIngestor(EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore) {
        logger.info(">>>>>> Configurando o ingestor de embeddings...");
        return EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(500, 50)) // Divide em pedaços de 500 caracteres, com
                                                                        // sobreposição de 50
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
    }

    // 5. Retriever (Busca os documentos relevantes)
    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel) {
        logger.info(">>>>> Configurando o retriever de conteúdo para Easy RAG...");
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.5)
                .build();
    }

    // 6. O Assistente configurado (Conecta Chat + RAG + Memória)
    @Bean
    public IRagAssistante ragAssistant(ChatLanguageModel chatLanguageModel, ContentRetriever contentRetriever) {
        logger.info(">>>>>> Configurando o RagAssistant com memória e retriever...");
        return AiServices.builder(IRagAssistante.class)
                .chatLanguageModel(chatLanguageModel)
                .contentRetriever(contentRetriever)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }

}
