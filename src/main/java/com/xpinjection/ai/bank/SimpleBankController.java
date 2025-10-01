package com.xpinjection.ai.bank;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/bank", produces = MediaType.APPLICATION_JSON_VALUE)
public class SimpleBankController {

    private final BankService bankService;

    public SimpleBankController(BankService bankService) {
        this.bankService = bankService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ChatResponse chat(@RequestBody ChatRequest request) {
        var reply = bankService.reply(request.message());
        return new ChatResponse(reply);
    }

    public record ChatRequest(String message) {}
    public record ChatResponse(String reply) {}
}
