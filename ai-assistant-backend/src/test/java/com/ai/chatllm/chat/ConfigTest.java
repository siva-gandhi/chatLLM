package com.ai.chatllm.chat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.openai.OpenAiEmbeddingModel;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class ConfigTest {

    @Test
    void exposesOpenAiEmbeddingModelAsPrimary() {
        OpenAiEmbeddingModel model = mock(OpenAiEmbeddingModel.class);
        assertSame(model, new Config().primaryEmbeddingModel(model));
    }
}
