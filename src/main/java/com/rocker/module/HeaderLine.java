package com.rocker.module;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class HeaderLine {

    private String recordType;
    private String sendingSystem;
    private String billingYearMonth;
    private String legalEntity;
    private LocalDate cutOffDate;
    private LocalDateTime fileCreationDateTime;

}
