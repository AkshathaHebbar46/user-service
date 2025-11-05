package org.userservice.user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.userservice.user_service.dto.request.UserRequestDTO;
import org.userservice.user_service.dto.response.UserResponseDTO;
import org.userservice.user_service.entity.UserEntity;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "createdAt", ignore = true)
    UserEntity toEntity(UserRequestDTO dto);

    UserResponseDTO toDTO(UserEntity entity);
}
