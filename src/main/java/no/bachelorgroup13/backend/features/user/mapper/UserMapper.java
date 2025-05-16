package no.bachelorgroup13.backend.features.user.mapper;

import org.springframework.stereotype.Component;

import no.bachelorgroup13.backend.features.user.dto.UserDto;
import no.bachelorgroup13.backend.features.user.entity.User;

/**
 * Mapper for converting between User entities and DTOs.
 * Handles the transformation of user data between layers.
 */
@Component
public class UserMapper {

    /**
     * Converts a User entity to a UserDto.
     * @param user The user entity to convert
     * @return The corresponding DTO, or null if input is null
     */
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setLicensePlate(user.getLicensePlate());
        dto.setSecondLicensePlate(user.getSecondLicensePlate());
        dto.setRole(user.getRole());
        return dto;
    }

    /**
     * Converts a UserDto to a User entity.
     * @param dto The DTO to convert
     * @return The corresponding entity, or null if input is null
     */
    public User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setId(dto.getId());
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setLicensePlate(dto.getLicensePlate());
        user.setSecondLicensePlate(dto.getSecondLicensePlate());
        user.setRole(dto.getRole());
        return user;
    }
}
