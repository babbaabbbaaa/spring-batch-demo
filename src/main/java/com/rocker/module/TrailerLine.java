package com.rocker.module;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class TrailerLine {

    private String recordType;
    private int numberOfDetailRecords;
    private BigDecimal totalAmountExclTax;
    private BigDecimal totalTaxAmount;
}
