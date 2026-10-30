package me.medelhimar.bdcc.transaction.services;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.medelhimar.bdcc.transaction.entities.BankTransaction;
import me.medelhimar.bdcc.transaction.repositories.TransactionRepository;
import me.medelhimar.bdcc.transaction.services.AccountClient.AccountView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository repository;
    private final AccountGateway accounts;

    @Value("${bank.transaction.max-amount}")
    private BigDecimal maxAmount;

    public BankTransaction create(UUID accountId, BigDecimal amount, String currency,
                                  String merchantCategory, String country) {
        validateAmount(amount);
        AccountView account = activeAccount(accountId, currency);
        if (BigDecimal.valueOf(account.balance()).compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        BankTransaction transaction = save(accountId, amount, currency, merchantCategory, country);
        accounts.adjustBalance(accountId, -amount.doubleValue());
        return transaction;
    }

    public List<BankTransaction> transfer(UUID sourceId, UUID destinationId, BigDecimal amount, String currency) {
        validateAmount(amount);
        if (sourceId == null || destinationId == null || sourceId.equals(destinationId)) {
            throw new IllegalArgumentException("Transfer accounts must differ");
        }
        AccountView source = activeAccount(sourceId, currency);
        activeAccount(destinationId, currency);
        if (BigDecimal.valueOf(source.balance()).compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        BankTransaction debit = save(sourceId, amount, currency, "TRANSFER_OUT", null);
        BankTransaction credit = save(destinationId, amount, currency, "TRANSFER_IN", null);
        accounts.adjustBalance(sourceId, -amount.doubleValue());
        try {
            accounts.adjustBalance(destinationId, amount.doubleValue());
        } catch (RuntimeException exception) {
            accounts.adjustBalance(sourceId, amount.doubleValue());
            throw exception;
        }
        return List.of(debit, credit);
    }

    @Transactional(readOnly = true)
    public BankTransaction get(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Transaction not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<BankTransaction> list(UUID accountId) {
        return accountId == null ? repository.findAll() : repository.findByAccountId(accountId);
    }

    private AccountView activeAccount(UUID id, String currency) {
        if (id == null || currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Account and currency are required");
        }
        AccountView account = accounts.get(id);
        if (!"ACTIVE".equals(account.status())) {
            throw new IllegalStateException("Account is blocked");
        }
        if (!currency.equals(account.currency())) {
            throw new IllegalArgumentException("Currency does not match the account");
        }
        return account;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || amount.compareTo(maxAmount) > 0
                || !Double.isFinite(amount.doubleValue())) {
            throw new IllegalArgumentException("Amount must be positive and at most " + maxAmount);
        }
    }

    private BankTransaction save(UUID accountId, BigDecimal amount, String currency,
                                 String merchantCategory, String country) {
        return repository.saveAndFlush(new BankTransaction(null, accountId, amount, currency,
                merchantCategory, country, Instant.now()));
    }
}
