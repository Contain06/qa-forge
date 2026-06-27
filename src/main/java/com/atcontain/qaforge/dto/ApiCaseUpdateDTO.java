package com.atcontain.qaforge.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiCaseUpdateDTO {
    @NotNull(message = "Case id cannot be null")
    private Integer id;

    @NotBlank(message = "Case name cannot be blank")
    private String caseName;

    @Pattern(regexp = "^(P0|P1|P2|P3)?$", message = "Case level must be P0, P1, P2 or P3")
    private String caseLevel;
    @Schema(type = "object", description = "JSON request headers")
    private JsonNode requestHeaders;

    @Schema(type = "object", description = "JSON request params")
    private JsonNode requestParams;

    @Schema(type = "object", description = "JSON request body")
    private JsonNode requestBody;

    @Schema(type = "object", description = "JSON expected result")
    private JsonNode expectedResult;
    private String description;
}
