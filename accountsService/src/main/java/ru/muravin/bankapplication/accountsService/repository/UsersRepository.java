package ru.muravin.bankapplication.accountsService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.muravin.bankapplication.accountsService.model.User;

public interface UsersRepository extends JpaRepository<User, Long> {
    Object findUserByLogin(String login);
}
