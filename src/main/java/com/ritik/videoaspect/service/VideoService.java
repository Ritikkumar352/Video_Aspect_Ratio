package com.ritik.videoaspect.service;

import com.ritik.videoaspect.model.VideoVersion;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoService {
    void handleVideoUpload(MultipartFile file);
    List<VideoVersion> getAllVersionsByGroup(Long groupId);
}
