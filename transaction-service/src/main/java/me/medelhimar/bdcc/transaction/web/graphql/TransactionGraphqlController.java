package me.medelhimar.bdcc.transaction.web.graphql;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.medelhimar.bdcc.transaction.entities.BankTransaction;
import me.medelhimar.bdcc.transaction.services.TransactionService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class TransactionGraphqlController {

    private final TransactionService service;

    @QueryMapping
    public BankTransaction transaction(@Argument String id) {
        return service.get(UUID.fromString(id));
    }

    @QueryMapping
    public List<BankTransaction> transactions(@Argument String accountId) {
        return service.list(accountId == null ? null : UUID.fromString(accountId));
    }

    @MutationMapping
    public BankTransaction createTransaction(@Argument String accountId, @Argument BigDecimal amount,
                                             @Argument String currency, @Argument String merchantCategory,
                                             @Argument String country) {
        return service.create(UUID.fromString(accountId), amount, currency, merchantCategory, country);
    }

    @MutationMapping
    public List<BankTransaction> transfer(@Argument String sourceId, @Argument String destinationId,
                                          @Argument BigDecimal amount, @Argument String currency) {
        return service.transfer(UUID.fromString(sourceId), UUID.fromString(destinationId), amount, currency);
    }
}
