package com.ritik.videoaspect.service.impl;

import com.ritik.videoaspect.model.VideoUploadGroup;
import com.ritik.videoaspect.model.VideoVersion;
import com.ritik.videoaspect.repository.VideoUploadGroupRepository;
import com.ritik.videoaspect.repository.VideoVersionRepository;
import com.ritik.videoaspect.service.VideoConversionService;
import com.ritik.videoaspect.service.VideoService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class VideoServiceImpl implements VideoService {

    private final VideoVersionRepository videoVersionRepository;
    private final VideoUploadGroupRepository videoUploadGroupRepository;
    private final VideoConversionService conversionService;

    public VideoServiceImpl(
            VideoVersionRepository videoVersionRepository,
            VideoUploadGroupRepository videoUploadGroupRepository,
            VideoConversionService conversionService
    ) {
        this.videoVersionRepository = videoVersionRepository;
        this.videoUploadGroupRepository = videoUploadGroupRepository;
        this.conversionService = conversionService;
    }


    // upload and convert
    @Override
    public Map<String, Object> handleVideoUpload(MultipartFile file, String mode) {
        try {
            if (!Objects.equals(mode, "pad") && !Objects.equals(mode, "crop")) {
                mode = "crop";
            }

            Path tempDir = Files.createTempDirectory("video_upload_");
            File originalFile = new File(tempDir.toFile(), file.getOriginalFilename());
            file.transferTo(originalFile);

            // Save group
            VideoUploadGroup group = new VideoUploadGroup();
            group.setOriginalFilename(file.getOriginalFilename());
            group = videoUploadGroupRepository.save(group);

            // Save original version
            byte[] originalData = Files.readAllBytes(originalFile.toPath());
            VideoVersion originalVersion = new VideoVersion();
            originalVersion.setConvertedFilename(file.getOriginalFilename());
            originalVersion.setAspectRatio("original");
            originalVersion.setResolution("original");
            originalVersion.setVideoData(originalData);
            originalVersion.setGroup(group);
            originalVersion.setUploadTime(LocalDateTime.now());
            videoVersionRepository.save(originalVersion);

            // Start background conversion
            conversionService.convertAsync(originalFile, mode, group);


            Map<String, Object> response = new HashMap<>();
            response.put("groupId", group.getId());
            response.put("originalVideoId", originalVersion.getId());
            response.put("streamUrl", "/api/videos/version/" + originalVersion.getId() + "/stream");
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error uploading video: " + e.getMessage(), e);
        }
    }

    // Async background video conversion
//    @Async
//    public CompletableFuture<Void> convertAsync(File originalFile, String mode, VideoUploadGroup group) {
//        try {
//            String[][] targets = {
//                    {"16:9", "1920x1080"},
//                    {"9:16", "1080x1920"},
//                    {"1:1", "1080x1080"},
//                    {"4:3", "1440x1080"}
//            };
//
//            for (String[] entry : targets) {
//                String aspect = entry[0];
//                String resolution = entry[1];
//                String[] dims = resolution.split("x");
//                String width = dims[0];
//                String height = dims[1];
//
//                String outputFileName = UUID.randomUUID() + "_" + aspect.replace(":", "_") + ".mp4";
//                File outputFile = new File(originalFile.getParentFile(), outputFileName);
//
//                String filter;
//                if (mode.equals("pad")) {
//                    filter = String.format("scale=%s:force_original_aspect_ratio=decrease,pad=%s:%s:(ow-iw)/2:(oh-ih)/2", resolution, width, height);
//                } else {
//                    filter = String.format("scale=%s:force_original_aspect_ratio=increase,crop=%s:%s", resolution, width, height);
//                }
//
//                String[] cmd = {
//                        "ffmpeg", "-i", originalFile.getAbsolutePath(),
//                        "-vf", filter,
//                        "-c:a", "copy",
//                        "-y", outputFile.getAbsolutePath()
//                };
//
//                System.out.println("[FFmpeg Command] " + String.join(" ", cmd));
//
//                ProcessBuilder pb = new ProcessBuilder(cmd);
//                pb.redirectErrorStream(true);
//                Process process = pb.start();
//
//                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//                    String line;
//                    while ((line = reader.readLine()) != null) {
//                        System.out.println("[FFmpeg] " + line);
//                    }
//                }
//
//                int exitCode = process.waitFor();
//                if (exitCode != 0) {
//                    System.err.println("FFmpeg failed for aspect ratio: " + aspect);
//                    continue;
//                }
//
//                byte[] data = Files.readAllBytes(outputFile.toPath());
//                VideoVersion version = new VideoVersion();
//                version.setConvertedFilename(outputFileName);
//                version.setAspectRatio(aspect);
//                version.setResolution(resolution);
//                version.setVideoData(data);
//                version.setGroup(group);
//                version.setUploadTime(LocalDateTime.now());
//                videoVersionRepository.save(version);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return CompletableFuture.completedFuture(null);
//    }

    // Get all converted versions of a video group
    @Override
    public List<VideoVersion> getAllVersionsByGroup(Long groupId) {
        return videoVersionRepository.findAllByGroup_Id(groupId);
    }

    // Get video by version ID
    @Override
    public VideoVersion getVideoById(UUID id) {
        return videoVersionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found"));
    }

    // upload no conversion
    @Override
    public UUID uploadOriginalOnly(MultipartFile file) {
        try {
            VideoUploadGroup group = new VideoUploadGroup();
            group.setOriginalFilename(file.getOriginalFilename());
            group = videoUploadGroupRepository.save(group);

            File temp = File.createTempFile("upload_", file.getOriginalFilename());
            file.transferTo(temp);
            byte[] data = Files.readAllBytes(temp.toPath());

            VideoVersion version = new VideoVersion();
            version.setConvertedFilename(file.getOriginalFilename());
            version.setAspectRatio("original");
            version.setResolution("original");
            version.setVideoData(data);
            version.setGroup(group);
            version.setUploadTime(LocalDateTime.now());

            videoVersionRepository.save(version);
            return version.getId();

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload original video: " + e.getMessage(), e);
        }
    }
}
