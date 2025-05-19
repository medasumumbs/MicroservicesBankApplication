package ru.muravin.bankapplication.accountsService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.muravin.bankapplication.accountsService.model.User;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByLogin(String login);
}
