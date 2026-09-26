package com.ai.chatllm.controller;

import com.ai.chatllm.model.ChatRequest;
import com.ai.chatllm.service.RagDocService;
import com.ai.chatllm.service.ChatService;
import com.ai.chatllm.util.Constants;
import com.ai.chatllm.util.InvalidModelProviderException;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import org.apache.commons.lang3.EnumUtils;

@RestController
@RequestMapping("/api/v1")
public class MainController {
    private static final Logger log = LoggerFactory.getLogger(MainController.class);
    private final ChatService chatService;
    private final RagDocService ragDocService;

    public MainController(ChatService chatService, RagDocService ragDocService){
        this.chatService = chatService;
        this.ragDocService = ragDocService;
    }

    @PostMapping("/chat")
    public String chatCompletion(@RequestBody ChatRequest chatRequest
            , @RequestHeader(value = "X-Session-ID", defaultValue = "default-session") String sessionId) throws InvalidModelProviderException {
        if(!EnumUtils.isValidEnumIgnoreCase(Constants.ModelProvider.class,chatRequest.modelProvider()))
            throw new InvalidModelProviderException(chatRequest.modelProvider());
        log.info("Message received for chat: {} for Provider : {} & Model : {} ",chatRequest.message(), chatRequest.modelProvider(), chatRequest.model());
        return chatService.chat(chatRequest.message(), EnumUtils.getEnumIgnoreCase(Constants.ModelProvider.class,chatRequest.modelProvider()),chatRequest.model(),sessionId);
    }

    @PostMapping(value = "/chatStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest chatRequest
            , @RequestHeader(value = "X-Session-ID", defaultValue = "default-session") String sessionId) throws InvalidModelProviderException {
        if(!EnumUtils.isValidEnumIgnoreCase(Constants.ModelProvider.class,chatRequest.modelProvider()))
            throw new InvalidModelProviderException(chatRequest.modelProvider());
        log.info("Message received : {} for Provider : {} & Model : {} ",chatRequest.message(), chatRequest.modelProvider(), chatRequest.model());
        return chatService.chatStream(chatRequest.message(), EnumUtils.getEnumIgnoreCase(Constants.ModelProvider.class,chatRequest.modelProvider()),chatRequest.model(),sessionId);
    }

    @PostMapping("/uploadFile")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file
            , @RequestHeader(value = "X-Session-ID", defaultValue = "default-session") String sessionId
            , @RequestHeader(value = "fileId") String fileId) {
        if (file.isEmpty())
            return ResponseEntity.badRequest().body("Please upload a valid file.");
        try {
            ragDocService.ingestFile(file,fileId,sessionId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to process file: " + e.getMessage());
        }
        return ResponseEntity.ok("File '" + file.getOriginalFilename() + "' ingested successfully!");
    }

    @DeleteMapping("/deleteFile")
    public ResponseEntity<String> deleteDocument(@RequestHeader(value = "X-Session-ID", defaultValue = "default-session") String sessionId
            , @RequestHeader(value = "fileId") String fileId){
        ragDocService.removeFile(sessionId,fileId);
        return ResponseEntity.ok("File Deleted from current session");
    }

    @ExceptionHandler(InvalidModelProviderException.class)
    public ResponseEntity<String> handleInvalidModel(@NonNull InvalidModelProviderException e){
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}