package me.medelhimar.bdcc.account.services;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import me.medelhimar.bdcc.account.entities.Account;
import me.medelhimar.bdcc.account.entities.AccountStatus;
import me.medelhimar.bdcc.account.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository repository;

    public Account create(String customerReference, String currency, double balance) {
        if (customerReference == null || customerReference.isBlank()
                || currency == null || currency.isBlank()
                || !Double.isFinite(balance) || balance < 0) {
            throw new IllegalArgumentException("Invalid account details");
        }
        return repository.save(Account.create(customerReference, currency, balance));
    }

    @Transactional(readOnly = true)
    public Account get(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Account not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Account> list() {
        return repository.findAll();
    }

    public Account changeStatus(UUID id, AccountStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
        Account account = get(id);
        account.setStatus(status);
        return account;
    }

    public Account adjustBalance(UUID id, double change) {
        Account account = get(id);
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is blocked");
        }
        double balance = account.getBalance() + change;
        if (!Double.isFinite(change) || !Double.isFinite(balance) || balance < 0) {
            throw new IllegalArgumentException("Invalid balance change");
        }
        account.setBalance(balance);
        return account;
    }
}
