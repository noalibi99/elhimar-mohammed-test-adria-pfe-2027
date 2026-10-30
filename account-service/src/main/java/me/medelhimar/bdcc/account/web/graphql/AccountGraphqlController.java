package me.medelhimar.bdcc.account.web.graphql;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.medelhimar.bdcc.account.entities.Account;
import me.medelhimar.bdcc.account.entities.AccountStatus;
import me.medelhimar.bdcc.account.services.AccountService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AccountGraphqlController {

    private final AccountService service;

    @QueryMapping
    public Account account(@Argument String id) {
        return service.get(UUID.fromString(id));
    }

    @QueryMapping
    public List<Account> accounts() {
        return service.list();
    }

    @MutationMapping
    public Account createAccount(@Argument String customerReference, @Argument String currency,
                                 @Argument double balance) {
        return service.create(customerReference, currency, balance);
    }

    @MutationMapping
    public Account changeAccountStatus(@Argument String id, @Argument AccountStatus status) {
        return service.changeStatus(UUID.fromString(id), status);
    }

    @MutationMapping
    public Account adjustAccountBalance(@Argument String id, @Argument double change) {
        return service.adjustBalance(UUID.fromString(id), change);
    }
}
