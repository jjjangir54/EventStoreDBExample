package com.vishnu.eventStoreTest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BankAccountEvent {

    private String eventType;
    private String accountNumber;
    private double amount;

    // Constructors, getters, setters
}
