package com.atcontain.qaforge.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchDeleteDTO {
    @NotEmpty(message = "Ids cannot be empty")
    private List<@NotNull(message = "Id cannot be null") Integer> ids;
}
