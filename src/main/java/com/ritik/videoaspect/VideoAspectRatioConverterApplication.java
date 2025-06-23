package com.ritik.videoaspect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class VideoAspectRatioConverterApplication {

	public static void main(String[] args) {
		SpringApplication.run(VideoAspectRatioConverterApplication.class, args);
	}

}
