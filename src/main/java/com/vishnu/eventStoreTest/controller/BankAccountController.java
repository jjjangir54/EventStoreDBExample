package com.vishnu.eventStoreTest.controller;

import java.util.List;

import com.vishnu.eventStoreTest.dto.BankAccountEvent;
import com.vishnu.eventStoreTest.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.vishnu.eventStoreTest.beans.BankAccount;

@RestController
@RequestMapping("/accounts")
public class BankAccountController {

    @Autowired
    private BankAccountService service;

    @PostMapping("/create")
    public ResponseEntity<String> createAccount(@RequestParam String accountNumber, @RequestParam double balance) {
        BankAccount accountCheck = service.findByAccountNumber(accountNumber);
        if (accountCheck == null) {
            service.saveAccountData(new BankAccount(accountNumber, balance));

            BankAccountEvent event = new BankAccountEvent("AccountCreated", accountNumber, balance);
            service.storeEvent(event);
            return new ResponseEntity<>("Account Opened successfully", HttpStatus.CREATED);
        }
        BankAccountEvent event = new BankAccountEvent("Account_Already_Exist", accountNumber, balance);
        service.storeEvent(event);
        return new ResponseEntity<>("Account already Exist", HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/deposit")
    public ResponseEntity<String> depositFunds(@RequestParam String accountNumber, @RequestParam double balance) {
        BankAccount account = service.findByAccountNumber(accountNumber);
        if (account != null) {
            double newBalance = account.getBalance() + balance;
            account.setBalance(newBalance);
            service.saveAccountData(account);

            BankAccountEvent event = new BankAccountEvent("Funds_Deposited", account.getAccountNumber(), newBalance);
            service.storeEvent(event);
            return new ResponseEntity<>("Funds Deposited successfully", HttpStatus.OK);
        }
        BankAccountEvent event = new BankAccountEvent("Account_Not_Exist", accountNumber, balance);
        service.storeEvent(event);
        return new ResponseEntity<>(("Please check your account number. No account found with " + accountNumber), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdrawFunds(@RequestParam String accountNumber, @RequestParam double balance) {
        BankAccount account = service.findByAccountNumber(accountNumber);
        if (account != null && account.getBalance() >= balance) {
            double newBalance = account.getBalance() - balance;
            account.setBalance(newBalance);
            service.saveAccountData(account);

            BankAccountEvent event = new BankAccountEvent("Funds_Withdrawn", account.getAccountNumber(), newBalance);
            service.storeEvent(event);
            return new ResponseEntity<>("Funds Withdrawn successfully", HttpStatus.OK);
        }
        if (account == null) {
            BankAccountEvent event = new BankAccountEvent("Account_Not_Exist", accountNumber, balance);
            service.storeEvent(event);
            return new ResponseEntity<>(("Please check your account number. No account found with " + accountNumber), HttpStatus.BAD_REQUEST);
        }
        BankAccountEvent event = new BankAccountEvent("Insufficient_Funds", accountNumber, account.getBalance());
        service.storeEvent(event);
        return new ResponseEntity<>(("Your account have Insufficient Funds : " + account.getBalance()), HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/events")
    public ResponseEntity<List<BankAccountEvent>> getAllEvents() {
        try {
            return ResponseEntity.ok(service.getAllEvents());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/events/{accountNumber}")
    public ResponseEntity<List<BankAccountEvent>> getAccountEvents(@PathVariable String accountNumber) {
        try {
            return ResponseEntity.ok(service.getAccountsEvents(accountNumber));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<BankAccount> getAccount(@PathVariable String accountNumber) {
        // Retrieve account from DB
        try {
            BankAccount account = service.findByAccountNumber(accountNumber);
            double balance = account.getBalance();
            return ResponseEntity.ok(new BankAccount(accountNumber, balance));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
/*        BankAccount account = service.findByAccountNumber(accountNumber);
        if (account != null) {
            double balance = account.getBalance();
            return ResponseEntity.ok(new BankAccount(accountNumber, balance));
        }

    @GetMapping("/{accountNumber}/exists")
    public ResponseEntity<Boolean> checkAccountExists(@PathVariable String accountNumber) {
        // Check if the account exists in the Oracle DB or based on events and return the response
        // ...
        return new ResponseEntity<>(HttpStatus.OK);
    }*/

