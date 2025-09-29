package com.xpinjection.ai.cooking;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.content.Media;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.util.Map;

@Service
public class CookingService {
    private static final String SYSTEM_PROMPT = "Look at the photo and recognize the dish name. " +
            "List the products (ingredients) needed to cook it and a short recipe with steps. " +
            "Return ONLY a JSON object that matches the following format: {format}. " +
            "Do not include any explanations, only provide a RFC8259 compliant JSON response following this format without deviation. " +
            "Image file name: {dishImageName}";

    private final ChatClient chatClient;

    public CookingService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.clone()
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    public Dish recognizeDish(String dishImageName) {
        var converter = new BeanOutputConverter<>(Dish.class);

        var systemMessage = SystemPromptTemplate.builder()
                .template(SYSTEM_PROMPT)
                .variables(Map.of(
                        "dishImageName", dishImageName,
                        "format", converter.getFormat()
                ))
                .build()
                .createMessage();

        var image = new ClassPathResource("images/" + dishImageName);
        var mimeType = dishImageName.toLowerCase().endsWith(".png") ? MimeTypeUtils.IMAGE_PNG : MimeTypeUtils.IMAGE_JPEG;
        var userMessage = UserMessage.builder()
                .text("Image: ")
                .media(new Media(mimeType, image))
                .build();

        var prompt = Prompt.builder()
                .messages(systemMessage, userMessage)
                .build();

        var response = chatClient
                .prompt(prompt)
                /*.options(OpenAiChatOptions.builder()
                        .responseFormat(new ResponseFormat(ResponseFormat.Type.JSON_SCHEMA, converter.getJsonSchema()))
                        .build())*/
                .call();

        return response.entity(converter);
    }
}
