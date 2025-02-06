package Race.Condition.Demo.Project;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCardTransaction, Long> {
}
