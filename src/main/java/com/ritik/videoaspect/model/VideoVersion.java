package com.ritik.videoaspect.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "video_versions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoVersion {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    private String convertedFilename;
    private String aspectRatio;   //  do we need to store original aspect and res also ??
    private String resolution;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] videoData;

    private LocalDateTime uploadTime = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "group_id")
    private VideoUploadGroup group;

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }
}
