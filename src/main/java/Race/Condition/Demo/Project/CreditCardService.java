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

    @Retryable(value = org.springframework.dao.CannotAcquireLockException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000), recover = "sendTransactionRecover")
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void sendTransaction(CreditCardTransaction transaction) {
        sendTransactionHandle(transaction);
    }

    private void sendTransactionHandle(CreditCardTransaction transaction) {
        try {
            if (transaction.getCustomerFirstName() != null) {
                customerRepository.findByFirstName(transaction.getCustomerFirstName()).ifPresentOrElse(customer -> {
                    historyService.saveMessageToHistory(transaction, "RECEIVED");
                    customer.setBalance(customer.getBalance() + transaction.getAmount());
                    customerRepository.save(customer);
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
        } catch (Exception e) {
            System.out.println("sendTransactionHandle has exception: " + e);
            throw new RuntimeException(e);
        }
    }

    @Recover
    public void sendTransactionRecover(Exception exception, CreditCardTransaction transaction) {
        try {
            System.out.println("sendTransactionRecover has exception: " + exception);
        } catch (Exception e) {
            System.out.println("sendTransactionRecover has error. class name:" + e.getClass().getName() + " ERROR!!!" + e.getMessage());
        }
    }

}

//http://localhost:8080/h2-console
/*

Parametrelerin Anlamı:

value:

Hangi istisna (exception) türünde yeniden deneme yapılacağını belirtir.
Bu örnekte, CannotAcquireLockException fırlatıldığında metot yeniden çalıştırılacaktır.
Birden fazla istisna belirtmek isterseniz, bir dizi (array) kullanabilirsiniz:
java
@Retryable(value = {IOException.class, CannotAcquireLockException.class})

maxAttempts:

Yeniden deneme sayısını belirtir (ilk deneme dahil).
Bu örnekte, metot toplamda 5 kez çalıştırılacaktır (1 normal deneme + 4 yeniden deneme).
Eğer belirtilen istisna 5 denemeden sonra hâlâ devam ediyorsa, yeniden deneme işlemi durur ve istisna ya fırlatılır ya da @Recover anotasyonu ile belirtilen kurtarma (recovery) metodu devreye girer.

backoff:

Yeniden denemeler arasındaki bekleme süresini (delay) ayarlar.
@Backoff anotasyonu ile şu ayarları yapabilirsiniz:
delay: Yeniden denemeler arasındaki sabit bekleme süresi (milisaniye cinsinden). Bu örnekte, her deneme arasında 1000ms (1 saniye) beklenir.
multiplier (isteğe bağlı): Üstel (exponential) bekleme süresi için kullanılır. Örneğin:
java
@Backoff(delay = 1000, multiplier = 2)
Bu durumda bekleme süreleri sırasıyla 1 saniye, 2 saniye, 4 saniye, 8 saniye şeklinde artar.
recover:

Yeniden deneme hakkı tükendiğinde çağrılacak kurtarma (recovery) metodunu belirtir.
Bu örnekte, sendTransactionRecover metodu, tüm denemeler başarısız olduğunda devreye girer.
Kurtarma metodunun şu kurallara uyması gerekir:
İlk parametresi, yakalanan istisna türü olmalıdır.
@Retryable metodunun parametreleri ile uyumlu olmalıdır.


Örnek Akış:
@Retryable ile işaretlenmiş metot çağrılır.
Eğer belirtilen istisna (CannotAcquireLockException) fırlatılırsa, Spring Retry mekanizması devreye girer.
Metot, belirtilen maxAttempts kadar yeniden çalıştırılır (bu örnekte 5 kez).
Her deneme arasında backoff ile belirtilen süre kadar beklenir (1 saniye).
Eğer tüm denemeler başarısız olursa, recover ile belirtilen kurtarma metodu çağrılır.


* */