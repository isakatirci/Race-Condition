package Race.Condition.Demo.Project;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@Table(name = "history")
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customerFirstName")
    private String customerFirstName;

    private int amount;

    private String status;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime created;

}
