package com.imcloud.saas_user.objStorage.controller;

import com.imcloud.saas_user.common.dto.ApiResponse;
import com.imcloud.saas_user.common.dto.ErrorResponseDto;
import com.imcloud.saas_user.common.dto.ErrorType;
import com.imcloud.saas_user.common.security.UserDetailsImpl;
import com.imcloud.saas_user.objStorage.service.NaverObjectStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Tag(name = "Object Storage Management")
@RestController
@RequestMapping("/api/objstorage")
@RequiredArgsConstructor
public class ObjStorageController {
    private final NaverObjectStorageService storageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file to object storage")
    public ApiResponse<?> uploadFile(
            @RequestParam String folder,
            @RequestParam("file") MultipartFile file,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        if (!folder.equals("original") && !folder.equals("processed")) {
            ErrorResponseDto errorResponseDto = ErrorResponseDto.of(ErrorType.ILLEGAL_ARGUMENT_EXCEPTION, "Invalid folder name");
            return ApiResponse.failOf(HttpStatus.BAD_REQUEST, errorResponseDto);
        }
        String filePath = storageService.handleUpload(file, userDetails, folder);
        return ApiResponse.successOf(HttpStatus.CREATED, filePath);
    }

    @GetMapping("/list")
    @Operation(summary = "List all files in object storage for the user")
    public ApiResponse<?> listFiles(
            @RequestParam String folder,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (!folder.equals("original") && !folder.equals("processed")) {
            ErrorResponseDto errorResponseDto = ErrorResponseDto.of(ErrorType.ILLEGAL_ARGUMENT_EXCEPTION, "Invalid folder name");
            return ApiResponse.failOf(HttpStatus.BAD_REQUEST, errorResponseDto);
        }
        List<String> files = storageService.listFiles(userDetails, folder);
        return ApiResponse.successOf(HttpStatus.OK, files);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete a file from object storage")
    public ApiResponse<?> deleteFile(@RequestParam String folder,
                                          @RequestParam String objectKey,
                                          @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (!folder.equals("original") && !folder.equals("processed")) {
            ErrorResponseDto errorResponseDto = ErrorResponseDto.of(ErrorType.ILLEGAL_ARGUMENT_EXCEPTION, "Invalid folder name");
            return ApiResponse.failOf(HttpStatus.BAD_REQUEST, errorResponseDto);
        }
        storageService.deleteFile(objectKey, userDetails, folder);
        return ApiResponse.successOf(HttpStatus.OK, "File deleted successfully");
    }

    @GetMapping("/download")
    @Operation(summary = "Get a signed URL for downloading a file (1 hour)")
    public ApiResponse<?> getSignedUrl(@RequestParam String folder,
                                            @RequestParam String objectKey,
                                            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        if (!folder.equals("original") && !folder.equals("processed")) {
            ErrorResponseDto errorResponseDto = ErrorResponseDto.of(ErrorType.ILLEGAL_ARGUMENT_EXCEPTION, "Invalid folder name");
            return ApiResponse.failOf(HttpStatus.BAD_REQUEST, errorResponseDto);
        }
        String url = storageService.getSignedUrl(objectKey, userDetails, folder);
        return ApiResponse.successOf(HttpStatus.OK, url);
    }

    @GetMapping("/buckets")
    @Operation(summary = "List all buckets in object storage")
    public ApiResponse<List<String>> listAllBuckets() {
        List<String> buckets = storageService.listAllBuckets();
        return ApiResponse.successOf(HttpStatus.OK, buckets);
    }

    @GetMapping("/storage-duration")
    @Operation(summary = "Get storage duration for each file of the user")
    public ApiResponse<Map<String, String>> getFileStorageDuration(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Map<String, String> durations = storageService.calculateFileStorageDuration(userDetails);
        return ApiResponse.successOf(HttpStatus.OK, durations);
    }

    @GetMapping("/network-traffic")
    @Operation(summary = "Get network traffic for each storage log of the user")
    public ApiResponse<Map<String, Long>> getNetworkTraffic(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Map<String, Long> trafficData = storageService.getNetworkTrafficForEachLog(userDetails);
        return ApiResponse.successOf(HttpStatus.OK, trafficData);
    }
}

