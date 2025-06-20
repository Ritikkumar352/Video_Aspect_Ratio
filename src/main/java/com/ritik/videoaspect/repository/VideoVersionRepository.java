package com.ritik.videoaspect.repository;

import com.ritik.videoaspect.model.VideoVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VideoVersionRepository extends JpaRepository<VideoVersion, UUID> {
    List<VideoVersion> findAllByGroup_Id(Long groupId);
}
