package com.ritik.videoaspect.service;

import com.ritik.videoaspect.model.VideoVersion;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface VideoService {
    Map<String, Object> handleVideoUpload(MultipartFile file, String mode);  // changed from void → Long
    List<VideoVersion> getAllVersionsByGroup(Long groupId);
    VideoVersion getVideoById(UUID id);


    // Test
    UUID uploadOriginalOnly(MultipartFile file);
//    VideoVersion getVideoById(UUID id);

}
