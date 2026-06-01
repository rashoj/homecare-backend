package com.homecare.service;

import com.homecare.dto.*;
import com.homecare.entity.*;
import com.homecare.repository.*;
import org.springframework.stereotype.Service;
import com.homecare.dto.NotificationRequest;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessagingService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public MessagingService(
            ConversationRepository conversationRepository,
            ConversationParticipantRepository participantRepository,
            MessageRepository messageRepository,
            UserRepository userRepository,   NotificationService notificationService
    ) {
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public ConversationResponse createConversation(CreateConversationRequest request) {
        User creator = userRepository.findById(request.getCreatedByUserId())
                .orElseThrow(() -> new RuntimeException("Creator user not found"));

        Conversation conversation = Conversation.builder()
                .clientId(request.getClientId())
                .subject(request.getSubject())
                .type(request.getType())
                .active(true)
                .build();

        Conversation savedConversation = conversationRepository.save(conversation);

        ConversationParticipant creatorParticipant = ConversationParticipant.builder()
                .conversation(savedConversation)
                .user(creator)
                .participantRole(creator.getRole().name())
                .active(true)
                .build();

        participantRepository.save(creatorParticipant);

        return mapConversation(savedConversation, creator.getId());
    }

    public List<ConversationResponse> getConversationsByUser(Long userId) {
        return participantRepository.findByUserIdAndActiveTrueOrderByJoinedAtDesc(userId)
                .stream()
                .map(participant ->
                        mapConversation(participant.getConversation(), userId)
                )
                .toList();
    }

    public List<MessageResponse> getMessages(Long conversationId, Long userId) {
        validateParticipant(conversationId, userId);

        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId)
                .stream()
                .map(this::mapMessage)
                .toList();
    }

    public MessageResponse sendMessage(SendMessageRequest request) {
        validateParticipant(request.getConversationId(), request.getSenderUserId());

        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        User sender = userRepository.findById(request.getSenderUserId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .messageBody(request.getMessageBody())
                .readByRecipient(false)
                .build();
        Message savedMessage = messageRepository.save(message);

        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        participantRepository.findByConversationIdAndActiveTrue(conversation.getId())
                .stream()
                .filter(participant ->
                        !participant.getUser().getId().equals(sender.getId())
                )
                .forEach(participant -> {
                    NotificationRequest notification = new NotificationRequest();
                    notification.setUserId(participant.getUser().getId());
                    notification.setTitle("New Message");
                    notification.setMessage(sender.getFullName() + " sent you a new message.");
                    notification.setType("NEW_MESSAGE");
                    notification.setRelatedEntityType("CONVERSATION");
                    notification.setRelatedEntityId(conversation.getId());

                    notificationService.createNotification(notification);
                });

        return mapMessage(savedMessage);
    }

    public MessageResponse markMessageRead(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        validateParticipant(message.getConversation().getId(), userId);

        if (!message.getSender().getId().equals(userId)) {
            message.setReadByRecipient(true);
            message.setReadAt(LocalDateTime.now());
        }

        return mapMessage(messageRepository.save(message));
    }

    private void validateParticipant(Long conversationId, Long userId) {
        boolean allowed = participantRepository
                .existsByConversationIdAndUserIdAndActiveTrue(conversationId, userId);

        if (!allowed) {
            throw new RuntimeException("Not authorized to access this conversation");
        }
    }

    private ConversationResponse mapConversation(Conversation conversation, Long currentUserId) {
        List<Message> lastMessages =
                messageRepository.findTop1ByConversationIdOrderBySentAtDesc(
                        conversation.getId()
                );

        Message lastMessage = lastMessages.isEmpty() ? null : lastMessages.get(0);

        long unreadCount =
                messageRepository.countByConversationIdAndSenderIdNotAndReadByRecipientFalse(
                        conversation.getId(),
                        currentUserId
                );

        return ConversationResponse.builder()
                .id(conversation.getId())
                .clientId(conversation.getClientId())
                .subject(conversation.getSubject())
                .type(conversation.getType())
                .active(conversation.getActive())
                .unreadCount(unreadCount)
                .lastMessage(lastMessage != null ? lastMessage.getMessageBody() : null)
                .lastMessageAt(lastMessage != null ? lastMessage.getSentAt() : null)
                .createdAt(conversation.getCreatedAt())
                .build();
    }

    private MessageResponse mapMessage(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderUserId(message.getSender().getId())
                .senderName(message.getSender().getFullName())
                .senderRole(message.getSender().getRole().name())
                .messageBody(message.getMessageBody())
                .readByRecipient(message.getReadByRecipient())
                .sentAt(message.getSentAt())
                .readAt(message.getReadAt())
                .build();
    }
    public void addParticipant(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean exists = participantRepository
                .existsByConversationIdAndUserIdAndActiveTrue(conversationId, userId);

        if (!exists) {
            ConversationParticipant participant = ConversationParticipant.builder()
                    .conversation(conversation)
                    .user(user)
                    .participantRole(user.getRole().name())
                    .active(true)
                    .build();

            participantRepository.save(participant);
        }
    }
}