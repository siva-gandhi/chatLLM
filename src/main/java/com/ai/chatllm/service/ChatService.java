package com.ai.chatllm.service;

import com.ai.chatllm.chat.ChatClientFactory;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.function.Consumer;
import static com.ai.chatllm.util.Constants.*;
import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private final ChatClientFactory chatClientFactory;

    public ChatService(ChatClientFactory chatClientFactory){
        this.chatClientFactory = chatClientFactory;
    }

    private void logInput(String modelProvider, String model){
        log.info("Message received for {} - {}",modelProvider,model);
    }

    private Consumer<ChatClient.AdvisorSpec> getMemoryAdvisor(String sessionId) {
        return advisorSpec -> advisorSpec.param(CONVERSATION_ID, sessionId).param(CHAT_MEMORY_RETRIEVE_SIZE, 10);
    }

    private ChatClient getChatClient(String modelProvider, String model){
        ModelProvider provider = EnumUtils.getEnumIgnoreCase(ModelProvider.class, modelProvider);
        return StringUtils.isEmpty(model) ? chatClientFactory.getChatClient(provider) : chatClientFactory.getChatClient(provider,model);
    }

    public String chat(String message,String modelProvider, String model,String sessionId){
        logInput(modelProvider,model);
        return getChatClient(modelProvider,model).prompt().user(message).advisors(getMemoryAdvisor(sessionId)).call().content();
    }

    public Flux<String> chatStream(String message, String modelProvider, String model, String sessionId){
        logInput(modelProvider,model);
        return getChatClient(modelProvider,model).prompt().user(message).advisors(getMemoryAdvisor(sessionId)).stream().content();
    }
}
