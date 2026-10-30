package me.medelhimar.bdcc.assistant.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import me.medelhimar.bdcc.assistant.services.AssistantService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AssistantService service;

    public ChatController(AssistantService service) {
        this.service = service;
    }

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return new ChatResponse(service.chat(request.message()));
    }

    public record ChatRequest(@NotBlank String message) {
    }

    public record ChatResponse(String response) {
    }
}
