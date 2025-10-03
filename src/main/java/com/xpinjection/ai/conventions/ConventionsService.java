package com.xpinjection.ai.conventions;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

@Service
public class ConventionsService {
    private static final String SYSTEM_PROMPT = "You are an assistant that answers questions about internal API conventions. " +
            "Use ONLY the information provided in the API conventions to answer. " +
            "If the answer is not contained in the API conventions, reply exactly: 'I don't know based on the existing conventions.' " +
            "Be concise and precise.";

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ChatClient.Builder rewriteQueryChatClientBuilder;

    public ConventionsService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.clone()
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
        this.rewriteQueryChatClientBuilder = chatClientBuilder.clone()
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultOptions(ChatOptions.builder()
                        .model("gpt-4o-mini")
                        .temperature(0.1).build());
        this.vectorStore = vectorStore;
    }

    public String answer(String question, ConventionsMode mode) {
        var searchRequest = SearchRequest.builder()
                .filterExpression("mode == '" + mode + "'")
                .similarityThreshold(0.6)
                .topK(3)
                .build();

        var qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(searchRequest)
                .build();

        return chatClient
                .prompt()
                .system(SYSTEM_PROMPT)
                .user(question)
                .advisors(qaAdvisor)
                .call()
                .content();
    }

    public String answerAdvanced(String question, ConventionsMode mode) {
        var ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .queryTransformers(RewriteQueryTransformer.builder()
                        .chatClientBuilder(rewriteQueryChatClientBuilder)
                        .build())
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .filterExpression(new FilterExpressionBuilder().eq("mode", mode.name()).build())
                        .similarityThreshold(0.4)
                        .topK(3)
                        .vectorStore(vectorStore)
                        .build())
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(true)
                        .build())
                .build();

        return chatClient
                .prompt()
                .system(SYSTEM_PROMPT)
                .user(question)
                .advisors(ragAdvisor)
                .call()
                .content();
    }
}
