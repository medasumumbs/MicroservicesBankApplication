package ru.muravin.bankapplication.accountsService.service;

import brave.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.muravin.bankapplication.accountsService.dto.AccountDto;
import ru.muravin.bankapplication.accountsService.dto.CheckedTransferDto;
import ru.muravin.bankapplication.accountsService.dto.NewAccountDto;
import ru.muravin.bankapplication.accountsService.dto.OperationDto;
import ru.muravin.bankapplication.accountsService.mapper.AccountMapper;
import ru.muravin.bankapplication.accountsService.model.Account;
import ru.muravin.bankapplication.accountsService.model.User;
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
    private final NotificationsServiceClient notificationsServiceClient;
    private final Tracer tracer;

    @Autowired
    public AccountsService(AccountsRepository accountsRepository, UsersRepository usersRepository, AccountMapper accountMapper, NotificationsServiceClient notificationsServiceClient, Tracer tracer) {
        this.accountsRepository = accountsRepository;
        this.usersRepository = usersRepository;
        this.accountMapper = accountMapper;
        this.notificationsServiceClient = notificationsServiceClient;
        this.tracer = tracer;
    }

    public String addAccount(NewAccountDto newAccountDto) {
        var span = tracer.nextSpan().name("db findUserByLogin").start();
        var user = usersRepository.findUserByLogin(newAccountDto.getLogin()).orElse(null);
        span.finish();
        if (user == null) {
            return  "Пользователь " + newAccountDto.getLogin() + " не найден";
        }
        span = tracer.nextSpan().name("db findByUserIdAndCurrency").start();
        var existingAccount = accountsRepository.findByUserIdAndCurrency(
                String.valueOf(user.getId()), newAccountDto.getCurrencyCode()
        );
        span.finish();
        if (existingAccount != null) {
            return "У пользователя " + newAccountDto.getLogin() + " уже открыт счёт в выбранной валюте";
        }
        var account = new Account();
        account.setBalance(0f);
        account.setCurrency(newAccountDto.getCurrencyCode());
        account.setCreatedAt(LocalDateTime.now());
        account.setUserId(String.valueOf(user.getId()));
        span = tracer.nextSpan().name("db Saving account").start();
        accountsRepository.save(account);
        span.finish();
        notificationsServiceClient.sendNotification("Registered user " + newAccountDto.getLogin());
        return "OK";
    }

    public List<AccountDto> findAccountsByUsername(String username) {
        var currencyTitles = getCurrencyTitlesMap();
        var span = tracer.nextSpan().name("db Find Accs by username").start();
        var result =  usersRepository.findUserByLogin(username).map(user->{
            return accountsRepository.findAllByUserId(String.valueOf(user.getId()));
        }).orElse(new ArrayList<>()).stream().map(accountMapper::toDto)
            .peek(accountDto -> accountDto.getCurrency().setTitle(currencyTitles.get(accountDto.getCurrency().getCurrencyCode())))
            .toList();
        span.finish();
        return result;
    }

    public static HashMap<String, String> getCurrencyTitlesMap() {
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
        var span = tracer.nextSpan().name("db findUserByLogin").start();
        var user = usersRepository.findUserByLogin(operationDto.getLogin()).orElse(null);
        span.finish();
        if (user == null) {
            return "Пользователь " + operationDto.getLogin() + " не найден";
        }
        span = tracer.nextSpan().name("findByUserIdAndCurrency").start();
        var account = accountsRepository.findByUserIdAndCurrency(String.valueOf(user.getId()), operationDto.getCurrencyCode());
        span.finish();
        if (account == null) {
            return "Счет в валюте '" + getCurrencyTitle(operationDto.getCurrencyCode()) + "' не открыт для пользователя "
                    + operationDto.getLogin();
        }
        account.setBalance(account.getBalance() + Float.parseFloat(operationDto.getAmount()));
        span = tracer.nextSpan().name("db Saving acc").start();
        accountsRepository.save(account);
        span.finish();
        notificationsServiceClient.sendNotification(
            "Cash In " + operationDto.getLogin() + " sum = " + operationDto.getAmount()
        );
        return "OK";
    }

    public String cashOut(OperationDto operationDto) {
        var span = tracer.nextSpan().name("db findUserByLogin").start();
        var user = usersRepository.findUserByLogin(operationDto.getLogin()).orElse(null);
        span.finish();
        if (user == null) {
            return "Пользователь " + operationDto.getLogin() + " не найден";
        }
        span = tracer.nextSpan().name("db findByUserIdAndCurrency").start();
        var account = accountsRepository.findByUserIdAndCurrency(String.valueOf(user.getId()), operationDto.getCurrencyCode());
        span.finish();
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
        span = tracer.nextSpan().name("db Saving acc").start();
        accountsRepository.save(account);
        span.finish();
        notificationsServiceClient.sendNotification(
                "Cash Out " + operationDto.getLogin() + " sum = " + operationDto.getAmount()
        );
        return "OK";
    }

    public AccountDto findAccountByUsernameAndCurrency(String username, String currency) {
        return accountMapper.toDto(
            accountsRepository.findByUserIdAndCurrency(
                String.valueOf(usersRepository.findUserByLogin(username).map(User::getId).orElse(null)),
                currency
            )
        );
    }

    @Transactional
    public String transfer(CheckedTransferDto transferDto) {
        var span = tracer.nextSpan().name("db find account from").start();
        var accountFrom = accountsRepository.findById(Long.valueOf(transferDto.getFromAccountNumber())).orElse(null);
        span.finish();
        if (accountFrom == null) {
            return "Не найден счет № " + transferDto.getFromAccountNumber() + "; Счет был закрыт.";
        }
        if (accountFrom.getBalance() < transferDto.getAmountFrom()) {
            return "Недостаточно средств для перевода на счету № " + transferDto.getFromAccountNumber();
        }
        span = tracer.nextSpan().name("db find account to").start();
        var accountTo = accountsRepository.findById(Long.valueOf(transferDto.getToAccountNumber())).orElse(null);
        span.finish();
        if (accountTo == null) {
            return "Не найден счет № " + transferDto.getToAccountNumber() + "; Счет был закрыт";
        }
        accountFrom.setBalance(accountFrom.getBalance() - Float.parseFloat(String.valueOf(transferDto.getAmountFrom())));
        span = tracer.nextSpan().name("db Saving acc from").start();
        accountsRepository.save(accountFrom);
        span.finish();
        accountTo.setBalance(accountTo.getBalance() + Float.parseFloat(String.valueOf(transferDto.getAmountTo())));
        span = tracer.nextSpan().name("db Saving acc to").start();
        accountsRepository.save(accountTo);
        span.finish();
        notificationsServiceClient.sendNotification("Transfer: " + transferDto);
        return "OK";
    }
}
