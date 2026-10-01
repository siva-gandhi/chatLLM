package com.ai.chatllm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.mock.web.MockMultipartFile;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class RagDocServiceTest {

    private VectorStore vectorStore;
    private RagDocService service;

    @BeforeEach
    void setUp() {
        vectorStore = mock(VectorStore.class);
        service = new RagDocService(vectorStore);
    }

    @Test
    void testIngestFile() {
        String content = "This is a document used to verify text extraction and vector storage. ".repeat(20);
        MockMultipartFile file = new MockMultipartFile("file", "notes.txt", "text/plain", content.getBytes());
        service.ingestFile(file, "file", "session");
        verify(vectorStore).accept(anyList());
    }

    @Test
    void testRemoveFile() {
        service.removeFile("session", "file");
        verify(vectorStore).delete(any(org.springframework.ai.vectorstore.filter.Filter.Expression.class));
    }
}
