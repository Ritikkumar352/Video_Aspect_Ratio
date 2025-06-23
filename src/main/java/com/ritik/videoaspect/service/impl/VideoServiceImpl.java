package com.ritik.videoaspect.service.impl;

import com.ritik.videoaspect.model.VideoUploadGroup;
import com.ritik.videoaspect.model.VideoVersion;
import com.ritik.videoaspect.repository.VideoUploadGroupRepository;
import com.ritik.videoaspect.repository.VideoVersionRepository;
import com.ritik.videoaspect.service.VideoService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class VideoServiceImpl implements VideoService {

    private final VideoVersionRepository videoVersionRepository;
    private final VideoUploadGroupRepository videoUploadGroupRepository;

    public VideoServiceImpl(VideoVersionRepository videoVersionRepository, VideoUploadGroupRepository videoUploadGroupRepository) {
        this.videoVersionRepository = videoVersionRepository;
        this.videoUploadGroupRepository = videoUploadGroupRepository;
    }

    @Override
    public Long handleVideoUpload(MultipartFile file, String mode) {
        try {
            if (!Objects.equals(mode, "pad") && !Objects.equals(mode, "crop")) {
                mode = "crop";
            }

            Path tempDir = Files.createTempDirectory("video_upload_");
            File originalFile = new File(tempDir.toFile(), file.getOriginalFilename());
            file.transferTo(originalFile);

            VideoUploadGroup group = new VideoUploadGroup();
            group.setOriginalFilename(file.getOriginalFilename());
            group = videoUploadGroupRepository.save(group);

            Map<String, String> targets = Map.of(
                    "16:9", "1920x1080",
                    "9:16", "1080x1920",
                    "1:1", "1080x1080",
                    "4:3", "1440x1080"
            );

            for (Map.Entry<String, String> entry : targets.entrySet()) {
                String aspect = entry.getKey();
                String resolution = entry.getValue();

                String[] dims = resolution.split("x");
                String width = dims[0];
                String height = dims[1];

                String outputFileName = UUID.randomUUID() + "_" + aspect.replace(":", "_") + ".mp4";
                File outputFile = new File(tempDir.toFile(), outputFileName);

                String filter;
                if (mode.equals("pad")) {
                    filter = String.format("scale=%s:force_original_aspect_ratio=decrease,pad=%s:%s:(ow-iw)/2:(oh-ih)/2", resolution, width, height);
                } else {
                    filter = String.format("scale=%s:force_original_aspect_ratio=increase,crop=%s:%s", resolution, width, height);
                }

                String[] cmd = {
                        "ffmpeg", "-i", originalFile.getAbsolutePath(),
                        "-vf", filter,
                        "-c:a", "copy",
                        "-y", outputFile.getAbsolutePath()
                };

                System.out.println("\n[FFmpeg Command] " + String.join(" ", cmd));

                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.redirectErrorStream(true);
                Process process = pb.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[FFmpeg] " + line);
                    }
                }

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new RuntimeException("FFmpeg failed for aspect ratio: " + aspect + ", mode: " + mode);
                }

                byte[] data = Files.readAllBytes(outputFile.toPath());
                VideoVersion version = new VideoVersion();
                version.setConvertedFilename(outputFileName);
                version.setAspectRatio(aspect);
                version.setResolution(resolution);
                version.setVideoData(data);
                version.setGroup(group);
                videoVersionRepository.save(version);
            }

            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);

            return group.getId();  // return group id

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error processing video: " + e.getMessage(), e);
        }
    }

    @Override
    public List<VideoVersion> getAllVersionsByGroup(Long groupId) {
        return videoVersionRepository.findAllByGroup_Id(groupId);
    }
}
