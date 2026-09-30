package com.ai.chatllm.model;

import jakarta.annotation.Nullable;

public record ChatRequest (String message, String modelProvider, @Nullable String model){}

