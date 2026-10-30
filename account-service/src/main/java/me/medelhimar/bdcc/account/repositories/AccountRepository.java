package me.medelhimar.bdcc.account.repositories;

import java.util.UUID;
import me.medelhimar.bdcc.account.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, UUID> {
}
