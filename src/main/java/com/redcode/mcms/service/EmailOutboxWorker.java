package com.redcode.mcms.service;

import com.redcode.mcms.entity.EmailOutbox;
import com.redcode.mcms.repository.EmailOutboxRepository;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;

@Singleton
@Startup
public class EmailOutboxWorker {
    @Inject private EmailOutboxRepository repository;
    @Inject private EmailOutboxService service;

    @Schedule(second = "0", minute = "*/1", hour = "*", persistent = false)
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void dispatchDueMessages() {
        for (EmailOutbox message : repository.findDue(25)) service.dispatch(message.getId());
    }
}
