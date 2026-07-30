package com.fatec.ragapp.service;

import java.io.InputStream;

public interface IIndexingService {
    void ingestDocument(InputStream dataStream);
}
