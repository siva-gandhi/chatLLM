package com.ai.chatllm.service;

import com.ai.chatllm.chat.ChatClientFactory;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.function.Consumer;
import static com.ai.chatllm.util.Constants.*;
import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Service
public class ChatService {

    private final ChatClientFactory chatClientFactory;

    public ChatService(ChatClientFactory chatClientFactory){
        this.chatClientFactory = chatClientFactory;
    }

    private Consumer<ChatClient.AdvisorSpec> getMemoryAdvisor(String sessionId) {
        return advisorSpec -> advisorSpec.param(CONVERSATION_ID, sessionId).param(CHAT_MEMORY_RETRIEVE_SIZE, 10);
    }

    public String chat(String message,String modelProvider,String sessionId){
        ChatClient chatClient = chatClientFactory.getChatClient(EnumUtils.getEnumIgnoreCase(ModelProvider.class,modelProvider));
        return chatClient.prompt().user(message).advisors(getMemoryAdvisor(sessionId)).call().content();
    }

    public Flux<String> chatStream(String message, String modelProvider, String sessionId){
        ChatClient chatClient = chatClientFactory.getChatClient(EnumUtils.getEnumIgnoreCase(ModelProvider.class,modelProvider));
        return chatClient.prompt().user(message).advisors(getMemoryAdvisor(sessionId)).stream().content();
    }
}
