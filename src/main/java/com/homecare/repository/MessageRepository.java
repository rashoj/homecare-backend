package com.homecare.repository;

import com.homecare.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId);

    List<Message> findTop1ByConversationIdOrderBySentAtDesc(Long conversationId);

    long countByConversationIdAndSenderIdNotAndReadByRecipientFalse(
            Long conversationId,
            Long senderId
    );
}