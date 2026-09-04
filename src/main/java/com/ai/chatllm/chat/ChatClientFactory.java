package com.ai.chatllm.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
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
import java.util.HashMap;
import java.util.Map;
import static com.ai.chatllm.util.Constants.*;

@Component
public class ChatClientFactory {

    private static final Logger log = LoggerFactory.getLogger(ChatClientFactory.class);
    private final Map<ModelProvider,Map<String,ChatClient>> chatClients = new EnumMap<>(ModelProvider.class);
    private final ChatModel openAiChatModel;
    private final ChatModel geminiChatModel;
    private final ChatModel ollamaChatModel;

    private ChatClient.Builder chatClientBuilder(ChatModel model){
        return ChatClient.builder(model).defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor
                        .builder(MessageWindowChatMemory.builder().build())
                        .build());
    }


    public ChatClientFactory(OpenAiChatModel openAiChatModel, GoogleGenAiChatModel googleGenAiChatModel, OllamaChatModel ollamaChatModel){

        this.geminiChatModel = googleGenAiChatModel;
        this.openAiChatModel = openAiChatModel;
        this.ollamaChatModel = ollamaChatModel;

        ChatClient geminiClient = chatClientBuilder(googleGenAiChatModel).build();
        ChatClient openAiClient = chatClientBuilder(openAiChatModel).build();
        ChatClient ollamClient = chatClientBuilder(ollamaChatModel).build();

        Map<String,ChatClient> geminiClientMap = new HashMap<>();
        Map<String,ChatClient> openAiClientMap = new HashMap<>();
        Map<String,ChatClient> ollamaClientMap = new HashMap<>();

        geminiClientMap.put(DEFAULT,geminiClient);
        openAiClientMap.put(DEFAULT,openAiClient);
        ollamaClientMap.put(DEFAULT,ollamClient);

        geminiClientMap.put(googleGenAiChatModel.getOptions().getModel(),geminiClient);
        openAiClientMap.put(openAiChatModel.getOptions().getModel(),openAiClient);
        ollamaClientMap.put(ollamaChatModel.getOptions().getModel(),ollamClient);

        this.chatClients.put(ModelProvider.GEMINI,geminiClientMap);
        this.chatClients.put(ModelProvider.OPENAI,openAiClientMap);
        this.chatClients.put(ModelProvider.OLLAMA,ollamaClientMap);
    }

    private ChatClient addChatClient(ModelProvider provider, String model){
        try {
            ChatClient chatClient = switch (provider) {
                case ModelProvider.GEMINI ->
                        chatClientBuilder(this.geminiChatModel).defaultOptions(GoogleGenAiChatOptions.builder().model(model)).build();
                case ModelProvider.OPENAI ->
                        chatClientBuilder(this.openAiChatModel).defaultOptions(OpenAiChatOptions.builder().model(model)).build();
                case ModelProvider.OLLAMA ->
                        chatClientBuilder(this.ollamaChatModel).defaultOptions(OllamaChatOptions.builder().model(model)).build();
            };
            this.chatClients.get(provider).put(model, chatClient);
            log.info("New Chat Client added for {} - {}", provider.name(),model);
            return chatClient;
        } catch (Exception e){
            log.warn("Unable to create ChatClient for model {} - {}",model,e.getMessage());
            return this.chatClients.get(provider).get(DEFAULT);
        }
    }

    public ChatClient getChatClient(ModelProvider provider){
        return this.chatClients.get(provider).get(DEFAULT);
    }

    public ChatClient getChatClient(ModelProvider provider,String model){
        if(this.chatClients.get(provider).containsKey(model))
            return this.chatClients.get(provider).get(model);
        return addChatClient(provider,model);
    }
}
