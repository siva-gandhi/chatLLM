package com.ai.chatllm.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;
import java.util.function.Function;
import static com.ai.chatllm.util.Constants.*;

@Component
public class ChatClientFactory {

    private final ChatClient openAiClient;
    private final ChatClient geminiClient;
    private final ChatClient ollamaClient;

    public ChatClientFactory(OpenAiChatModel openAiChatModel, GoogleGenAiChatModel googleGenAiChatModel, OllamaChatModel ollamaChatModel){
        Function<ChatModel, ChatClient> getChatClient = model -> ChatClient.builder(model)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor
                        .builder(MessageWindowChatMemory.builder().build())
                        .build())
                .build();
        this.openAiClient = getChatClient.apply(openAiChatModel);
        this.geminiClient = getChatClient.apply(googleGenAiChatModel);
        this.ollamaClient = getChatClient.apply(ollamaChatModel);

    }

    public ChatClient getChatClient(ModelProvider provider){
        if(ModelProvider.GEMINI.equals(provider))
            return this.geminiClient;
        if(ModelProvider.OLLAMA.equals(provider))
            return this.ollamaClient;
        return this.openAiClient;
    }
}
