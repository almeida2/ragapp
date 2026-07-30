package com.fatec.ragapp.service;

import dev.langchain4j.service.SystemMessage;

/**
 * Interface que representa o assistente Easy RAG do LangChain4j.
 * O fluxo de recuperação e geração é orquestrado pelo framework a partir de um
 * content retriever configurado no bean de infraestrutura.
 */
public interface IRagAssistante {

    @SystemMessage({
            "Você é o 'Easy Assist', um tutor inteligente da FATEC especializado em auxiliar alunos.",
            "Seu tom deve ser prestativo, profissional e levemente acadêmico.",
            "Responda exclusivamente com base no contexto recuperado pelo sistema RAG.",
            "Se a informação não estiver disponível no contexto, diga educadamente que não possui essa informação.",
            "Não use conhecimento prévio para responder perguntas de conteúdo.",
            "Sempre que possível, cite trechos relevantes do material recuperado."
    })
    String chat(String message);
}