package com.ritik.videoaspect.service;

import com.ritik.videoaspect.model.VideoUploadGroup;

import java.io.File;

public interface VideoConversionService {
    void convertAsync(File originalFile, String mode, VideoUploadGroup group);

}
