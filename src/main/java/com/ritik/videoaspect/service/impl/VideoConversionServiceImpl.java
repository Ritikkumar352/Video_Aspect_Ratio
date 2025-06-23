package com.ritik.videoaspect.service.impl;
import com.ritik.videoaspect.model.VideoUploadGroup;
import com.ritik.videoaspect.model.VideoVersion;
import com.ritik.videoaspect.repository.VideoVersionRepository;
import com.ritik.videoaspect.service.VideoConversionService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VideoConversionServiceImpl implements VideoConversionService {

    private final VideoVersionRepository videoVersionRepository;

    public VideoConversionServiceImpl(VideoVersionRepository videoVersionRepository) {
        this.videoVersionRepository = videoVersionRepository;
    }

    @Override
    @Async
    public void convertAsync(File originalFile, String mode, VideoUploadGroup group) {
        try {
            String[][] targets = {
                    {"16:9", "1920x1080"},
                    {"9:16", "1080x1920"},
                    {"1:1", "1080x1080"},
                    {"4:3", "1440x1080"}
            };

            for (String[] entry : targets) {
                String aspect = entry[0];
                String resolution = entry[1];
                String[] dims = resolution.split("x");
                String width = dims[0];
                String height = dims[1];

                String outputFileName = UUID.randomUUID() + "_" + aspect.replace(":", "_") + ".mp4";
                File outputFile = new File(originalFile.getParentFile(), outputFileName);

                String filter = mode.equals("pad")
                        ? String.format("scale=%s:force_original_aspect_ratio=decrease,pad=%s:%s:(ow-iw)/2:(oh-ih)/2", resolution, width, height)
                        : String.format("scale=%s:force_original_aspect_ratio=increase,crop=%s:%s", resolution, width, height);

                String[] cmd = {
                        "ffmpeg", "-i", originalFile.getAbsolutePath(),
                        "-vf", filter,
                        "-c:a", "copy",
                        "-y", outputFile.getAbsolutePath()
                };

                System.out.println("[FFmpeg Command] " + String.join(" ", cmd));

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
                    System.err.println("FFmpeg failed for aspect ratio: " + aspect);
                    continue;
                }

                byte[] data = Files.readAllBytes(outputFile.toPath());
                VideoVersion version = new VideoVersion();
                version.setConvertedFilename(outputFileName);
                version.setAspectRatio(aspect);
                version.setResolution(resolution);
                version.setVideoData(data);
                version.setGroup(group);
                version.setUploadTime(LocalDateTime.now());
                videoVersionRepository.save(version);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
