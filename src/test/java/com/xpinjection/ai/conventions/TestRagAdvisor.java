package com.xpinjection.ai.conventions;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TestRagAdvisor implements CallAdvisor {
    private final List<Consumer<List<Document>>> consumers = new ArrayList<>();

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        var response = callAdvisorChain.nextCall(chatClientRequest);
        List<Document> documents = response.chatResponse().getMetadata().get(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT);
        if (documents != null) {
            notifyConsumers(documents);
        }
        return response;
    }

    public void addConsumer(Consumer<List<Document>> consumer) {
        consumers.add(consumer);
    }

    public void clearConsumers() {
        consumers.clear();
    }

    public void notifyConsumers(List<Document> documents) {
        consumers.forEach(consumer -> consumer.accept(documents));
    }
}
