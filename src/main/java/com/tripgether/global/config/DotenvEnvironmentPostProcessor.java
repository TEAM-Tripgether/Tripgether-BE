package com.tripgether.global.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Properties;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Resource resource = new FileSystemResource(".env");
        if (resource.exists()) {
            Properties properties = new Properties();
            try {
                // .env 파일을 읽어서 Properties로 변환
                String content = new String(resource.getInputStream().readAllBytes());
                String[] lines = content.split("\n");

                for (String line : lines) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            properties.setProperty(parts[0].trim(), parts[1].trim());
                        }
                    }
                }

                // Spring Environment에 .env 속성 추가
                environment.getPropertySources().addLast(
                    new PropertiesPropertySource("dotenv", properties)
                );

                System.out.println("✅ .env 파일이 성공적으로 로드되었습니다!");

            } catch (IOException e) {
                System.err.println("❌ .env 파일 읽기 실패: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ .env 파일을 찾을 수 없습니다. 기본 설정을 사용합니다.");
        }
    }
}
