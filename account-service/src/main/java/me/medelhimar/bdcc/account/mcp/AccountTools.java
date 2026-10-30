package me.medelhimar.bdcc.account.mcp;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.medelhimar.bdcc.account.entities.Account;
import me.medelhimar.bdcc.account.entities.AccountStatus;
import me.medelhimar.bdcc.account.services.AccountService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountTools {

    private final AccountService service;

    @Tool(description = "Get a bank account by its UUID")
    public Account getAccount(String id) {
        return service.get(UUID.fromString(id));
    }

    @Tool(description = "List bank accounts")
    public List<Account> listAccounts() {
        return service.list();
    }

    @Tool(description = "Create a bank account")
    public Account createAccount(String customerReference, String currency, double balance) {
        return service.create(customerReference, currency, balance);
    }

    @Tool(description = "Change the status of a bank account")
    public Account changeAccountStatus(String id, AccountStatus status) {
        return service.changeStatus(UUID.fromString(id), status);
    }

    @Tool(description = "Adjust a bank account balance by a signed amount")
    public Account adjustAccountBalance(String id, double change) {
        return service.adjustBalance(UUID.fromString(id), change);
    }

    @Bean
    public ToolCallbackProvider accountToolCallbacks() {
        return MethodToolCallbackProvider.builder().toolObjects(this).build();
    }
}
