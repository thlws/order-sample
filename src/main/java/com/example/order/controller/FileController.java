package com.example.order.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/files")
public class FileController {

    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            // 保存文件到指定目录
            String fileName = file.getOriginalFilename();
            Path path = Paths.get("/usr/local/zone/images", fileName);
            Files.write(path, file.getBytes());

            return ResponseEntity.ok("File uploaded successfully: " + fileName);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed");
        }
    }

    @PostMapping("/uploadMultiple")
    public ResponseEntity<String> handleMultipleFilesUpload(@RequestParam("files") MultipartFile[] files) {
        for (MultipartFile file : files) {
            try {
                String fileName = file.getOriginalFilename();
                Path path = Paths.get("/usr/local/zone/images", fileName);
                Files.write(path, file.getBytes());
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed");
            }
        }
        return ResponseEntity.ok("Files uploaded successfully");
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            // 读取文件内容
            Path path = Paths.get("/usr/local/zone/images", fileName);
            Resource resource = new FileSystemResource(path);

            // 设置文件下载响应头
            String contentType = Files.probeContentType(path);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
