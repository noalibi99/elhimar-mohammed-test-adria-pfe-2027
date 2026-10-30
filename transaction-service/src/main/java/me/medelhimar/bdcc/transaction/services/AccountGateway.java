package me.medelhimar.bdcc.transaction.services;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.medelhimar.bdcc.transaction.services.AccountClient.AccountView;
import me.medelhimar.bdcc.transaction.services.AccountClient.BalanceChange;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountGateway {

    private final AccountClient client;

    @CircuitBreaker(name = "accountService")
    public AccountView get(UUID id) {
        return client.get(id);
    }

    @CircuitBreaker(name = "accountService")
    public AccountView adjustBalance(UUID id, double change) {
        return client.adjustBalance(id, new BalanceChange(change));
    }
}
