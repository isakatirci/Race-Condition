package Race.Condition.Demo.Project;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
@AllArgsConstructor
public class RaceConditionApplication {

    public static final String CUSTOMER_FIRST_NAME = "İSA";
    private final CreditCardService creditCardService;
    private final CustomerRepository customerRepository;
    private final static AtomicInteger counter = new AtomicInteger(0); // a global counter

    @SneakyThrows
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(RaceConditionApplication.class, args);
        RaceConditionApplication application = applicationContext.getBean(RaceConditionApplication.class);
        application.test();
    }

    //https://www.baeldung.com/spring-scheduled-tasks
    public void test() throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Thread thread = createNewTransaction(i);
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        customerRepository.findByFirstName(CUSTOMER_FIRST_NAME).ifPresentOrElse((customer) -> {
            System.out.println("FINISHED: Total Balance: " + customer.getBalance());
        }, () -> {
            System.out.println("Customer Not Found");
        });
    }

    private Thread createNewTransaction(int i) {
        System.out.println("i: " + i);
        final CreditCardTransaction transaction = new CreditCardTransaction();
        transaction.setAmount(1);
        transaction.setProductName("debit");
        transaction.setCustomerFirstName(CUSTOMER_FIRST_NAME);
        Thread thread = new Thread(() -> {
            try {
                creditCardService.sendTransaction(transaction);
            } catch (Exception e) {
                incrementCounter();
                System.out.println("class name:" + e.getClass().getName() + " ERROR!!!" + e.getMessage());
            }

        });
        return thread;
    }

    private static void incrementCounter() {
        int incrementAndGet = counter.incrementAndGet();
        System.out.println("incrementCounter" + Thread.currentThread().getName() + ": " + incrementAndGet);
    }

    @Bean
    public CommandLineRunner loadData() {
        return (args) -> {
            customerRepository.deleteAll();
            customerRepository.findByFirstName(CUSTOMER_FIRST_NAME).ifPresentOrElse((customer) -> {
                customer.setBalance(0);
                customerRepository.save(customer);
            }, () -> {
                Customer speaker = new Customer();
                speaker.setId(1l);
                speaker.setFirstName("İSA");
                speaker.setLastName("KATIRCI");
                customerRepository.save(speaker);

            });
        };
    }


}
