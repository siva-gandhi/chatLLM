package com.ai.chatllm.service;

import com.ai.chatllm.chat.ChatClientFactory;
import com.ai.chatllm.util.Constants.ModelProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.vectorstore.VectorStore;
import reactor.core.publisher.Flux;
import java.util.function.Consumer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    private ChatClientFactory factory;
    private ChatClient.ChatClientRequestSpec request;
    private ChatClient.AdvisorSpec advisorSpec;
    private ChatService service;
    private ChatMemoryRepository chatMemoryRepository;

    @BeforeEach
    void setUp() {
        factory = mock(ChatClientFactory.class);
        request = mock(ChatClient.ChatClientRequestSpec.class);
        advisorSpec = mock(ChatClient.AdvisorSpec.class);
        chatMemoryRepository = mock(ChatMemoryRepository.class);
        when(request.user(anyString())).thenReturn(request);
        when(advisorSpec.param(anyString(), any())).thenReturn(advisorSpec);
        doAnswer(invocation -> {
            Consumer<ChatClient.AdvisorSpec> consumer = invocation.getArgument(0);
            consumer.accept(advisorSpec);
            return request;
        }).when(request).advisors(org.mockito.ArgumentMatchers.<Consumer<ChatClient.AdvisorSpec>>any());
        when(request.advisors(any(org.springframework.ai.chat.client.advisor.api.Advisor.class)))
                .thenReturn(request);
        when(factory.getChatClientForReq(any(), anyString())).thenReturn(request);
        service = new ChatService(factory, mock(VectorStore.class), chatMemoryRepository);
    }

    @Test
    void chatSetsMemoryParametersAndReturnsResponse() {
        ChatClient.CallResponseSpec call = mock(ChatClient.CallResponseSpec.class);
        when(request.call()).thenReturn(call);
        when(call.content()).thenReturn("answer");
        assertEquals("answer", service.chat("question", ModelProvider.OPENAI, "model", "session"));
        verify(advisorSpec).param(org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID, "session");
        verify(factory).getChatClientForReq(ModelProvider.OPENAI, "model");
    }

    @Test
    void chatStreamReturnsModelStream() {
        Flux<String> stream = Flux.just("answer", " continued");
        ChatClient.StreamResponseSpec response = mock(ChatClient.StreamResponseSpec.class);
        when(request.stream()).thenReturn(response);
        when(response.content()).thenReturn(stream);
        assertEquals(stream, service.chatStream("question", ModelProvider.OLLAMA, "model", "session"));
        verify(advisorSpec).param(org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID, "session");
        verify(factory).getChatClientForReq(ModelProvider.OLLAMA, "model");
    }

    @Test
    void testDeleteSession(){
        service.deleteSession("sessionId");
        verify(chatMemoryRepository).deleteByConversationId("sessionId");
    }
}
