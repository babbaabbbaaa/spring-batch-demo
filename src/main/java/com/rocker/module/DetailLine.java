package com.rocker.module;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class DetailLine {

    private String recordType;
    private String sequenceNumber;
    private Integer subSequenceNumber;
    private String businessId;
    private String invoiceAccountName;
    private String generalProductName;
    private String gtTaxCode;
    private String taxZeroRateInd;
    private String amountCurrency;
    private BigDecimal amountExclTax;
    private BigDecimal taxAmount;
    private BigDecimal taxRate;


}
