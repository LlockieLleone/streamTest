package com.lingjun.streamtest.controller;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/streamtest")
public class download {

    @GetMapping("/download")
    public void download(HttpServletResponse response) throws IOException {
        String filePath = "E:/streamtest.txt";
        File file = new File(filePath);
        InputStream inputStream = new FileInputStream(file);

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=file.txt");

        ServletOutputStream outputStream = response.getOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        inputStream.close();
        outputStream.flush();
        outputStream.close();
    }

    @GetMapping("/stream")
    public SseEmitter stream() throws IOException {
        String filePath = "E:/streamtest.txt";
        File file = new File(filePath);
        InputStream inputStream = new FileInputStream(file);

        SseEmitter emitter = new SseEmitter();

        new Thread(() -> {
            byte[] buffer = new byte[2];
            int len;
            try {
                while ((len = inputStream.read(buffer)) != -1) {
                    String data = new String(buffer, 0, len);
                    emitter.send(data);
                    Thread.sleep(10); // 等待 1 秒
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        return emitter;
    }

}
