package com.xpinjection.ai.chat;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/chat", produces = MediaType.APPLICATION_JSON_VALUE)
public class SimpleChatController {

    private final ChatService chatService;

    public SimpleChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ChatResponse chat(@RequestBody ChatRequest request) {
        var reply = chatService.reply(request.message());
        return new ChatResponse(reply);
    }

    @PostMapping(path = "/memory", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ChatResponse chatWithMemory(@RequestBody MemoryChatRequest request) {
        var reply = chatService.replyWithMemory(request.sessionId(), request.message());
        return new ChatResponse(reply);
    }

    public record ChatRequest(String message) {}
    public record MemoryChatRequest(String sessionId, String message) {}
    public record ChatResponse(String reply) {}
}
