package me.medelhimar.bdcc.assistant.services;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AssistantService {

    private final ChatClient bankingChatClient;

    public AssistantService(ChatClient bankingChatClient) {
        this.bankingChatClient = bankingChatClient;
    }

    public String chat(String message) {
        return bankingChatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
