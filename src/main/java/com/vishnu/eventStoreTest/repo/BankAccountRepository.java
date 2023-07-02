package com.vishnu.eventStoreTest.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vishnu.eventStoreTest.beans.BankAccount;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, String> {
    BankAccount findByAccountNumber(String accountNumber);
}
