package com.ritik.videoaspect.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "video_upload_groups")
@Data
public class VideoUploadGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalFilename;

    private Date uploadTime = new Date();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<VideoVersion> versions = new ArrayList<>();
}
