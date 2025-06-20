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
    public ResponseEntity<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        videoService.handleVideoUpload(file);
        return ResponseEntity.ok("Video uploaded and processed.");
    }

    // get all viedo from that group ... using grp id
    @GetMapping("/group/{id}")
    public ResponseEntity<List<VideoVersion>> getAllVersionsByGroup(@PathVariable Long id) {
        List<VideoVersion> versions = videoService.getAllVersionsByGroup(id);
        return ResponseEntity.ok(versions);
    }
}
