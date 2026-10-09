package com.hscmt.common.util;

import com.hscmt.common.response.CommandResult;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ProcessUtil {
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    /* 프로세스 획득 */
    public static Process getProcess (String command) throws Exception{
        String[] shellCommand = getShellCommand(command);
        ProcessBuilder pb = new ProcessBuilder(shellCommand);
        pb.redirectErrorStream(true);
        return pb.start();
    }

    /* 프로세스 실행 */
    public static CommandResult runProcess(Process process) throws Exception {
        Future<String> stdoutFuture = EXECUTOR.submit(() -> readWithDetectedEncoding(process.getInputStream(), "STDOUT"));
        Future<String> stderrFuture = EXECUTOR.submit(() -> readWithDetectedEncoding(process.getErrorStream(), "STDERR"));

        int exitCode = process.waitFor();
        String stdout = stdoutFuture.get();
        String stderr = stderrFuture.get();

        return CommandResult.builder()
                .exitCode(exitCode)
                .outputMessage(stdout)
                .errorMessage(stderr)
                .build();
    }

    /* 프로세스 실행 */
    public static CommandResult runCommand(String command) throws Exception {
        return runProcess(getProcess(command));
    }

    /* 프로세스 종료 */
    public static CommandResult killProcess(String pid) throws Exception{
        return killProcess(Long.parseLong(pid));
    }
    
    /* 프로세스 종료 */
    public static CommandResult killProcess(long pid) throws Exception{
        String [] killCommand = getKillCommand(pid);
        ProcessBuilder pb = new ProcessBuilder(killCommand);
        pb.redirectErrorStream(true);
        return runProcess(pb.start());
    }

    /* 실행 커맨드 가져오기 */
    private static String[] getShellCommand(String command) {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return new String[]{"cmd.exe", "/c", command};
        } else {
            return new String[]{"/bin/bash", "-c", command}; // Linux/Mac
        }
    }

    /* 종료 커맨드 가져오기 */
    private static String[] getKillCommand(long pid) {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            // /T: 자식 프로세스 포함, /F: 강제 종료
            return new String[]{"cmd.exe", "/c", "taskkill /PID " + pid + " /T /F"};
        } else {
            // 기본적으로 Linux / Mac 은 kill -9
            return new String[]{"/bin/bash", "-c", "kill -9 " + pid};
        }
    }

    /* 프롬프트결과 읽기 */
    private static String readWithDetectedEncoding(InputStream inputStream, String label) throws IOException {
        byte[] rawBytes;

        // 1. InputStream -> ByteArrayOutputStream safely
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int n;
            while ((n = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, n);
            }
            rawBytes = baos.toByteArray();  // 안전하게 종료
        }

        // 2. 인코딩 감지
        String encoding = detectEncoding(rawBytes);
        String result = new String(rawBytes, Charset.forName(encoding));

        // 3. 로그 출력
        for (String line : result.split(System.lineSeparator())) {
            if (!line.isBlank()) {
                log.info("[runCommand {}] {}", label, line);
            }
        }

        return result;
    }

    /* 인코딩감지 */
    private static String detectEncoding(byte[] bytes) {
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(bytes, 0, bytes.length);
        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        return encoding != null ? encoding : Charset.defaultCharset().name();
    }

    /* 실행기 종료 */
    public static void shutdownExecutor() {
        EXECUTOR.shutdown();
        try {
            if (!EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXECUTOR.shutdownNow();
        }
    }
}
