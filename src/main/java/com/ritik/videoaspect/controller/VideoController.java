package com.ritik.videoaspect.controller;

import com.ritik.videoaspect.dto.VideoVersionDTO;
import com.ritik.videoaspect.model.VideoVersion;
import com.ritik.videoaspect.service.VideoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    // Upload original + async conversion
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "mode", defaultValue = "crop") String mode
    ) {
        Map<String, Object> result = videoService.handleVideoUpload(file, mode);
        return ResponseEntity.ok(result);
    }

    // Upload original only (no conversion)
    @PostMapping("/upload-original")
    public ResponseEntity<Map<String, Object>> uploadOriginalOnly(
            @RequestParam("file") MultipartFile file
    ) {
        UUID videoId = videoService.uploadOriginalOnly(file);
        Map<String, Object> response = new HashMap<>();
        response.put("videoId", videoId);
        response.put("streamUrl", "/api/videos/version/" + videoId + "/stream");
        return ResponseEntity.ok(response);
    }

    // Get all versions by groupId
    @GetMapping("/group/{id}")
    public ResponseEntity<List<VideoVersionDTO>> getAllVersionsByGroup(@PathVariable Long id) {
        return ResponseEntity.ok(videoService.getAllVersionsByGroup(id));
    }


    // Stream video by UUID
    @GetMapping("/version/{id}/stream")
    public ResponseEntity<byte[]> streamVideo(@PathVariable UUID id) {
        VideoVersion video = videoService.getVideoById(id);
        return ResponseEntity.ok()
                .header("Content-Type", "video/mp4")
                .header("Content-Disposition", "inline; filename=" + video.getConvertedFilename())
                .body(video.getVideoData());
    }
}
