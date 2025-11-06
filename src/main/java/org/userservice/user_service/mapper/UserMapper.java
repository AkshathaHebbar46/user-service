package org.userservice.user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.userservice.user_service.dto.request.user.UserRequestDTO;
import org.userservice.user_service.dto.response.user.UserResponseDTO;
import org.userservice.user_service.entity.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "role", ignore = true)
    UserEntity toEntity(UserRequestDTO dto);

    UserResponseDTO toDTO(UserEntity entity);
}
