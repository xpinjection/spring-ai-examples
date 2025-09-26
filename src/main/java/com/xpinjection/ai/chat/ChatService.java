package com.xpinjection.ai.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {
    private static final String SYSTEM_PROMPT = "Explain the requested term in simple words " +
            "as if you are a school teacher speaking to 10-year-old kids. " +
            "Keep your explanation to one paragraph.";

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public ChatService(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        SafeGuardAdvisor.builder()
                                .sensitiveWords(List.of("Sex", "sex", "Porn", "porn", "War", "war"))
                                .failureResponse("Don't abuse the system!")
                                .build()
                )
                .build();
        this.chatMemory = chatMemory;
    }

    public String reply(String userMessage) {
        return chatClient
                .prompt()
                .system(SYSTEM_PROMPT)
                .user(userMessage)
                .call()
                .content();
    }

    public String replyWithMemory(String sessionId, String userMessage) {
        var memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory)
                .conversationId(sessionId)
                .build();
        return chatClient
                .prompt()
                .advisors(memoryAdvisor)
                .system(SYSTEM_PROMPT)
                .user(userMessage)
                .call()
                .content();
    }
}
