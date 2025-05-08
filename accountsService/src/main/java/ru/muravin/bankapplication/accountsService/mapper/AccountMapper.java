package ru.muravin.bankapplication.accountsService.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.muravin.bankapplication.accountsService.dto.AccountDto;
import ru.muravin.bankapplication.accountsService.dto.UserDto;
import ru.muravin.bankapplication.accountsService.model.Account;
import ru.muravin.bankapplication.accountsService.model.User;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currency", source = "dto.currencyCode")
    Account toEntity(AccountDto dto);
    @Mapping(target = "currencyCode", source = "account.currency")
    @Mapping(target = "accountNumber", source = "account.id")
    AccountDto toDto(Account account);
}
