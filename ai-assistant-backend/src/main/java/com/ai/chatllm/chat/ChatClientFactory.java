package com.ai.chatllm.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import static com.ai.chatllm.util.Constants.*;

@Component
public class ChatClientFactory {

    private final Map<ModelProvider,ChatClient> chatClientMap = new EnumMap<>(ModelProvider.class);

    public ChatClientFactory(OpenAiChatModel openAiChatModel, GoogleGenAiChatModel googleGenAiChatModel
            , OllamaChatModel ollamaChatModel, ChatMemoryRepository chatMemoryRepository){
        Function<ChatModel,ChatClient> function = model -> ChatClient
                .builder(model)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor
                        .builder(MessageWindowChatMemory.builder()
                                .chatMemoryRepository(chatMemoryRepository)
                                .maxMessages(20)
                                .build())
                        .build())
                .build();
        chatClientMap.put(ModelProvider.GEMINI,function.apply(googleGenAiChatModel));
        chatClientMap.put(ModelProvider.OPENAI,function.apply(openAiChatModel));
        chatClientMap.put(ModelProvider.OLLAMA,function.apply(ollamaChatModel));
    }

    public ChatClient.ChatClientRequestSpec getChatClientForReq(ModelProvider provider, String model){
        ChatClient chatClient = this.chatClientMap.get(provider);
        return switch(provider){
            case ModelProvider.GEMINI -> chatClient.prompt().options(GoogleGenAiChatOptions.builder().model(model));
            case ModelProvider.OLLAMA -> chatClient.prompt().options(OllamaChatOptions.builder().model(model));
            case ModelProvider.OPENAI -> chatClient.prompt().options(OpenAiChatOptions.builder().model(model));
        };
    }
}