package com.hscmt.simulation;

import com.hscmt.common.util.FileUtil;
import com.hscmt.common.util.StringUtil;
import com.hscmt.simulation.common.comp.VirtualEnvironmentComponent;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;

@RestController
@RequiredArgsConstructor
public class Controller {

    private final VirtualEnvironmentComponent vcomp;

    @PostMapping("/download/chrome")
    public void downloadChromePortable (HttpServletResponse response) throws Exception {

        File chrome = new File(FileUtil.getFilePath("GoogleChromePortable122(240802162642).zip", vcomp.getFileServerBasePath()));

        if (!chrome.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, StringUtil.contentDispositionFileName(chrome.getName()));
        response.setContentLengthLong(chrome.length());

        try (FileInputStream fis = new FileInputStream(chrome); ServletOutputStream os = response.getOutputStream();) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
    }
}
