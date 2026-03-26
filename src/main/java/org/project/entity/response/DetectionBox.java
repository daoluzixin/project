package org.project.entity.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectionBox {
    private String className;

    private Integer classId;

    private Float confidence;

    private Integer x1;

    private Integer y1;
    private Integer x2;
    private Integer y2;
}