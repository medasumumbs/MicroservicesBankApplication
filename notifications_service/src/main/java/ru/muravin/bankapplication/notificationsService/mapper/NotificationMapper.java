package ru.muravin.bankapplication.notificationsService.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.muravin.bankapplication.notificationsService.dto.NotificationDto;
import ru.muravin.bankapplication.notificationsService.model.Notification;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", source = "dto.timestamp")
    Notification toEntity(NotificationDto dto);
    NotificationDto toDto(Notification notification);
}
