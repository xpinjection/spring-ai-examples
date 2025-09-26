package com.xpinjection.ai.conventions;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConventionsService {
    private static final String SYSTEM_PROMPT = "You are an assistant that answers questions about internal API conventions. " +
            "Use ONLY the information provided in the API conventions to answer. " +
            "If the answer is not contained in the API conventions, reply exactly: 'I don't know based on the existing conventions.' " +
            "Be concise and precise.";

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public ConventionsService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    public String answer(String question) {
        var qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .similarityThreshold(0.6)
                        .topK(3)
                        .build())
                .build();

        return chatClient
                .prompt()
                .system(SYSTEM_PROMPT)
                .user(question)
                .advisors(qaAdvisor)
                .call()
                .content();
    }
}
