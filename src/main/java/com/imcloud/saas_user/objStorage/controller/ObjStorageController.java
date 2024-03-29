package com.imcloud.saas_user.objStorage.controller;

import com.imcloud.saas_user.common.dto.ApiResponse;
import com.imcloud.saas_user.common.dto.ErrorResponseDto;
import com.imcloud.saas_user.common.dto.ErrorType;
import com.imcloud.saas_user.common.entity.StorageLog;
import com.imcloud.saas_user.common.entity.enums.FileActionType;
import com.imcloud.saas_user.common.security.UserDetailsImpl;
import com.imcloud.saas_user.fileAction.dto.FileActionDto;
import com.imcloud.saas_user.fileAction.dto.FileActionHistoryDto;
import com.imcloud.saas_user.objStorage.dto.StorageLogResponseDto;
import com.imcloud.saas_user.objStorage.service.NaverObjectStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.imcloud.saas_user.common.dto.ErrorType.IO_EXCEPTION;
import static com.imcloud.saas_user.common.dto.ErrorType.SQL_EXCEPTION;

@Tag(name = "Object Storage Management")
@RestController
@RequestMapping("/api/objstorage")
@RequiredArgsConstructor
public class ObjStorageController {
    private final NaverObjectStorageService storageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file to object storage")
    public ApiResponse<?> uploadFile(
            @RequestParam String fileName,
            @RequestParam("file") MultipartFile file,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException, GeneralSecurityException {
        /*if (!folder.equals("original") && !folder.equals("processed")) {
            ErrorResponseDto errorResponseDto = ErrorResponseDto.of(ErrorType.ILLEGAL_ARGUMENT_EXCEPTION, "Invalid folder name");
            return ApiResponse.failOf(HttpStatus.BAD_REQUEST, errorResponseDto);
        }*/
        FileActionDto fileActionDto = storageService.handleUpload(file, fileName, userDetails);
        return ApiResponse.successOf(HttpStatus.CREATED, fileActionDto);
    }

    @GetMapping("/checkToBeDeidentified")
    @Operation(summary = "Check the deidentification status of a storage log")
    public ApiResponse<Boolean> checkDeidentificationStatus(
            @RequestParam Long storageLogId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {

        boolean status = storageService.checkToBeDeidentified(userDetails, storageLogId);
        return ApiResponse.successOf(HttpStatus.OK, status);
    }

    @GetMapping("/file-actions/deidentified-target")
    @Operation(summary = "Find File Actions with Deidentified Target")
    public ApiResponse<Page<FileActionDto>> findFileActionsWithDeidentifiedTarget(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Page<FileActionDto> fileActions = storageService.findFileActionsWithDeidentifiedTarget(userDetails, page, size);
        return ApiResponse.successOf(HttpStatus.OK, fileActions);
    }

    @GetMapping("/file-actions/recent")
    @Operation(summary = "Get Recent File Actions",
            description = "Retrieves a page of FileAction objects for the current user that were stored within the last 7 days.")
    public ApiResponse<Page<FileActionDto>> getRecentFileActions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Page<FileActionDto> fileActions = storageService.findRecentFileActions(userDetails, page, size);
        return ApiResponse.successOf(HttpStatus.OK, fileActions);
    }

    @GetMapping("/toggleIsDeidentifiedTarget")
    @Operation(summary = "Toggle the deidentification status of a storage log")
    public ApiResponse<Boolean> toggleDeidentificationStatus(
            @RequestParam Long storageLogId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {

        boolean newStatus = storageService.toggleIsDeidentifiedTarget(userDetails, storageLogId);
        return ApiResponse.successOf(HttpStatus.OK, newStatus);
    }

    @GetMapping("/search-files")
    @Operation(summary = "Search file actions based on criteria")
    public ApiResponse<Page<FileActionDto>> searchFileActions(
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) String objectKey,
            @RequestParam(required = false) Boolean toBeDeidentified,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime storedAtStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime storedAtEnd,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime isDeidentifiedAtStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime isDeidentifiedAtEnd,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Page<FileActionDto> fileActions = storageService.searchFiles(
                userDetails, fileName, objectKey, toBeDeidentified, storedAtStart, storedAtEnd, isDeidentifiedAtStart, isDeidentifiedAtEnd, page, size);

        return ApiResponse.successOf(HttpStatus.OK, fileActions);
    }

    @GetMapping("/search-filesActionHistory")
    @Operation(summary = "Search file action histories based on criteria")
    public ApiResponse<Page<FileActionHistoryDto>> searchFileActionHistories(
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) String objectKey,
            @RequestParam(required = false) FileActionType actionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime actionTimeStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime actionTimeEnd,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails){

        Page<FileActionHistoryDto> fileActionHistories = storageService.searchFileActionHistories(
                userDetails, fileName, objectKey, actionType,
                actionTimeStart, actionTimeEnd, page, size);

        return ApiResponse.successOf(HttpStatus.OK, fileActionHistories);
    }

    /*@GetMapping("/list")
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
    }*/

    @DeleteMapping("/delete")
    @Operation(summary = "Delete a file from object storage")
    public ApiResponse<?> deleteFile(@RequestParam String objectKey,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
       /* if (!folder.equals("original") && !folder.equals("processed")) {
            ErrorResponseDto errorResponseDto = ErrorResponseDto.of(ErrorType.ILLEGAL_ARGUMENT_EXCEPTION, "Invalid folder name");
            return ApiResponse.failOf(HttpStatus.BAD_REQUEST, errorResponseDto);
        }*/
        storageService.deleteFile(objectKey, userDetails);
        return ApiResponse.successOf(HttpStatus.OK, "File deleted successfully");
    }

    @GetMapping("/download")
    @Operation(summary = "Get a signed URL for downloading a file (1 hour)")
    public ApiResponse<?> getSignedUrl(@RequestParam String objectKey,
                                       @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException, GeneralSecurityException {
        /*if (!folder.equals("original") && !folder.equals("processed")) {
            ErrorResponseDto errorResponseDto = ErrorResponseDto.of(ErrorType.ILLEGAL_ARGUMENT_EXCEPTION, "Invalid folder name");
            return ApiResponse.failOf(HttpStatus.BAD_REQUEST, errorResponseDto);
        }*/
        String url = storageService.getSignedUrl(objectKey, userDetails);
        return ApiResponse.successOf(HttpStatus.OK, url);
    }

    @GetMapping("/exportDecryptedData")
    @Operation(summary = "클라우드에 저장된 파일을 csv 타입의 string으로 반환", description = "클라우드에 저장된 파일을 csv 타입의 string으로 반환")
    public ApiResponse<?> downloadDecryptedData(
            @RequestParam String objectKey,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            String csvData = storageService.getDownloadByString(objectKey, userDetails);
            return ApiResponse.successOf(HttpStatus.OK, csvData);
        } catch (IOException | GeneralSecurityException e) {
            ErrorResponseDto errorResponse = ErrorResponseDto.of(IO_EXCEPTION, e.getMessage());
            return ApiResponse.failOf(HttpStatus.INTERNAL_SERVER_ERROR, errorResponse);
        }
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

