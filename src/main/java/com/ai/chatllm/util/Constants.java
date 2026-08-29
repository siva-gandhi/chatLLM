package com.ai.chatllm.util;

public class Constants {
    private Constants(){
        throw new UnsupportedOperationException("Utility Class");
    }
    public enum ModelProvider{ OPENAI, GEMINI, OLLAMA }
    public static final String SYSTEM_PROMPT = "Act as a friendly conversational assistant";
    public static final String CHAT_MEMORY_RETRIEVE_SIZE = "chat_memory_retrieve_size";
}