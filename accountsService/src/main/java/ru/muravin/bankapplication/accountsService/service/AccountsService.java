package ru.muravin.bankapplication.accountsService.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.muravin.bankapplication.accountsService.dto.NewAccountDto;
import ru.muravin.bankapplication.accountsService.model.Account;
import ru.muravin.bankapplication.accountsService.repository.AccountsRepository;
import ru.muravin.bankapplication.accountsService.repository.UsersRepository;

import java.time.LocalDateTime;

@Service
public class AccountsService {
    private final UsersRepository usersRepository;
    private AccountsRepository accountsRepository;

    @Autowired
    public AccountsService(AccountsRepository accountsRepository, UsersRepository usersRepository) {
        this.accountsRepository = accountsRepository;
        this.usersRepository = usersRepository;
    }

    public String addAccount(NewAccountDto newAccountDto) {
        var user = usersRepository.findUserByLogin(newAccountDto.getLogin()).orElse(null);
        if (user == null) {
            return  "Пользователь " + newAccountDto.getLogin() + " не найден";
        }
        var existingAccount = accountsRepository.findByUserIdAndCurrency(
                String.valueOf(user.getId()), newAccountDto.getCurrencyCode()
        );
        if (existingAccount != null) {
            return "У пользователя " + newAccountDto.getLogin() + " уже открыт счёт в выбранной валюте";
        }
        var account = new Account();
        account.setBalance(0f);
        account.setCurrency(newAccountDto.getCurrencyCode());
        account.setCreatedAt(LocalDateTime.now());
        account.setUserId(String.valueOf(user.getId()));
        accountsRepository.save(account);
        return "OK";
    }
}
