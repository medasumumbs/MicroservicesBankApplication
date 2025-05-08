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
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String login;
    @Setter
    private String password;
    private String name;
    private String lastName;
    private String patronymic;
    private LocalDate dateOfBirth;
    @Setter
    private LocalDateTime createdAt;
}
