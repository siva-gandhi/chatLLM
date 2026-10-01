package com.ai.chatllm.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Objects;
import static com.ai.chatllm.util.Constants.*;
import static org.springframework.ai.vectorstore.filter.Filter.*;

@Service
public class RagDocService {
    private final VectorStore vectorStore;
    private final TokenTextSplitter splitter;

    public RagDocService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.splitter = TokenTextSplitter.builder()
                .withChunkSize(2000)
                .withMinChunkSizeChars(150)
                .withMinChunkLengthToEmbed(100)
                .withMaxNumChunks(200)
                .withKeepSeparator(true)
                .build();
    }

    public void ingestFile(MultipartFile file,String fileId,String sessionId) {
        TikaDocumentReader reader = new TikaDocumentReader(file.getResource());
        List<Document> documents = splitter.apply(reader.get());
        documents.forEach(doc -> {
            doc.getMetadata().put(SESSION_ID, sessionId);
            doc.getMetadata().put(FILE_ID, fileId);
            doc.getMetadata().put(FILENAME, Objects.requireNonNull(file.getOriginalFilename()));
        });
        vectorStore.accept(documents);
    }

    public void removeFile(String sessionId, String fileId){
        vectorStore.delete(new Expression(ExpressionType.AND,
                new Expression(ExpressionType.EQ, new Key(SESSION_ID), new Value(sessionId)),
                new Expression(ExpressionType.EQ, new Key(FILE_ID), new Value(fileId))));

    }
}