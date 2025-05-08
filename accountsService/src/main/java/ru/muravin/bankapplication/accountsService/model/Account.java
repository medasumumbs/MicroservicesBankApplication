package ru.muravin.bankapplication.accountsService.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "accounts")
/*
* <column name="id" type="bigint">
                <constraints primaryKey="true"/>
            </column>
            <column name="user_id" type="bigint" >
            </column>
            <column name="currency" type="varchar(4)">
                <constraints nullable="false" />
            </column>
            <column name="balance" type="decimal">
                <constraints nullable="false" />
            </column>
            <column name="created_at" type="timestamp">
                <constraints nullable="false" />
            </column>*/
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String userId;
    private String currency;
    private Float Balance;
    @Setter
    private LocalDateTime createdAt;
}
