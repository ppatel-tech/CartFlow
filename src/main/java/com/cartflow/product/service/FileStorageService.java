package com.cartflow.product.service;

import com.cartflow.exception.BusinessException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final Cloudinary cloudinary;

    public String storeFile(MultipartFile file) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", "cartflow/products"));

            String secureUrl = (String) uploadResult.get("secure_url");

            log.info("File uploaded to Cloudinary: {}", secureUrl);

            return secureUrl;

        } catch (IOException ex) {
            throw new BusinessException("Failed to upload file: " + ex.getMessage());
        }
    }
}