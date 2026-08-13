package com.daytodo.domain.course.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 추억 사진 원본 파일을 영구 저장소에 올리고 접근 가능한 URL 을 돌려준다.
 * (FE 가 보내던 content:// 로컬 URI 는 시간이 지나면 못 읽으므로, 서버가 파일을 받아 영구 URL 로 치환한다.)
 */
public interface MemoryPhotoStorage {

    String upload(Long courseId, MultipartFile image);

    void deleteByUrl(String imageUrl);
}
