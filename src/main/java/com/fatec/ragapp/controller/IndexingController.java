package com.fatec.ragapp.controller;

import dev.ai4j.openai4j.OpenAiHttpException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fatec.ragapp.service.IIndexingService;

import java.io.IOException;

/**
 * O processo de RAG é dividido em três etapas: INDEXAÇÃO, RECUPERAÇÃO e
 * GERAÇÃO.
 * Na fase de INDEXAÇÃO, documentos são processados e armazenados em um índice
 * para facilitar a recuperação futura.
 * A indexação envolve a extração de informações relevantes dos documentos, como
 * texto, metadados e outros elementos que possam ser úteis para responder a
 * perguntas.
 * A fase de RECUPERAÇÃO envolve a busca de informações relevantes no índice com
 * base em a pergunta do usuário.
 * Finalmente, na fase de GERAÇÃO, uma resposta é formulada com base nas
 * informações
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/indexing")
public class IndexingController {

    private final IIndexingService indexingService;

    public IndexingController(IIndexingService indexingService) {
        this.indexingService = indexingService;
    }

    /**
     * Endpoint para upload de documentos para indexação.
     * 
     * @param file Arquivo para upload.
     * @return ResponseEntity com o resultado do upload.
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Por favor, selecione um arquivo.");
        }

        try {
            indexingService.ingestDocument(file.getInputStream());
            return ResponseEntity.ok("Documento indexado com sucesso: " + file.getOriginalFilename());
        } catch (OpenAiHttpException e) {
            return ResponseEntity.internalServerError()
                    .body("Erro ao processar o arquivo para indexação: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Erro ao processar o arquivo para indexação: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body("Erro ao indexar o documento: " + e.getMessage());
        }
    }
}