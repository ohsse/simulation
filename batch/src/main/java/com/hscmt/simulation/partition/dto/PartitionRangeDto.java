package com.hscmt.simulation.partition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartitionRangeDto {
    private String partitionName;
    private LocalDate fromDate;
    private LocalDate toDate;
}

