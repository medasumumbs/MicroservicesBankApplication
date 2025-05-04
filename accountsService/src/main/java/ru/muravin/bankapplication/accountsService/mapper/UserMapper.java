package ru.muravin.bankapplication.accountsService.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.muravin.bankapplication.accountsService.dto.UserDto;
import ru.muravin.bankapplication.accountsService.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "dto.firstName")
    @Mapping(target = "patronymic", source = "dto.lastName")
    User toEntity(UserDto dto);
    UserDto toDto(User user);
}
