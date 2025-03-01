package Race.Condition.Demo.Project;

import Race.Condition.Demo.Project.transaction.B;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
@AllArgsConstructor
@EnableRetry
@EnableJpaAuditing
public class RaceConditionApplication {

	public static final String CUSTOMER_FIRST_NAME = "İSA";
	private final CreditCardService creditCardService;
	private final CustomerRepository customerRepository;
	private final static AtomicInteger counter = new AtomicInteger(0); // a global counter

	@SneakyThrows
	public static void main(String[] args) {
		ConfigurableApplicationContext applicationContext = SpringApplication.run(
				RaceConditionApplication.class, args);
		B b = applicationContext.getBean(B.class);
		b.testBSave();
	/*	RaceConditionApplication application = applicationContext.getBean(RaceConditionApplication.class);
		application.test2();*/
	}

	//https://www.baeldung.com/spring-scheduled-tasks
	public void test1() throws InterruptedException {
		List<Thread> threads = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			Thread thread = createNewTransaction1(i);
			threads.add(thread);
			thread.start();
		}
		for (Thread thread : threads) {
			thread.join();
		}

		System.out.println(
				"incrementCounter" + Thread.currentThread().getName() + ": " + counter.get());

		customerRepository.findByFirstName(CUSTOMER_FIRST_NAME).ifPresentOrElse((customer) -> {
			System.out.println("FINISHED: Total Balance: " + customer.getBalance());
		}, () -> {
			System.out.println("Customer Not Found");
		});
	}

	private Thread createNewTransaction1(final int i) {
		final CreditCardTransaction transaction = new CreditCardTransaction();
		transaction.setAmount(1);
		transaction.setProductName("debit");
		transaction.setCustomerFirstName(CUSTOMER_FIRST_NAME);
		Thread thread = new Thread(() -> {
			try {
				System.out.println("i: " + i);
				creditCardService.sendTransaction(transaction);
			} catch (Exception e) {
				incrementCounter();
				System.out.println(
						"class name:" + e.getClass().getName() + " ERROR!!!" + e.getMessage());
			}

		});
		return thread;
	}


	public void test2() throws InterruptedException {
		ArrayList<Future<Integer>> futures = new ArrayList<>();

		ExecutorService executor = Executors.newFixedThreadPool(
				Runtime.getRuntime().availableProcessors()
		);

		for (int i = 0; i < 100; i++) {
			Future<Integer> future = createNewTransaction2(executor, i);
			futures.add(future);
		}

		List<CompletableFuture<Integer>> completableFutures = futures.stream()
				.map(f -> CompletableFuture.supplyAsync(() -> {
					try {
						return f.get();
					} catch (InterruptedException | ExecutionException e) {
						System.out.println(
								"supplyAsync: interrupt has error " + e.getClass().getName()
										+ " ERROR!!!" + e.getMessage());
						throw new RuntimeException(e);
					} catch (Exception e) {
						System.out.println(
								"supplyAsync: unknown has error " + e.getClass().getName()
										+ " ERROR!!!" + e.getMessage());
						throw new RuntimeException(e);
					}
				}))
				.toList();

		CompletableFuture<Void> allFutures = CompletableFuture.allOf(
				completableFutures.toArray(new CompletableFuture[0])
		);

		// Get all results
		List<Integer> results = allFutures.thenApply(v ->
				completableFutures.stream()
						.map(CompletableFuture::join)
						.collect(Collectors.toList())
		).join();

		System.out.println(
				"incrementCounter" + Thread.currentThread().getName() + ": " + counter.get());

		customerRepository.findByFirstName(CUSTOMER_FIRST_NAME).ifPresentOrElse((customer) -> {
			System.out.println("FINISHED: Total Balance: " + customer.getBalance());
		}, () -> {
			System.out.println("Customer Not Found");
		});
	}


	private Future<Integer> createNewTransaction2(ExecutorService executor, final int i) {
		final CreditCardTransaction transaction = new CreditCardTransaction();
		transaction.setAmount(1);
		transaction.setProductName("debit");
		transaction.setCustomerFirstName(CUSTOMER_FIRST_NAME);
		Future<Integer> submit = executor.submit(() -> {
			try {
				System.out.println("i: " + i);
				creditCardService.sendTransaction(transaction);
				return 0;
			} catch (Exception e) {
				incrementCounter();
				System.out.println(
						"class name:" + e.getClass().getName() + " ERROR!!!" + e.getMessage());
				return 1;
			}

		});
		return submit;
	}


	private static void incrementCounter() {
		int incrementAndGet = counter.incrementAndGet();
		System.out.println(
				"incrementCounter" + Thread.currentThread().getName() + ": " + incrementAndGet);
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
