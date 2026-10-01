package com.ai.chatllm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import static org.mockito.Mockito.mockStatic;

class ChatLlmApplicationTest {

    @Test
    void mainStartsSpringApplication() {
        String[] arguments = {"--spring.main.web-application-type=none"};
        try (var springApplication = mockStatic(SpringApplication.class)) {
            ChatLlmApplication.main(arguments);
            springApplication.verify(() -> SpringApplication.run(ChatLlmApplication.class, arguments));
        }
    }
}
