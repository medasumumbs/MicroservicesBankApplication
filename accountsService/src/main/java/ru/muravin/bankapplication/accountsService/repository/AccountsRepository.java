package ru.muravin.bankapplication.accountsService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.muravin.bankapplication.accountsService.model.Account;
import ru.muravin.bankapplication.accountsService.model.User;

import java.util.List;
import java.util.Optional;

public interface AccountsRepository extends JpaRepository<Account, Long> {
    List<Account> findAllByUserId(String id);
}
