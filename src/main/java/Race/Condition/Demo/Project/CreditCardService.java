package Race.Condition.Demo.Project;

import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

@Service
//@EnableRetry
//@EnableScheduling
//@EnableAsync
@RequiredArgsConstructor
public class CreditCardService {
    private final CustomerRepository customerRepository;
    private final HistoryRepository historyRepository;
    private final HistoryService historyService;


    // @Transactional(isolation = Isolation.REPEATABLE_READ, timeout = 60)
    // @Retryable(value = org.springframework.dao.CannotAcquireLockException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000), recover = "addLikesToSpeakerRecover")
    //@Scheduled(fixedDelay = 1000)
    //@Async
    public void sendTransaction(CreditCardTransaction transaction) {
        if (transaction.getCustomerFirstName() != null) {
            customerRepository.findByFirstName(transaction.getCustomerFirstName()).ifPresentOrElse(customer -> {
                historyService.saveMessageToHistory(transaction, "RECEIVED");
                customer.setBalance(customer.getBalance() + transaction.getAmount());
                customerRepository.save(customer);
                System.out.printf("The account balance of customer %s has changed: %s%n", customer.getFirstName() + " " + customer.getLastName(), transaction.getAmount());
            }, () -> {
                System.err.printf("Customer with first name %s not found%n", transaction.getCustomerFirstName());
                historyService.saveMessageToHistory(transaction, "ORPHANED");
            });
        } else {
            System.err.println("Error during adding transaction, no IDs given%n");
            historyService.saveMessageToHistory(transaction, "CORRUPTED");
        }
    }
}
