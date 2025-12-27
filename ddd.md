```java
package com.ykb.nl.sepa.mapper;

import com.ykb. nl.sepa.dao.entity.OutgoingTransactionEntity;
import com.ykb. nl.sepa.dao.entity.OutgoingTransactionEntityWR;
import com.ykb.nl.sepa.dto.OutgoingTransactionIntbankQueryResponseDTO;
import jakarta.validation.constraints.NotNull;

/**
 * Mapper interface for OutgoingTransaction entities to DTOs.
 * Handles mapping of outgoing transaction data for intbank queries.
 */
public interface OutgoingTransactionIntbankMapper {

    /**
     * Maps OutgoingTransactionEntity to IntbankQueryResponseDTO.
     * 
     * @param entity the source entity, must not be null
     * @return mapped DTO with transaction status conversion
     */
    @NotNull
    OutgoingTransactionIntbankQueryResponseDTO toIntbankResponseDTO(@NotNull OutgoingTransactionEntity entity);

    /**
     * Maps OutgoingTransactionEntity to IntbankQueryResponseDTO.
     * 
     * @param entity the source entity
     * @return mapped DTO with transaction status conversion
     */
    OutgoingTransactionIntbankQueryResponseDTO toResponseDTO(OutgoingTransactionEntity entity);

    /**
     * Maps OutgoingTransactionEntityWR to IntbankQueryResponseDTO.
     * 
     * @param entity the source entity (write-read variant)
     * @return mapped DTO with transaction status conversion
     */
    OutgoingTransactionIntbankQueryResponseDTO toOutgoingTransactionIntbankQueryResponseDTO(OutgoingTransactionEntityWR entity);

    /**
     * Maps transaction status using parameter service.
     * If mapping not found, returns original status.
     * 
     * @param transactionStatus the original transaction status
     * @return mapped transaction status or original if mapping not found
     */
    String mapTransactionStatus(String transactionStatus);
}
```

**Kullanım için implementation sınıfı:**

```java
package com.ykb.nl.sepa.mapper;

import com. ykb.nl.sepa. dao.entity.OutgoingTransactionEntity;
import com. ykb.nl.sepa. dao.entity.OutgoingTransactionEntityWR;
import com.ykb.nl.sepa.dto.OutgoingTransactionIntbankQueryResponseDTO;
import com.ykb. nl.sepa.service. ParameterService;
import org. mapstruct.Mapper;
import org.mapstruct. Mapping;
import org.mapstruct.Named;
import jakarta. validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class OutgoingTransactionIntbankMapperImpl implements OutgoingTransactionIntbankMapper {

    @Autowired
    protected ParameterService parameterService;

    private static final String TRANSACTION_STATUS_MAPPING_KEY = "SEPAINTBANKTRANSTATMAPPING";

    @Override
    @Mapping(target = "debtorAccountNo", source = "debitAccount")
    @Mapping(target = "debtorIban", source = "debitIban")
    @Mapping(target = "transactionStatus", source = "transactionStatus", qualifiedByName = "mapTransactionStatus")
    @NotNull
    public abstract OutgoingTransactionIntbankQueryResponseDTO toIntbankResponseDTO(@NotNull OutgoingTransactionEntity entity);

    @Override
    @Mapping(source = "debitAccount", target = "debtorAccountNo")
    @Mapping(source = "debitIban", target = "debtorIban")
    @Mapping(source = "transactionStatus", target = "transactionStatus", qualifiedByName = "mapTransactionStatus")
    public abstract OutgoingTransactionIntbankQueryResponseDTO toResponseDTO(OutgoingTransactionEntity entity);

    @Override
    @Mapping(source = "debitAccount", target = "debtorAccountNo")
    @Mapping(source = "debitIban", target = "debtorIban")
    @Mapping(source = "transactionStatus", target = "transactionStatus", qualifiedByName = "mapTransactionStatus")
    public abstract OutgoingTransactionIntbankQueryResponseDTO toOutgoingTransactionIntbankQueryResponseDTO(OutgoingTransactionEntityWR entity);

    @Override
    @Named("mapTransactionStatus")
    protected String mapTransactionStatus(String transactionStatus) {
        if (transactionStatus == null || transactionStatus.isBlank()) {
            return transactionStatus;
        }
        String mapped = parameterService.getParameterValue(transactionStatus, TRANSACTION_STATUS_MAPPING_KEY);
        return mapped != null && !mapped.isBlank() ? mapped : transactionStatus;
    }
}
```

**Alternatif:  Delegation Pattern ile (daha temiz approach)**

