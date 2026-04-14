package com.antigravity.logistics.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Data
@Builder
public class RevenueReportDTO {
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal totalRevenue;
    private String groupedBy;
    private List<RevenueGroup> groups;

    @Data
    @Builder
    public static class RevenueGroup {
        private String groupName;
        private BigDecimal revenue;
    }
}
