package com.xpinjection.ai.conventions;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/conventions", produces = MediaType.APPLICATION_JSON_VALUE)
public class ConventionsController {
    private final ConventionsIndexingService indexingService;
    private final ConventionsService conventionsService;

    public ConventionsController(ConventionsIndexingService indexingService,
                                 ConventionsService conventionsService) {
        this.indexingService = indexingService;
        this.conventionsService = conventionsService;
    }

    @PostMapping(path = "/index")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void index(@RequestBody IndexRequest request) {
        indexingService.index(request.mode());
    }

    @PostMapping(path = "/ask", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AskResponse ask(@RequestBody AskRequest request) {
        var answer = conventionsService.answer(request.question(), request.mode());
        return new AskResponse(answer);
    }

    @PostMapping(path = "/askAdvanced", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AskResponse askAdvanced(@RequestBody AskRequest request) {
        var answer = conventionsService.answerAdvanced(request.question(), request.mode());
        return new AskResponse(answer);
    }

    public record AskRequest(String question, ConventionsMode mode) {}
    public record IndexRequest(ConventionsMode mode) {}
    public record AskResponse(String answer) {}
}
