package Race.Condition.Demo.Project;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class HistoryService {
    private final HistoryRepository historyRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 1)
    public void saveMessageToHistory(CreditCardTransaction expenditure, String status) {
        try {
            historyRepository.save(History.builder()
                    .customerFirstName(expenditure.getCustomerFirstName())
                    .amount(expenditure.getAmount())
                    .status(status)
                    .build());

        } catch (Exception ex) {
            System.err.println("Failed to save message to history." + ex);
        }
    }
}
