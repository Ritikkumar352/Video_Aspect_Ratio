package com.ritik.videoaspect.controller;

import com.ritik.videoaspect.model.VideoVersion;
import com.ritik.videoaspect.service.VideoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "mode", defaultValue = "crop") String mode
    ) {
        Long groupId = videoService.handleVideoUpload(file, mode);
        return ResponseEntity.ok("Video uploaded and processed. Group ID: " + groupId);
    }

    @GetMapping("/group/{id}")
    public ResponseEntity<List<VideoVersion>> getAllVersionsByGroup(@PathVariable Long id) {
        List<VideoVersion> versions = videoService.getAllVersionsByGroup(id);
        return ResponseEntity.ok(versions);
    }
}