```java
package com.ykb.nl.sepa.mapper;

import com. ykb.nl.sepa. dao.entity.OutgoingTransactionEntity;
import com. ykb.nl.sepa. dao.entity.OutgoingTransactionEntityWR;
import com.ykb.nl.sepa.dto.OutgoingTransactionIntbankQueryResponseDTO;
import com.ykb. nl.sepa.service. ParameterService;
import org. springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.NotNull;

/**
 * Facade for OutgoingTransaction mapping operations.
 * Delegates to MapStruct mapper and adds custom business logic.
 */
@Component
@RequiredArgsConstructor
public class OutgoingTransactionIntbankMapperFacade implements OutgoingTransactionIntbankMapper {

    private final OutgoingTransactionIntbankMapperInternal internalMapper;
    private final ParameterService parameterService;

    private static final String TRANSACTION_STATUS_MAPPING_KEY = "SEPAINTBANKTRANSTATMAPPING";

    @Override
    @NotNull
    public OutgoingTransactionIntbankQueryResponseDTO toIntbankResponseDTO(@NotNull OutgoingTransactionEntity entity) {
        OutgoingTransactionIntbankQueryResponseDTO dto = internalMapper. toIntbankResponseDTO(entity);
        dto.setTransactionStatus(mapTransactionStatus(dto.getTransactionStatus()));
        return dto;
    }

    @Override
    public OutgoingTransactionIntbankQueryResponseDTO toResponseDTO(OutgoingTransactionEntity entity) {
        if (entity == null) {
            return null;
        }
        OutgoingTransactionIntbankQueryResponseDTO dto = internalMapper.toResponseDTO(entity);
        dto.setTransactionStatus(mapTransactionStatus(dto.getTransactionStatus()));
        return dto;
    }

    @Override
    public OutgoingTransactionIntbankQueryResponseDTO toOutgoingTransactionIntbankQueryResponseDTO(OutgoingTransactionEntityWR entity) {
        if (entity == null) {
            return null;
        }
        OutgoingTransactionIntbankQueryResponseDTO dto = internalMapper.toOutgoingTransactionIntbankQueryResponseDTO(entity);
        dto.setTransactionStatus(mapTransactionStatus(dto.getTransactionStatus()));
        return dto;
    }

    @Override
    public String mapTransactionStatus(String transactionStatus) {
        if (transactionStatus == null || transactionStatus.isBlank()) {
            return transactionStatus;
        }
        String mapped = parameterService.getParameterValue(transactionStatus, TRANSACTION_STATUS_MAPPING_KEY);
        return mapped != null && !mapped.isBlank() ? mapped : transactionStatus;
    }

    /**
     * Internal MapStruct mapper - package private
     */
    @Mapper(componentModel = "spring")
    interface OutgoingTransactionIntbankMapperInternal {
        
        @Mapping(target = "debtorAccountNo", source = "debitAccount")
        @Mapping(target = "debtorIban", source = "debitIban")
        OutgoingTransactionIntbankQueryResponseDTO toIntbankResponseDTO(OutgoingTransactionEntity entity);

        @Mapping(source = "debitAccount", target = "debtorAccountNo")
        @Mapping(source = "debitIban", target = "debtorIban")
        OutgoingTransactionIntbankQueryResponseDTO toResponseDTO(OutgoingTransactionEntity entity);

        @Mapping(source = "debitAccount", target = "debtorAccountNo")
        @Mapping(source = "debitIban", target = "debtorIban")
        OutgoingTransactionIntbankQueryResponseDTO toOutgoingTransactionIntbankQueryResponseDTO(OutgoingTransactionEntityWR entity);
    }
}
```

**Önerilen Yaklaşım (DDD Uyumlu):**

```java
package com.ykb.nl.sepa.mapper;

import com. ykb.nl.sepa. dao.entity.OutgoingTransactionEntity;
import com. ykb.nl.sepa. dao.entity.OutgoingTransactionEntityWR;
import com.ykb.nl.sepa.dto.OutgoingTransactionIntbankQueryResponseDTO;
import jakarta.validation.constraints.NotNull;

/**
 * Port interface for OutgoingTransaction mapping (Hexagonal Architecture).
 * Defines contract for mapping transaction entities to DTOs.
 */
public interface OutgoingTransactionIntbankMapper {

    @NotNull
    OutgoingTransactionIntbankQueryResponseDTO toIntbankResponseDTO(@NotNull OutgoingTransactionEntity entity);

    OutgoingTransactionIntbankQuery
