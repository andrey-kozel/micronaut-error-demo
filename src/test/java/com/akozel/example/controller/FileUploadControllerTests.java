package com.akozel.example.controller;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.multipart.MultipartBody;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

@MicronautTest
class FileUploadControllerTests {

  private static final byte[] ANY_BIG_FILE = StringUtils.repeat("Data", 20000000)
    .getBytes(StandardCharsets.UTF_8);

  @Inject
  @Client("/api/storage")
  private HttpClient client;

  private final CloseableHttpClient closeableHttpClient = HttpClients.custom()
    .setMaxConnPerRoute(50)
    .setMaxConnTotal(200)
    .setDefaultRequestConfig(RequestConfig.custom()
      .setConnectTimeout(60000)
      .build())
    .evictIdleConnections(5, TimeUnit.MINUTES)
    .evictExpiredConnections()
    .build();

  @Test
  public void micronautHttpClientWorksAsExpected() {
    IntStream.range(0, 30)
      .forEach(attempt -> {
        final MultipartBody multipartBody = MultipartBody
          .builder()
          .addPart("tenantId", "1")
          .addPart("resourceId", "1")
          .addPart("objectType", "MAIL")
          .addPart("encryptionKey", "MAIL")
          .addPart("retentionTime", "1")
          .addPart("tags", "{}")
          .addPart("file", "file.json", MediaType.TEXT_PLAIN_TYPE, ANY_BIG_FILE)
          .build();
        final HttpResponse response = client
          .toBlocking()
          .exchange(HttpRequest.POST("/fullPath", multipartBody)
            .contentType(MediaType.MULTIPART_FORM_DATA_TYPE)
            .accept(MediaType.APPLICATION_JSON_TYPE), String.class);

        assertThat(response.getStatus().getCode()).isEqualTo(200);
      });
  }

  @Test
  @Property(name = "micronaut.server.multipart.disk", value = "true")
  public void outOfMemoryOn600XmX() {
    IntStream.range(0, 30)
      .forEach(attempt -> {
        final MultipartBody multipartBody = MultipartBody
          .builder()
          .addPart("tenantId", "1")
          .addPart("resourceId", "1")
          .addPart("objectType", "MAIL")
          .addPart("encryptionKey", "MAIL")
          .addPart("retentionTime", "1")
          .addPart("tags", "{}")
          .addPart("file", "file.json", MediaType.TEXT_PLAIN_TYPE, ANY_BIG_FILE)
          .build();
        final HttpResponse response = client
          .toBlocking()
          .exchange(HttpRequest.POST("/fullPath", multipartBody)
            .contentType(MediaType.MULTIPART_FORM_DATA_TYPE)
            .accept(MediaType.APPLICATION_JSON_TYPE), String.class);
        assertThat(response.getStatus().getCode()).isEqualTo(200);
      });
  }

  @Test
  public void apacheClientThrowsLeakException() {
    IntStream.range(0, 30)
      .forEach(attempt -> {
        final String url = "http://localhost:35012/api/storage/fullPath";
        final ContentType contentType = ContentType.TEXT_PLAIN;
        final HttpPost request = new HttpPost(url);
        final MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create()
          .addTextBody("tenantId", "1")
          .addTextBody("objectType", "MAIL")
          .addTextBody("resourceId", "2")
          .addTextBody("tags", "{}")
          .addTextBody("retentionTime", "12")
          .addTextBody("encryptionKey", "key")
          .addBinaryBody("file", ANY_BIG_FILE, contentType, "any-path.txt");

        request.setEntity(multipartEntityBuilder.build());
        execute(request);
      });
  }

  @Test
  @Property(name = "micronaut.server.multipart.disk", value = "true")
  public void apacheClientOutOfMemoryOn500Xmx() {
    IntStream.range(0, 30)
      .forEach(attempt -> {
        final String url = "http://localhost:35012/api/storage/fullPath";
        final ContentType contentType = ContentType.TEXT_PLAIN;
        final HttpPost request = new HttpPost(url);
        final MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create()
          .addTextBody("tenantId", "1")
          .addTextBody("objectType", "MAIL")
          .addTextBody("resourceId", "2")
          .addTextBody("tags", "{}")
          .addTextBody("retentionTime", "12")
          .addTextBody("encryptionKey", "key")
          .addBinaryBody("file", ANY_BIG_FILE, contentType, "any-path.txt");

        request.setEntity(multipartEntityBuilder.build());
        execute(request);
      });
  }

  private void execute(
    final HttpRequestBase request
  ) {
    try {
      final CloseableHttpResponse response = this.closeableHttpClient.execute(request);
      EntityUtils.consume(response.getEntity());
    } catch (Exception ex) {

    }
  }
}
