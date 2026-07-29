package com.daytodo.domain.user.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ProfileImageStorage {

    String upload(Long userId, MultipartFile image);

    void deleteByUrl(String imageUrl);
}
