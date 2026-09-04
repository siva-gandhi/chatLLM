package com.ai.chatllm.controller;

import com.ai.chatllm.model.ChatRequest;
import com.ai.chatllm.service.ChatService;
import com.ai.chatllm.util.Constants;
import com.ai.chatllm.util.InvalidModelProviderException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import org.apache.commons.lang3.EnumUtils;

@RestController
@RequestMapping("/api/v1")
public class MainController {
    private final ChatService chatService;

    public MainController(ChatService chatService){
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public String chatCompletion(@RequestBody ChatRequest chatRequest
            , @RequestHeader(value = "X-Session-ID", defaultValue = "default-session") String sessionId) throws InvalidModelProviderException {
        if(!EnumUtils.isValidEnumIgnoreCase(Constants.ModelProvider.class,chatRequest.modelProvider()))
            throw new InvalidModelProviderException(chatRequest.modelProvider());

        return chatService.chat(chatRequest.message(), chatRequest.modelProvider(),chatRequest.model(),sessionId);
    }

    @PostMapping(value = "/chatStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest chatRequest
            , @RequestHeader(value = "X-Session-ID", defaultValue = "default-session") String sessionId){
        return chatService.chatStream(chatRequest.message(), chatRequest.modelProvider(),chatRequest.model(),sessionId);
    }

    @ExceptionHandler(InvalidModelProviderException.class)
    public ResponseEntity<String> handleInvalidModel(@NonNull InvalidModelProviderException e){
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}