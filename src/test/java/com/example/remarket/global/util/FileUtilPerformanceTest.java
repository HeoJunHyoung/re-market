package com.example.remarket.global.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class FileUtilPerformanceTest {

    @Autowired
    private FileUtil fileUtil;

    @Test
    @DisplayName("파일 10개 순차 저장 성능 테스트")
    void sequentialUploadTest() throws IOException {
        // 1. 테스트 준비: 5MB짜리 가짜 이미지 파일 10개 생성
        List<MultipartFile> files = new ArrayList<>();
        int fileCount = 10;
        int sizePerFile = 5 * 1024 * 1024; // 5MB

        for (int i = 0; i < fileCount; i++) {
            // 내용은 0으로 채워진 더미 데이터
            byte[] content = new byte[sizePerFile];
            files.add(new MockMultipartFile(
                    "images",
                    "test_image_" + i + ".jpg",
                    "image/jpeg",
                    content
            ));
        }

        System.out.println("=== 테스트 시작: 5MB 파일 " + fileCount + "개 저장 ===");

        // 2. 시간 측정 시작
        long startTime = System.currentTimeMillis();

        // 3. 실제 로직 실행 (변경 전: 순차 실행)
        fileUtil.storeFiles(files);

        // 4. 시간 측정 종료
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        System.out.println("=== 테스트 종료 ===");
        System.out.println("총 소요 시간: " + totalTime + "ms");
        System.out.println("평균 소요 시간(개당): " + (totalTime / fileCount) + "ms");
    }
}