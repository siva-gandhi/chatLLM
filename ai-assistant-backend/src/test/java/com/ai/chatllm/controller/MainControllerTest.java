package com.ai.chatllm.controller;

import com.ai.chatllm.chat.ChatRequest;
import com.ai.chatllm.service.ChatService;
import com.ai.chatllm.service.RagDocService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import reactor.core.publisher.Flux;
import com.ai.chatllm.util.Constants.ModelProvider;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MainControllerTest {

    private ChatService chatService;
    private RagDocService ragDocService;
    private MainController controller;

    @BeforeEach
    void setUp() {
        chatService = mock(ChatService.class);
        ragDocService = mock(RagDocService.class);
        controller = new MainController(chatService, ragDocService);
    }

    @Test
    void testChatCompletion() {
        ChatRequest request = new ChatRequest("Hello", ModelProvider.OPENAI, "model");
        when(chatService.chat("Hello", ModelProvider.OPENAI, "model", "session")).thenReturn("Hi");
        assertEquals("Hi", controller.chatCompletion(request, "session"));
        verify(chatService).chat("Hello", ModelProvider.OPENAI, "model", "session");
    }


    @Test
    void testChatStream() {
        ChatRequest request = new ChatRequest("Hello", ModelProvider.GEMINI, "model");
        Flux<String> response = Flux.just("Hello", " there");
        when(chatService.chatStream("Hello", ModelProvider.GEMINI, "model", "session")).thenReturn(response);
        assertSame(response, controller.chatStream(request, "session"));
        verify(chatService).chatStream("Hello", ModelProvider.GEMINI, "model", "session");
    }

    @Test
    void testUploadDocument() {
        MockMultipartFile file = new MockMultipartFile("file", "file.txt", "text/plain", "document content".getBytes());
        controller.uploadDocument(file, "session", "file");
        verify(ragDocService).ingestFile(file, "file", "session");
    }


    @Test
    void testDeleteDocument() {
        controller.deleteDocument("session", "file");
        verify(ragDocService).removeFile("session", "file");
    }

    @Test
    void testDeleteSession() {
        controller.deleteSession("session",null);
        verify(chatService).deleteSession("session");
        controller.deleteSession(null,true);
        verify(chatService).deleteAllSessions();
    }
}
