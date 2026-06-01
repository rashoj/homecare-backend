package com.homecare.repository;

import com.homecare.entity.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {

    List<ConversationParticipant> findByUserIdAndActiveTrueOrderByJoinedAtDesc(Long userId);

    List<ConversationParticipant> findByConversationIdAndActiveTrue(Long conversationId);

    Optional<ConversationParticipant> findByConversationIdAndUserIdAndActiveTrue(
            Long conversationId,
            Long userId
    );

    boolean existsByConversationIdAndUserIdAndActiveTrue(
            Long conversationId,
            Long userId
    );
}