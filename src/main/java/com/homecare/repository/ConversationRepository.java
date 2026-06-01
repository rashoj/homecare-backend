package com.homecare.repository;

import com.homecare.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByClientIdAndActiveTrueOrderByUpdatedAtDesc(Long clientId);
}