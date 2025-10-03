package com.xpinjection.ai.conventions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(IntegrationTestConfig.class)
@ActiveProfiles(profiles = {"local", "test"})
class ConventionsServiceRelevancyIntegrationTest {

    @Autowired
    private ConventionsService conventionsService;

    @Autowired
    private ConventionsIndexingService indexingService;

    @Autowired
    @Qualifier("bedrockChatClientBuilder")
    private ChatClient.Builder bedrockChatClientBuilder;

    @Autowired
    private TestRagAdvisor testRagAdvisor;

    @AfterEach
    void cleanUp() {
        testRagAdvisor.clearConsumers();
    }

    @Test
    void ragFindsRelevantDocumentsAndUsesThemToGenerateAnswer() {
        indexingService.index(ConventionsMode.MARKDOWN);

        var question = "How shall I name my API URL?";
        List<Document> usedDocuments = new ArrayList<>();
        testRagAdvisor.addConsumer(usedDocuments::addAll);
        var answer = conventionsService.answerAdvanced(question, ConventionsMode.MARKDOWN);
        assertThat(usedDocuments)
                .withFailMessage("Expected 3 documents found by RAG, but got %s", usedDocuments)
                .hasSize(3);

        var evaluator = new RelevancyEvaluator(bedrockChatClientBuilder);
        var result = evaluator.evaluate(new EvaluationRequest(question, usedDocuments, answer));
        assertThat(result.isPass())
                .withFailMessage("Relevancy failed. Reason: %s. Answer: %s", result.getFeedback(), answer)
                .isTrue();
    }
}
