package com.jpmc.midascore.service;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionProcessorService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionProcessorService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private IncentiveApiService incentiveApiService;

    @Transactional
    public void processTransaction(Transaction transaction) {
        logger.info("Processing transaction: {}", transaction);

        // Get sender and recipient
        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());

        if (sender == null) {
            logger.error("Sender with ID {} not found", transaction.getSenderId());
            return;
        }

        if (recipient == null) {
            logger.error("Recipient with ID {} not found", transaction.getRecipientId());
            return;
        }

        // Check if sender has sufficient balance
        if (sender.getBalance() < transaction.getAmount()) {
            logger.warn("Insufficient balance for sender {}: has {}, needs {}",
                    sender.getId(), sender.getBalance(), transaction.getAmount());
            return;
        }

        // Get incentive from API
        float incentive = incentiveApiService.getIncentive(transaction);
        logger.info("Incentive for transaction: {}", incentive);

        // Process the transaction
        sender.setBalance(sender.getBalance() - transaction.getAmount());
        recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentive);

        // Save updated records
        userRepository.save(sender);
        userRepository.save(recipient);

        // Record the transaction in the database with incentive
        TransactionRecord transactionRecord = new TransactionRecord(sender, recipient, transaction.getAmount(), incentive);
        transactionRepository.save(transactionRecord);

        logger.info("Transaction processed successfully: {} sent {} to {} (incentive: {})",
                sender.getName(), transaction.getAmount(), recipient.getName(), incentive);
    }
}
