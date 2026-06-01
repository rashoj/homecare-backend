package com.homecare.controller;

import com.homecare.dto.ConversationResponse;
import com.homecare.dto.CreateConversationRequest;
import com.homecare.dto.MessageResponse;
import com.homecare.dto.SendMessageRequest;
import com.homecare.service.MessagingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin("*")
public class MessagingController {

    private final MessagingService messagingService;

    public MessagingController(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @PostMapping("/conversations")
    public ConversationResponse createConversation(
            @RequestBody CreateConversationRequest request
    ) {
        return messagingService.createConversation(request);
    }

    @GetMapping("/conversations/user/{userId}")
    public List<ConversationResponse> getUserConversations(
            @PathVariable Long userId
    ) {
        return messagingService.getConversationsByUser(userId);
    }

    @GetMapping("/conversations/{conversationId}/user/{userId}")
    public List<MessageResponse> getMessages(
            @PathVariable Long conversationId,
            @PathVariable Long userId
    ) {
        return messagingService.getMessages(conversationId, userId);
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public MessageResponse sendMessage(
            @PathVariable Long conversationId,
            @RequestBody SendMessageRequest request
    ) {
        request.setConversationId(conversationId);
        return messagingService.sendMessage(request);
    }

    @PutMapping("/messages/{messageId}/read/user/{userId}")
    public MessageResponse markMessageRead(
            @PathVariable Long messageId,
            @PathVariable Long userId
    ) {
        return messagingService.markMessageRead(messageId, userId);
    }
}