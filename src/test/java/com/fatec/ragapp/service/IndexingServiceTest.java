package com.fatec.ragapp.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class IndexingServiceTest {

    @Autowired
    private IndexingService indexingService;

    @Test
    public void ct01_Quando_DocumentoValido_EntaoDeveIndexarDocumento() {
        // Dado que um documento valido é passado para o metodo ingestDocument
        String documentContent = "Este é um documento de teste para validar a indexação real da aplicação.";
        InputStream inputStream = new ByteArrayInputStream(documentContent.getBytes(StandardCharsets.UTF_8));

        // Quando o metodo ingestDocument é chamado
        // Entao o fluxo completo (parse + ingestão real) ocorre sem lançar exceções.
        assertDoesNotThrow(() -> {
            indexingService.ingestDocument(inputStream);
        });
    }
}
