package com.xpinjection.ai.conventions;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.bedrock.converse.BedrockProxyChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.observation.ChatClientObservationConvention;
import org.springframework.ai.model.chat.client.autoconfigure.ChatClientBuilderConfigurer;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class IntegrationTestConfig {
    @Bean
    @Primary
    ChatClient.Builder openAiChatClientBuilder(ChatClientBuilderConfigurer chatClientBuilderConfigurer,
                                               OpenAiChatModel openAiChatModel,
                                               ObjectProvider<ObservationRegistry> observationRegistry,
                                               ObjectProvider<ChatClientObservationConvention> observationConvention,
                                               TestRagAdvisor testRagAdvisor) {
        var builder = ChatClient.builder(openAiChatModel,
                observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP),
                observationConvention.getIfUnique(() -> null))
                .defaultAdvisors(testRagAdvisor);
        return chatClientBuilderConfigurer.configure(builder);
    }

    @Bean
    ChatClient.Builder bedrockChatClientBuilder(BedrockProxyChatModel bedrockChatModel) {
        return ChatClient.builder(bedrockChatModel);
    }

    @Bean
    TestRagAdvisor testRagAdvisor() {
        return new TestRagAdvisor();
    }
}
