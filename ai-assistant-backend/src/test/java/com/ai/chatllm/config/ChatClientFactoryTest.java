package com.ai.chatllm.config;

import com.ai.chatllm.util.Constants.ModelProvider;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatClientFactoryTest {

    @Test
    void buildsRequestsForEveryProvider() {
        OpenAiChatModel openAi = mock(OpenAiChatModel.class);
        GoogleGenAiChatModel gemini = mock(GoogleGenAiChatModel.class);
        OllamaChatModel ollama = mock(OllamaChatModel.class);
        ChatMemoryRepository chatMemoryRepository = mock(ChatMemoryRepository.class);
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        ChatClient client = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec request = mock(ChatClient.ChatClientRequestSpec.class);

        when(builder.defaultSystem(any(String.class))).thenReturn(builder);
        when(builder.defaultAdvisors(any(org.springframework.ai.chat.client.advisor.api.Advisor.class)))
                .thenReturn(builder);
        when(builder.build()).thenReturn(client);
        when(client.prompt()).thenReturn(request);
        when(request.options(any(org.springframework.ai.chat.prompt.ChatOptions.Builder.class)))
                .thenReturn(request);

        try (var chatClient = mockStatic(ChatClient.class)) {
            chatClient.when(() -> ChatClient.builder( openAi)).thenReturn(builder);
            chatClient.when(() -> ChatClient.builder(gemini)).thenReturn(builder);
            chatClient.when(() -> ChatClient.builder( ollama)).thenReturn(builder);

            ChatClientFactory factory = new ChatClientFactory(openAi, gemini, ollama, chatMemoryRepository);

            assertSame(request, factory.getChatClientForReq(ModelProvider.OPENAI, "openai-model"));
            assertSame(request, factory.getChatClientForReq(ModelProvider.GEMINI, "gemini-model"));
            assertSame(request, factory.getChatClientForReq(ModelProvider.OLLAMA, "ollama-model"));
        }

        verify(request, times(3)).options(any(org.springframework.ai.chat.prompt.ChatOptions.Builder.class));
    }
}
