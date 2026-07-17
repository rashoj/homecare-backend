package com.homecare.ai.dto;

import java.util.List;

public record AIOperationsCenterDTO(

        AgencyHealthDTO health,

        AppointmentInsightDTO appointments,

        OpenShiftInsightDTO openShifts,

        EVVInsightDTO evv,

        IncidentInsightDTO incidents,

        List<RecommendationDTO> recommendations,

        String executiveBrief

) {
}