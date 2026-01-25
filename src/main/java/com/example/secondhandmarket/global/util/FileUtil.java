package com.example.secondhandmarket.global.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class FileUtil {

    @Value("${file.dir}")
    private String fileDir;

    /**
     * 단일 파일 저장
     */
    public String storeFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            return null;
        }

        // 실제 S3 업로드 상황을 가정하여 0.5초 지연 추가
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String storeFileName = createStoreFileName(originalFilename);// UUID 변환

        multipartFile.transferTo(new File(getFullPath(storeFileName)));

        return storeFileName;
    }

    /**
     * 여러 파일 저장
     */
    // [1] 동기 방식
//    public List<String> storeFiles(List<MultipartFile> multipartFiles) throws IOException {
//        List<String> storeFileResult = new ArrayList<>();
//
//        if (multipartFiles == null || multipartFiles.isEmpty()) {
//            return storeFileResult;
//        }
//
//        for (MultipartFile multipartFile : multipartFiles) {
//            if (!multipartFile.isEmpty()) {
//                storeFileResult.add(storeFile(multipartFile));
//            }
//        }
//        return storeFileResult;
//    }

    // [2] 비동기 방식
    public List<String> storeFiles(List<MultipartFile> multipartFiles) {
        if (multipartFiles == null || multipartFiles.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 각 파일 저장을 비동기 Task로 변환하여 병렬 실행
        List<CompletableFuture<String>> futures = multipartFiles.stream()
                .filter(file -> !file.isEmpty())
                .map(file -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return storeFile(file);
                    } catch (IOException e) {
                        // 람다 내부에서는 체크 예외를 런타임 예외로 포장해야 함
                        throw new RuntimeException(e);
                    }
                }))
                .collect(Collectors.toList());

        // 2. 모든 작업이 완료될 때까지 대기(join) 후 결과 수집
        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }


    /************************
     **** 내부 헬퍼 메서드 ****
     ***********************/

    // 파일명 중복 방지를 위해 UUID 사용
    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    // 확장자 추출
    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }

    // 전체 경로 추출
    public String getFullPath(String fileName) {
        return fileDir + fileName; // "C:/images/" + UUID.확장자
    }
}
