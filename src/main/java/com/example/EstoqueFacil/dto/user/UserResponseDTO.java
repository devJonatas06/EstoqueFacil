package com.example.EstoqueFacil.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDTO {

    private Long id;
    private String name;
    private String email;
    private Boolean active;
    private Set<String> roles;
    private LocalDateTime createdAt;
}