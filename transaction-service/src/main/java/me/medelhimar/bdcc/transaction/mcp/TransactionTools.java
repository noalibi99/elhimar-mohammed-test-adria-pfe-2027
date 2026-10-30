package me.medelhimar.bdcc.transaction.mcp;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.medelhimar.bdcc.transaction.entities.BankTransaction;
import me.medelhimar.bdcc.transaction.services.TransactionService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionTools {

    private final TransactionService service;

    @Tool(description = "Get a bank transaction by UUID")
    public BankTransaction getTransaction(String id) {
        return service.get(UUID.fromString(id));
    }

    @Tool(description = "List transactions for an account")
    public List<BankTransaction> listTransactions(String accountId) {
        return service.list(UUID.fromString(accountId));
    }

    @Tool(description = "Record a debit transaction")
    public BankTransaction createTransaction(String accountId, BigDecimal amount, String currency,
                                             String merchantCategory, String country) {
        return service.create(UUID.fromString(accountId), amount, currency, merchantCategory, country);
    }

    @Tool(description = "Transfer money between two bank accounts")
    public List<BankTransaction> transfer(String sourceId, String destinationId, BigDecimal amount,
                                          String currency) {
        return service.transfer(UUID.fromString(sourceId), UUID.fromString(destinationId), amount, currency);
    }

    @Bean
    public ToolCallbackProvider transactionToolCallbacks() {
        return MethodToolCallbackProvider.builder().toolObjects(this).build();
    }
}
