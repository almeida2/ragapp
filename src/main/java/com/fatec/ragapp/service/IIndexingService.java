package com.fatec.ragapp.service;

import java.io.InputStream;

/**
 * Interface para indexação de documentos.
 */
public interface IIndexingService {
    /**
     * Indexa um documento.
     * 
     * @param dataStream Stream de dados do documento.
     */
    void ingestDocument(InputStream dataStream);
}
