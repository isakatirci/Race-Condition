package Race.Condition.Demo.Project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class CreditCardTransaction {
    private Long id;
    private int amount;
    private String productName;
    private String customerFirstName;
}

