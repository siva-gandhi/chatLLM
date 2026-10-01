package com.ai.chatllm.model;

import com.ai.chatllm.util.Constants.ModelProvider;
import jakarta.validation.constraints.NotNull;

public record ChatRequest (@NotNull String message, @NotNull ModelProvider modelProvider, @NotNull String model){}

