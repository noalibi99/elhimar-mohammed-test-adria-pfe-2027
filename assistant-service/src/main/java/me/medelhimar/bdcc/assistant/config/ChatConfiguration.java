package me.medelhimar.bdcc.assistant.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfiguration {

    @Bean
    ChatClient bankingChatClient(ChatModel chatModel, ToolCallbackProvider toolCallbackProvider) {
        return ChatClient.builder(chatModel)
                .defaultSystem("""
                        You are a banking operations assistant. Use the provided tools whenever the user asks
                        to read or change banking data. Never invent account IDs, balances, transactions, or
                        operation results. Before a destructive financial operation, ensure all required values
                        are present. Report tool errors clearly and keep answers concise.
                        """)
                .defaultToolCallbacks(toolCallbackProvider)
                .build();
    }
}
