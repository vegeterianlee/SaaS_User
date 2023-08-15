package com.imcloud.saas_user.activityLog.dto;

import com.imcloud.saas_user.common.entity.ActivityLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
public class ActivityLogResponseDto {

    @Schema(type = "integer", example = "1", description = "Activity log ID")
    private Long id;

    @Schema(type = "string", example = "User123", description = "User ID")
    private String userId;

    @Schema(type = "string", example = "192.168.1.1", description = "IP Address")
    private String ipAddress;

    @Schema(type = "string", example = "example.txt", description = "File Name")
    private String fileName;

    @Schema(type = "string", example = "5", description = "Processing Time")
    private String processingTime;

    @Schema(type = "string", example = "2023-08-14T12:00:00", description = "Created At")
    private LocalDateTime createdAt;

    @Schema(type = "integer", example = "5", description = "Number of Columns in log details")
    private Integer numberOfColumns;

    @Schema(type = "Long", example = "1024KB", description = "Data Size in KB")
    private Long dataSize;

    @Schema(type = "string", example = "excel", description = "Data Type")
    private String dataType;

    @Schema(description = "List of log details")
    private List<LogDetailDto> logDetails;



    @Builder
    @Getter
    @Setter
    public static class LogDetailDto {
        @Schema(type = "string", example = "Categorization", description = "Data Method")
        private String dataMethod;

        @Schema(type = "string", example = "CV_POSITION", description = "Column Name")
        private String columnName;

        @Schema(type = "string", example = "CV_POSITION categorizaed, data size: 1024 KB", description = "Description")
        private String description;

    }

    public static ActivityLogResponseDto of(ActivityLog activityLog) {
        ActivityLogResponseDtoBuilder builder = ActivityLogResponseDto.builder()
                .id(activityLog.getId())
                .userId(activityLog.getUserId())
                .ipAddress(activityLog.getIpAddress())
                .fileName(activityLog.getFileName())
                .processingTime(activityLog.getProcessingTime())
                .createdAt(activityLog.getCreatedAt())
                .numberOfColumns(activityLog.getLogDetailSet() != null ? activityLog.getLogDetailSet().size() : 0);

        if (activityLog.getLogDetailSet() != null && !activityLog.getLogDetailSet().isEmpty()) {
            builder.dataSize(activityLog.getLogDetailSet().iterator().next().getDataSize())
                    .dataType(activityLog.getLogDetailSet().iterator().next().getDataType())
                    .logDetails(activityLog.getLogDetailSet().stream()
                            .map(detail -> LogDetailDto.builder()
                                    .dataMethod(detail.getDataMethod())
                                    .columnName(detail.getColumnName())
                                    .description(detail.getDescription())
                                    .build())
                            .collect(Collectors.toList()));
        } else {
            builder.logDetails(Collections.emptyList());
        }

        return builder.build();
    }


}
