package com.xpinjection.ai.bank;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class BankService {
    private static final String SYSTEM_PROMPT = """
    You are Bank Spend Explorer, an assistant that answers user questions about their spending in a given period.
    You have access to web search to find MCC codes for categories
    and SQL tool to run read-only SQL queries on the DB defined by schema in DDL format:

    ```
    {DB_SCHEMA}
    ```

    Rules:

    * Parse user request → extract date range, category, card/account filters.
    * If MCCs are given: validate & use. Else: search MCCs via web search tool.
    * Build **read-only SQL** (only columns/tables in specified DB schema). Filter by date + MCC + card/account.
    * Never fabricate MCCs or schema fields.
    * Never run write queries.""";

    private static final String DB_SCHEMA_RESOURCE_PATH = "db/migration/V2__transactions.sql";

    private final ChatClient chatClient;
    private final String dbSchema;

    public BankService(ChatClient.Builder chatClientBuilder, List<McpSyncClient> mcpClients) throws IOException {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpClients))
                .build();
        this.dbSchema = new ClassPathResource(DB_SCHEMA_RESOURCE_PATH).getContentAsString(StandardCharsets.UTF_8);
    }

    public String reply(String userMessage) {
        return chatClient
                .prompt()
                .system(s -> s.text(SYSTEM_PROMPT).param("DB_SCHEMA", dbSchema))
                .user(userMessage)
                .call()
                .content();
    }
}
