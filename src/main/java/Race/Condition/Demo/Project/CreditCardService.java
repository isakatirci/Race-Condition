package Race.Condition.Demo.Project;

import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@EnableRetry
//@EnableScheduling
//@EnableAsync
@RequiredArgsConstructor
public class CreditCardService {
    private final CustomerRepository customerRepository;
    private final HistoryRepository historyRepository;
    private final HistoryService historyService;

/*    @Retryable(value = org.springframework.dao.CannotAcquireLockException.class, maxAttempts = 250, backoff = @Backoff(delay = 1000), recover = "sendTransactionRecover")
    @Transactional(isolation = Isolation.SERIALIZABLE)*/
    public void sendTransaction(CreditCardTransaction transaction) {
        sendTransactionHandle(transaction);
    }

    private void sendTransactionHandle(CreditCardTransaction transaction) {
        if (transaction.getCustomerFirstName() != null) {
            customerRepository.findByFirstName(transaction.getCustomerFirstName()).ifPresentOrElse(customer -> {
                historyService.saveMessageToHistory(transaction, "RECEIVED");
                customer.setBalance(customer.getBalance() + transaction.getAmount());
                try {
                    customerRepository.save(customer);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                System.out.printf("The account balance of customer %s has changed: %s%n",
                        customer.getFirstName() + " " + customer.getLastName(), transaction.getAmount());
            }, () -> {
                System.err.printf("Customer with first name %s not found%n", transaction.getCustomerFirstName());
                historyService.saveMessageToHistory(transaction, "ORPHANED");
            });
        } else {
            System.err.println("Error during adding transaction, no IDs given");
            historyService.saveMessageToHistory(transaction, "CORRUPTED");
        }
    }

    @Recover
    public void sendTransactionRecover(Exception exception, CreditCardTransaction transaction) {
        try {
            sendTransactionHandle(transaction);
        } catch (Exception e) {
            System.out.println("sendTransactionRecover has error. class name:" + e.getClass().getName() + " ERROR!!!" + e.getMessage());
        }
    }

}

//http://localhost:8080/h2-console/login.do?jsessionid=fe82cff5d8d5dc81b2c2c20f41d05e07#
