package com.jpmc.midascore.controller;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BalanceController {
    private static final Logger logger = LoggerFactory.getLogger(BalanceController.class);

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/balance")
    public Balance getBalance(@RequestParam Long userId) {
        logger.info("Balance query for user ID: {}", userId);

        UserRecord user = userRepository.findById(userId.longValue());

        if (user == null) {
            logger.warn("User with ID {} not found, returning balance of 0", userId);
            return new Balance(0);
        }

        Balance balance = new Balance(user.getBalance());
        logger.info("User {} balance: {}", user.getName(), balance.getAmount());

        return balance;
    }
}
