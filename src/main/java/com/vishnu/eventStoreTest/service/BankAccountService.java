package com.vishnu.eventStoreTest.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.vishnu.eventStoreTest.dto.BankAccountEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventDataBuilder;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.EventStoreDBClientSettings;
import com.eventstore.dbclient.EventStoreDBConnectionString;
import com.eventstore.dbclient.ReadStreamOptions;
import com.eventstore.dbclient.ResolvedEvent;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vishnu.eventStoreTest.beans.BankAccount;
import com.vishnu.eventStoreTest.repo.BankAccountRepository;


@Service
public class BankAccountService {

    private static final String CONNECTION_STRING = "esdb://localhost:2113?tls=false";
    private static final String STREAM_NAME = "bankAccountStream";
    private final EventStoreDBClient client;

    @Autowired
    private BankAccountRepository accountRepository;
    
    public BankAccountService() {
        EventStoreDBClientSettings settings = EventStoreDBConnectionString.parseOrThrow(CONNECTION_STRING);
        client = EventStoreDBClient.create(settings);
    }
    public void saveAccountData(BankAccount bankAccount) {
        accountRepository.save(bankAccount);
    }
    public BankAccount findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    public EventData createEventData(String eventType, String accountNumber, double amount) {
        UUID eventId = UUID.randomUUID();
        String eventData = "{\"eventType\":\"" + eventType + "\","
                + "\"accountNumber\":\"" + accountNumber + "\","
                + "\"amount\":" + amount + "}";
        return EventDataBuilder.json(eventId,eventType,eventData.getBytes()).build();
    }

    public void storeEvent(BankAccountEvent event) {
        EventData eventData = createEventData(event.getEventType(), event.getAccountNumber(), event.getAmount());
        client.appendToStream(STREAM_NAME, eventData).join();
    }

    public List<BankAccountEvent> getAllEvents() {
        // Read all events from the stream
        List<ResolvedEvent> resolvedEvents = client.readStream(STREAM_NAME,ReadStreamOptions.get()).join().getEvents();
        //List<ResolvedEvent> resolvedEvents = client.readStream(STREAM_NAME).join().getEvents();
        // Map resolved events to BankAccountEvent objects
        List<BankAccountEvent> events = new ArrayList<>();
        for (ResolvedEvent resolvedEvent : resolvedEvents) {
            BankAccountEvent event = createBankAccountEvent(resolvedEvent);
            events.add(event);
        }
        return events;
    }

    public List<BankAccountEvent> getAccountsEvents(String accountNumber) {
        // Read all events from the stream
        List<ResolvedEvent> resolvedEvents = client.readStream(STREAM_NAME,ReadStreamOptions.get()).join().getEvents();
        // Map resolved events to BankAccountEvent objects
        List<BankAccountEvent> events = new ArrayList<>();
        for (ResolvedEvent resolvedEvent : resolvedEvents) {
            BankAccountEvent event = createBankAccountEvent(resolvedEvent);
            if(event.getAccountNumber().equals(accountNumber)) {
                events.add(event);
            }
        }
        return events;
    }

    private BankAccountEvent createBankAccountEvent(ResolvedEvent resolvedEvent) {
            String eventType = resolvedEvent.getOriginalEvent().getEventType();
            String eventJson = new String(resolvedEvent.getOriginalEvent().getEventData(), StandardCharsets.UTF_8);
            JsonObject eventObject = new JsonParser().parse(eventJson).getAsJsonObject();

            String accountNumber = eventObject.get("accountNumber").getAsString();
            double amount = eventObject.get("amount").getAsDouble();

            return new BankAccountEvent(eventType, accountNumber, amount);
    }
}
