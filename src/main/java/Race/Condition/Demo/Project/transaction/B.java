package Race.Condition.Demo.Project.transaction;

import Race.Condition.Demo.Project.Customer;
import Race.Condition.Demo.Project.CustomerRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class B {

	private final CustomerRepository customerRepository;
	private final A a;

	@Transactional
	public void testBSave() {
		a.testA_findAll_forEach_println();
		customerRepository.save(
				new Customer(100L, "Jack1", "Bauer1", 0, LocalDateTime.now(), LocalDateTime.now()));
		customerRepository.save(
				new Customer(200L, "Jack2", "Bauer2", 0, LocalDateTime.now(), LocalDateTime.now()));
		a.testA_findAll_forEach_println();
		customerRepository.save(
				new Customer(300L, "Jack3", "Bauer3", 0, LocalDateTime.now(), LocalDateTime.now()));
		a.testA_findAll_forEach_println();
		customerRepository.save(
				new Customer(400L, "Jack4", "Bauer4", 0, LocalDateTime.now(), LocalDateTime.now()));
		customerRepository.save(
				new Customer(500L, "Jack5", "Bauer5", 0, LocalDateTime.now(), LocalDateTime.now()));
	}
}
