package me.medelhimar.bdcc.transaction.repositories;

import java.util.List;
import java.util.UUID;
import me.medelhimar.bdcc.transaction.entities.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<BankTransaction, UUID> {

    List<BankTransaction> findByAccountId(UUID accountId);
}
