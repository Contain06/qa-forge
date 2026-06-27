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
public class ApiInfoAddDTO {
    @NotNull(message = "Project id cannot be null")
    private Integer projectId;

    @NotBlank(message = "API name cannot be blank")
    private String apiName;

    @NotBlank(message = "Request method cannot be blank")
    @Pattern(regexp = "^(GET|POST|PUT|DELETE|PATCH)$", message = "Request method must be GET, POST, PUT, DELETE or PATCH")
    private String requestMethod;

    @NotBlank(message = "API path cannot be blank")
    @Pattern(regexp = "^/.*", message = "API path must start with /")
    private String apiPath;

    @Schema(type = "object", description = "JSON request headers")
    private JsonNode requestHeaders;

    @Schema(type = "object", description = "JSON request params")
    private JsonNode requestParams;

    @Schema(type = "object", description = "JSON request body")
    private JsonNode requestBody;
    private String description;

}
