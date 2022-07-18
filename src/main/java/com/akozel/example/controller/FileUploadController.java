package com.akozel.example.controller;

import javax.validation.constraints.NotBlank;

import com.akozel.example.dto.ResultDto;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.version.annotation.Version;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Part;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.validation.Validated;
import lombok.extern.slf4j.Slf4j;

@Controller("/api/storage")
@Validated
@Version("1")
@Slf4j
public class FileUploadController {

  @Post(value = "/{fullPath}", consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.APPLICATION_JSON)
  public ResultDto upload(
    @PathVariable final String fullPath,
    @Part @NotBlank final String tenantId,
    @Part @Nullable final String resourceId,
    @Part @NotBlank final String objectType,
    @Part @Nullable final String encryptionKey,
    @Part @Nullable final Long retentionTime,
    @Part @Nullable final String tags,
    @Part final CompletedFileUpload file
  ) {
    try {
      log.info("File uploaded {}", file.getName());
      return ResultDto.builder()
        .fileName(file.getName())
        .size(file.getSize())
        .build();
    } finally {
      file.discard();
    }
  }

}
