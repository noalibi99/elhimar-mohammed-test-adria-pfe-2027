package me.medelhimar.bdcc.account.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import me.medelhimar.bdcc.account.entities.Account;
import me.medelhimar.bdcc.account.entities.AccountStatus;
import me.medelhimar.bdcc.account.services.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService service;

    @PostMapping
    public ResponseEntity<Account> create(@Valid @RequestBody CreateAccount request) {
        Account account = service.create(request.customerReference(), request.currency(), request.balance());
        return ResponseEntity.created(URI.create("/api/accounts/" + account.getId())).body(account);
    }

    @GetMapping("/{id}")
    public Account get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping
    public List<Account> list() {
        return service.list();
    }

    @PatchMapping("/{id}/status")
    public Account changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeStatus request) {
        return service.changeStatus(id, request.status());
    }

    @PatchMapping("/{id}/balance")
    public Account adjustBalance(@PathVariable UUID id, @Valid @RequestBody BalanceChange request) {
        return service.adjustBalance(id, request.change());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail notFound(NoSuchElementException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail invalid(IllegalArgumentException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail blocked(IllegalStateException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    public record CreateAccount(@NotBlank String customerReference, @NotBlank String currency,
                                @NotNull Double balance) {
    }

    public record ChangeStatus(@NotNull AccountStatus status) {
    }

    public record BalanceChange(@NotNull Double change) {
    }
}
