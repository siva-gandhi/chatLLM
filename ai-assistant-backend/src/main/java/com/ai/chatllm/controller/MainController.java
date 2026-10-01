package com.ai.chatllm.controller;

import com.ai.chatllm.model.ChatRequest;
import com.ai.chatllm.service.RagDocService;
import com.ai.chatllm.service.ChatService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1")
public class MainController {
    private final ChatService chatService;
    private final RagDocService ragDocService;

    public MainController(ChatService chatService, RagDocService ragDocService){
        this.chatService = chatService;
        this.ragDocService = ragDocService;
    }

    @PostMapping("/chat")
    public String chatCompletion(@RequestBody ChatRequest chatRequest, @RequestHeader(value = "X-Session-ID") @NotNull String sessionId) {
        return chatService.chat(chatRequest.message(), chatRequest.modelProvider(),chatRequest.model(),sessionId);
    }

    @PostMapping(value = "/chatStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest chatRequest, @RequestHeader(value = "X-Session-ID") @NotNull String sessionId) {
        return chatService.chatStream(chatRequest.message(), chatRequest.modelProvider(),chatRequest.model(),sessionId);
    }

    @PostMapping("/uploadFile")
    public void uploadDocument(@RequestParam("file") @NotEmpty MultipartFile file
            , @RequestHeader(value = "X-Session-ID") @NotNull String sessionId, @RequestHeader(value = "fileId") @NotNull String fileId) {
        ragDocService.ingestFile(file,fileId,sessionId);
    }

    @DeleteMapping("/deleteFile")
    public void deleteDocument(@RequestHeader(value = "X-Session-ID") @NotNull String sessionId
            , @RequestHeader(value = "fileId") @NotNull String fileId){
        ragDocService.removeFile(sessionId,fileId);
    }
}