package com.task.spribe.model.table;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("user")
@Builder
public class User {
    @Id
    @Column("id")
    private Long id;
    @Column("username")
    private String username;
    @Column("role")
    private String role;
}
