package me.medelhimar.bdcc.transaction.services;

import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "account-service", url = "${bank.account-service.url}")
public interface AccountClient {

    @GetMapping("/api/accounts/{id}")
    AccountView get(@PathVariable UUID id);

    @PatchMapping("/api/accounts/{id}/balance")
    AccountView adjustBalance(@PathVariable UUID id, @RequestBody BalanceChange change);

    record AccountView(UUID id, String status, String currency, double balance) {
    }

    record BalanceChange(double change) {
    }
}
