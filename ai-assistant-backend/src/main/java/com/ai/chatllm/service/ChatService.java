package com.ai.chatllm.service;

import com.ai.chatllm.config.ChatClientFactory;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.function.Function;
import static com.ai.chatllm.util.Constants.*;
import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;
import static org.springframework.ai.vectorstore.filter.Filter.*;

@Service
public class ChatService {
    private final ChatClientFactory chatClientFactory;
    private final Function<String, Advisor> ragAdvisor;

    public ChatService(ChatClientFactory chatClientFactory, VectorStore vectorStore){
        this.chatClientFactory = chatClientFactory;
        this.ragAdvisor = sessionId -> RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .filterExpression(new Expression(ExpressionType.EQ,
                                new Key(SESSION_ID),new Value(sessionId)))
                        .topK(5)
                        .similarityThreshold(0.3)
                        .vectorStore(vectorStore)
                        .build())
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(true)
                        .build())
                .build();
    }

    public String chat(String message, ModelProvider provider, String model,String sessionId){
        return chatClientFactory.getChatClientForReq(provider, model)
                .user(message)
                .advisors(spec -> spec.param(CONVERSATION_ID,sessionId))
                .advisors(this.ragAdvisor.apply(sessionId))
                .call().content();
    }

    public Flux<String> chatStream(String message, ModelProvider provider, String model, String sessionId){
        return chatClientFactory.getChatClientForReq(provider, model)
                .user(message)
                .advisors(spec -> spec.param(CONVERSATION_ID,sessionId))
                .advisors(this.ragAdvisor.apply(sessionId))
                .stream().content();
    }
}