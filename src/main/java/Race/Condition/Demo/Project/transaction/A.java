package Race.Condition.Demo.Project.transaction;

import Race.Condition.Demo.Project.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class A {

	private final CustomerRepository customerRepository;

	@Transactional()
	public void testA_findAll_forEach_println() {
		customerRepository.findAll().forEach(System.out::println);
	}
}
