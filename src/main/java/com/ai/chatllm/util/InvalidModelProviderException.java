package com.ai.chatllm.util;

public class InvalidModelProviderException extends Exception{
    public InvalidModelProviderException(String message){

        super("Invalid Model Provider - %s . Available Providers : OPENAI, GEMINI, OLLAMA".formatted(message));
    }
}