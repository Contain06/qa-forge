package com.atcontain.qaforge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDTO {
    private Integer id;
    private String projectName;
    private String projectCode;
    private String description;
    private Integer ownerId;
    private Integer status;
}
