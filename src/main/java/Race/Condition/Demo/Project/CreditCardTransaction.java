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
@Table(name = "creditCardTransaction")
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CreditCardTransaction {
    @Id
    private Long id;
    @Column(name = "amount")
    private int amount;
    @Column(name = "productName")
    private String productName;
    @Column(name = "customerFirstName")
    private String customerFirstName;
}

