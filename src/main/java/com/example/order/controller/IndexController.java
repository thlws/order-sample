package com.example.order.controller;

import com.example.order.dto.OrderDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@Slf4j
public class IndexController {

    @Value("${spring.cloud.nacos.discovery.metadata.x-saic-tag:}")
    private String tag;

    private static final AtomicLong counter = new AtomicLong(0);

    @PostMapping("/add")
    public OrderDto add(@RequestBody OrderDto orderDto) {
        log.info(orderDto.toString());
        return orderDto;
    }

    @RequestMapping("/slow/{seconds}")
    public ResponseEntity<String> index(@PathVariable(value = "seconds") Integer seconds) {
        try {
            if (counter.incrementAndGet() / 3 == 0) {
                TimeUnit.SECONDS.sleep(seconds);
            }
        } catch (Exception e) {
            log.error("error",e);
        }
        return ResponseEntity.ok("seconds=" + seconds);
    }

    @RequestMapping("/list")
    public ResponseEntity<Map<String,Object>> index(HttpServletRequest request) {
        String data = request.getParameter("data");
        String userId= request.getHeader("userid");
        String brandCode= request.getHeader("brandCode");
        String cellphone= request.getHeader("cellphone");
        OrderDto.UserDto.UserDtoBuilder userDtoBuilder = OrderDto.UserDto.builder()
                .userId(userId)
                .data(data)
                .brandCode(brandCode)
                .cellphone(cellphone);

        List<OrderDto> dtoList = Arrays.asList(
                OrderDto.builder()
                        .userDto(userDtoBuilder.build())
                        .orderId("1")
                        .userId("1")
                        .productId("1")
                        .productName("苹果")
                        .productPrice("1")
                        .build(),
                OrderDto.builder()
                        .userDto(userDtoBuilder.build())
                        .orderId("2")
                        .userId("2")
                        .productId("2")
                        .productName("香蕉")
                        .productPrice("2")
                        .build()
        );

        Map<String,Object> map = new java.util.HashMap<>();
        map.put("tag",tag);
        map.put("dtoList",dtoList);

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-saic-service-name", "order");
        headers.add("x-saic-service-ip", "192.168.8.210");

        return new ResponseEntity<>(map, headers, HttpStatus.OK);

        //return ResponseEntity.ok(map);

    }

    // 处理单文件上传
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@RequestPart("file") MultipartFile file) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            return new ResponseEntity<>("请选择要上传的文件", HttpStatus.BAD_REQUEST);
        }

        try {
            // 确保上传目录存在
            File uploadDir = new File(System.getProperty("user.dir") + "/uploads");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成唯一文件名，避免覆盖
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + fileExtension;

            // 保存文件
            Path path = Paths.get(uploadDir.getAbsolutePath() + File.separator + fileName);
            Files.write(path, file.getBytes());

            return new ResponseEntity<>("文件上传成功: " + fileName, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("文件上传失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    // 处理多文件上传
    @PostMapping("/upload-multiple")
    public ResponseEntity<String> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        if (files.length == 0) {
            return new ResponseEntity<>("请选择要上传的文件", HttpStatus.BAD_REQUEST);
        }

        File uploadDir = new File(System.getProperty("user.dir") + "/uploads");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        StringBuilder result = new StringBuilder();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {

                    // 生成唯一文件名
                    String originalFilename = file.getOriginalFilename();
                    String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    String fileName = UUID.randomUUID().toString() + fileExtension;

                    // 保存文件
                    Path path = Paths.get(uploadDir.getAbsolutePath() + File.separator + fileName);
                    Files.write(path, file.getBytes());

                    result.append("文件上传成功: ").append(fileName).append("<br>");
                } catch (IOException e) {
                    result.append("文件上传失败 (").append(file.getOriginalFilename()).append("): ").append(e.getMessage()).append("<br>");
                }
            }
        }

        return new ResponseEntity<>(result.toString(), HttpStatus.OK);
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
