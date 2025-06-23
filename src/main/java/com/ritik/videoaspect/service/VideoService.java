package com.ritik.videoaspect.service;

import com.ritik.videoaspect.model.VideoVersion;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoService {
    Long handleVideoUpload(MultipartFile file, String mode);  // changed from void → Long
    List<VideoVersion> getAllVersionsByGroup(Long groupId);
}
