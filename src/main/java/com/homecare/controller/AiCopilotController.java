package com.homecare.controller;

import com.homecare.ai.dto.AIOperationsCenterDTO;
import com.homecare.dto.AiCopilotRequest;
import com.homecare.dto.AiCopilotResponse;
import com.homecare.ai.service.OperationsCenterService;
import com.homecare.service.AiCopilotService;
import com.homecare.service.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-copilot")
public class AiCopilotController {

    private final AiCopilotService aiCopilotService;
    private final CurrentUserService currentUserService;
    private final OperationsCenterService operationsCenterService;

    public AiCopilotController(
            AiCopilotService aiCopilotService,
            CurrentUserService currentUserService,
            OperationsCenterService operationsCenterService
    ) {
        this.aiCopilotService = aiCopilotService;
        this.currentUserService = currentUserService;
        this.operationsCenterService = operationsCenterService;
    }

    @PostMapping("/ask")
    public ResponseEntity<AiCopilotResponse> ask(@RequestBody AiCopilotRequest request) {
        Long organizationId = currentUserService.getCurrentOrganizationId();

        AiCopilotResponse response = aiCopilotService.answer(
                request.getMessage(),
                organizationId
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/operations-center")
    public ResponseEntity<AIOperationsCenterDTO> getOperationsCenter() {
        Long organizationId = currentUserService.getCurrentOrganizationId();

        return ResponseEntity.ok(
                operationsCenterService.getOperationsCenter(organizationId)
        );
    }
}