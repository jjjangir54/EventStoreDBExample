package com.vishnu.eventStoreTest.beans;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class BankAccount {

    @Id
    private String accountNumber;
    private double balance;

    // Constructors, getters, setters
}
