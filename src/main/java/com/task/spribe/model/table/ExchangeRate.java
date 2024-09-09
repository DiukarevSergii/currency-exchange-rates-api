package com.task.spribe.model.table;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table("exchange_rate")
@Builder
public class ExchangeRate {

    @Id
    @Column("id")
    private Long id;

    @Column("currency_code")
    private String currencyCode;

    @Column("rate")
    private BigDecimal rate;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("created_by_user_id")
    private Long createdByUserId;

    @Column("updated_by_user_id")
    private Long updatedByUserId;

}
