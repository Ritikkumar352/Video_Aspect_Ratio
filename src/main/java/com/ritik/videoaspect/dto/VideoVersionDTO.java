package com.ritik.videoaspect.dto;

import lombok.*;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoVersionDTO {
    private UUID id;
    private String convertedFilename;
    private String aspectRatio;
    private String resolution;
    private String streamUrl;
}
