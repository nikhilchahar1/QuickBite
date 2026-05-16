package com.quickbite.payment.dto;

import com.quickbite.payment.entity.WalletStatement;
import com.quickbite.payment.enums.WalletTransactionType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WalletStatementResponse {

    private Long statementId;
    private WalletTransactionType type;
    private Double amount;
    private Double balanceAfter;
    private String description;
    private Long referenceId;
    private LocalDateTime createdAt;

    public static WalletStatementResponse from(WalletStatement ws) {
        WalletStatementResponse r = new WalletStatementResponse();
        r.setStatementId(ws.getStatementId());
        r.setType(ws.getType());
        r.setAmount(ws.getAmount());
        r.setBalanceAfter(ws.getBalanceAfter());
        r.setDescription(ws.getDescription());
        r.setReferenceId(ws.getReferenceId());
        r.setCreatedAt(ws.getCreatedAt());
        return r;
    }
}