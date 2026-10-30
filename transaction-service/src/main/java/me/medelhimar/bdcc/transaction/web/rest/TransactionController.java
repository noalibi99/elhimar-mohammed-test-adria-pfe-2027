package me.medelhimar.bdcc.transaction.web.rest;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.medelhimar.bdcc.transaction.entities.BankTransaction;
import me.medelhimar.bdcc.transaction.services.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @PostMapping
    public ResponseEntity<BankTransaction> create(@Valid @RequestBody CreateTransaction request) {
        BankTransaction transaction = service.create(request.accountId(), request.amount(),
                request.currency(), request.merchantCategory(), request.country());
        return ResponseEntity.created(URI.create("/api/transactions/" + transaction.getId())).body(transaction);
    }

    @PostMapping("/transfers")
    public List<BankTransaction> transfer(@Valid @RequestBody Transfer request) {
        return service.transfer(request.sourceId(), request.destinationId(), request.amount(), request.currency());
    }

    @GetMapping("/{id}")
    public BankTransaction get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping
    public List<BankTransaction> list(@RequestParam(required = false) UUID accountId) {
        return service.list(accountId);
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

    @ExceptionHandler({FeignException.class, CallNotPermittedException.class})
    public ProblemDetail accountUnavailable(RuntimeException exception) {
        if (exception instanceof FeignException feign && (feign.status() == 404 || feign.status() == 409)) {
            return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(feign.status()), "Account request failed");
        }
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, "Account service unavailable");
    }

    public record CreateTransaction(@NotNull UUID accountId, @NotNull BigDecimal amount,
                                    @NotBlank String currency, String merchantCategory, String country) {
    }

    public record Transfer(@NotNull UUID sourceId, @NotNull UUID destinationId,
                           @NotNull BigDecimal amount, @NotBlank String currency) {
    }
}
