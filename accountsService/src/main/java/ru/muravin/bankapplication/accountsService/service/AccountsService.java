package ru.muravin.bankapplication.accountsService.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.muravin.bankapplication.accountsService.dto.AccountDto;
import ru.muravin.bankapplication.accountsService.dto.NewAccountDto;
import ru.muravin.bankapplication.accountsService.dto.OperationDto;
import ru.muravin.bankapplication.accountsService.mapper.AccountMapper;
import ru.muravin.bankapplication.accountsService.model.Account;
import ru.muravin.bankapplication.accountsService.repository.AccountsRepository;
import ru.muravin.bankapplication.accountsService.repository.UsersRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class AccountsService {
    private final UsersRepository usersRepository;
    private final AccountMapper accountMapper;
    private final AccountsRepository accountsRepository;

    @Autowired
    public AccountsService(AccountsRepository accountsRepository, UsersRepository usersRepository, AccountMapper accountMapper) {
        this.accountsRepository = accountsRepository;
        this.usersRepository = usersRepository;
        this.accountMapper = accountMapper;
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

    public List<AccountDto> findAccountsByUsername(String username) {
        var currencyTitles = getCurrencyTitlesMap();
        return usersRepository.findUserByLogin(username).map(user->{
            return accountsRepository.findAllByUserId(String.valueOf(user.getId()));
        }).orElse(new ArrayList<>()).stream().map(accountMapper::toDto)
            .peek(accountDto -> accountDto.getCurrency().setTitle(currencyTitles.get(accountDto.getCurrency().getCurrencyCode())))
            .toList();
    }

    private static HashMap<String, String> getCurrencyTitlesMap() {
        var currencyTitles = new HashMap<String,String>();
        currencyTitles.put("USD", "Доллар США");
        currencyTitles.put("EUR", "Евро");
        currencyTitles.put("RUB", "Рубль");
        return currencyTitles;
    }
    private static String getCurrencyTitle(String currencyCode) {
        return getCurrencyTitlesMap().get(currencyCode);
    }

    public String cashIn(OperationDto operationDto) {
        var user = usersRepository.findUserByLogin(operationDto.getLogin()).orElse(null);
        if (user == null) {
            return "Пользователь " + operationDto.getLogin() + " не найден";
        }
        var account = accountsRepository.findByUserIdAndCurrency(String.valueOf(user.getId()), operationDto.getCurrencyCode());
        if (account == null) {
            return "Счет в валюте '" + getCurrencyTitle(operationDto.getCurrencyCode()) + "' не открыт для пользователя "
                    + operationDto.getLogin();
        }
        account.setBalance(account.getBalance() + Float.parseFloat(operationDto.getAmount()));
        accountsRepository.save(account);
        return "OK";
    }

    public String cashOut(OperationDto operationDto) {
        var user = usersRepository.findUserByLogin(operationDto.getLogin()).orElse(null);
        if (user == null) {
            return "Пользователь " + operationDto.getLogin() + " не найден";
        }
        var account = accountsRepository.findByUserIdAndCurrency(String.valueOf(user.getId()), operationDto.getCurrencyCode());
        if (account == null) {
            return "Счет в валюте '" + getCurrencyTitle(operationDto.getCurrencyCode()) + "' не открыт для пользователя "
                    + operationDto.getLogin();
        }
        var amount = Float.parseFloat(operationDto.getAmount());
        if (account.getBalance() < amount) {
            return "На счету недостаточно средств для списания - не хватает "
                    +  (amount - account.getBalance()) + " единиц в валюте: '" + getCurrencyTitle(operationDto.getCurrencyCode()) + "'";
        }
        account.setBalance(account.getBalance() - Float.parseFloat(operationDto.getAmount()));
        accountsRepository.save(account);
        return "OK";
    }
}
