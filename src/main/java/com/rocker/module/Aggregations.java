package com.rocker.module;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document("Aggregations")
public class Aggregations {

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter YYYYMMDDHHMMSS = DateTimeFormatter.ofPattern("yyyyMMddHHmmssnn");

    private ObjectId _id;
    private HeaderLine header;
    private List<DetailLine> details;
    private TrailerLine trailer;

    public void initiateHeader(String line) {
        String[] attributes = line.split("\\|", 6);
        header = HeaderLine.builder()
                .recordType(attributes[0])
                .sendingSystem(attributes[1])
                .billingYearMonth(attributes[2])
                .legalEntity(attributes[3])
                .cutOffDate(LocalDate.parse(attributes[4], YYYYMMDD))
                .fileCreationDateTime(LocalDateTime.parse(attributes[5], YYYYMMDDHHMMSS))
                .build();
    }

    public void initiateDetail(String line) {
        if (null == details) {
            details = new ArrayList<>();
        }
        String[] attributes = line.split("\\|", 12);
        DetailLine detail = DetailLine.builder()
                .recordType(attributes[0])
                .sequenceNumber(attributes[1])
                .subSequenceNumber(Integer.valueOf(attributes[2]))
                .businessId(attributes[3])
                .invoiceAccountName(attributes[4])
                .generalProductName(attributes[5])
                .gtTaxCode(attributes[6])
                .taxZeroRateInd(attributes[7])
                .amountCurrency(attributes[8])
                .amountExclTax(new BigDecimal(attributes[9]))
                .taxAmount(new BigDecimal(attributes[10]))
                .taxRate(new BigDecimal(attributes[11]))
                .build();
        details.add(detail);
    }

    public void initiateTrailer(String line) {
        String[] attributes = line.split("\\|", 4);
        trailer = TrailerLine.builder()
                .recordType(attributes[0])
                .numberOfDetailRecords(Integer.parseInt(attributes[1]))
                .totalAmountExclTax(new BigDecimal(attributes[2]))
                .totalTaxAmount(new BigDecimal(attributes[3]))
                .build();
    }
}
