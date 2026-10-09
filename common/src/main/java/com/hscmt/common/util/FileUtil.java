package com.hscmt.common.util;

import com.hscmt.common.enumeration.FileExtension;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class FileUtil {

    public static List<File> getOnlyFilesInDirectory(String path) {
        List<File> files = new ArrayList<>();
        File file = new File(path);
        if (file.isDirectory()) {
            return List.of(file.listFiles());
        } else {
            return null;
        }
    }

    public static void deleteFilesInDirectory(Path directoryPath) throws IOException {
        if (Files.exists(directoryPath) && Files.isDirectory(directoryPath)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directoryPath)) {
                for (Path file : stream) {
                    if (Files.isRegularFile(file)) {
                        Files.delete(file);
                    }
                }
            }
        }
    }

    public static void deleteFilesInDirectory(String directoryPathStr) throws IOException {
        deleteFilesInDirectory(Paths.get(directoryPathStr));
    }

    public static void uploadMultiPartFile (MultipartFile file, String fileSaveDirPath) throws IOException {
        createFile (fileSaveDirPath);
        file.transferTo(new File (fileSaveDirPath + File.separator + file.getOriginalFilename()));
    }

    public static boolean createFile (String filePath) {
        return createFile (new File (filePath));
    }

    public static boolean createFile (File file) {
        if (!file.exists()) {
            return file.mkdirs();
        }
        return false;
    }

    public static String getFileExtension (String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index > 0) {
            return fileName.substring(index + 1);
        }
        return "";
    }

    public static String getFileNameWithoutExtension (String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index > 0) {
            return fileName.substring(0, index);
        }
        return fileName;
    }

    public static void copyFile (String targetPath, String goalPath) throws NoSuchFileException {
        Path source =  Paths.get(targetPath);
        Path target = Paths.get(goalPath);

        if (!Files.exists(source)) {
            log.error("복사할 대상이 존재하지 않음. {}", targetPath);
        }

        try {
            if (Files.isDirectory(source)) {
                // 원본이 디렉토리라면 -> target도 디렉토리여야 함
                // target이 디렉토리 경로가 아니라면: target 위치에 같은 이름으로 디렉토리 생성
                Path targetDir = target;
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }
                // 디렉토리 자체를 옮김
                Files.copy(source, targetDir, StandardCopyOption.REPLACE_EXISTING);
            } else {
                // 원본이 파일이라면 -> target은 파일 경로
                Path targetDir = target.getParent();
                if (targetDir != null) {
                    Files.createDirectories(targetDir);
                }
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            log.error("파일 복사 실패 : {}", e.getMessage());
        }
    }

    public static void moveFile (String targetPath, String goalPath) throws Exception{
        Path source =  Paths.get(targetPath);
        Path target = Paths.get(goalPath);

        if (!Files.exists(source)) {
            log.error("이동시켜야할 원본파일이 존재하지 않아. {}", targetPath);
            throw new NoSuchFileException("target path File not exists");
        }

        try {
            if (Files.isDirectory(source)) {
                // 원본이 디렉토리라면 -> target도 디렉토리여야 함
                // target이 디렉토리 경로가 아니라면: target 위치에 같은 이름으로 디렉토리 생성
                Path targetDir = target;
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }
                // 디렉토리 자체를 옮김
                Files.move(source, targetDir, StandardCopyOption.REPLACE_EXISTING);
            } else {
                // 원본이 파일이라면 -> target은 파일 경로
                Path targetDir = target.getParent();
                if (targetDir != null) {
                    Files.createDirectories(targetDir);
                }
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            log.error("파일 이동 실패 : {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static boolean removeFile (String targetPath) {
        if (targetPath != null) {
            if (Files.isDirectory(Paths.get(targetPath))){
                return removeRecursively(targetPath);
            } else {
                File removeFile = new File(targetPath);
                return removeFile.delete();
            }
        } else {
            return false;
        }
    }

    /**
     * 파일 인코딩 charset 확인하기
     * @param file
     * @return
     */
    public static String getFileEncodingCharset (File file) {
        String charset = null;

        try (
                FileInputStream fis = new FileInputStream(file)
        ){
            byte[] bytes = new byte[4096];
            UniversalDetector detector = new UniversalDetector(null);
            int nread;

            while ((nread = fis.read(bytes)) > 0 && !detector.isDone()) {
                detector.handleData(bytes, 0, nread);
            }

            detector.dataEnd();
            charset = detector.getDetectedCharset();
            detector.reset();
            bytes = null;

        } catch (IOException e) {
            log.error(e.getMessage());
        }

        return charset != null ? charset : StandardCharsets.UTF_8.displayName();
    }

    public static String getFilePath (String fileName, String... pathName) {
        StringBuilder builder = new StringBuilder(getDirPath(pathName));
        builder.append(File.separator);
        builder.append(fileName);
        return builder.toString();
    }

    public static String getUrlPath (String fileName, String ...pathName) {
        final String slash = "/";
        StringBuilder builder = new StringBuilder();

        for (int i = 0 ; i < pathName.length ; i++) {
            if (i != pathName.length - 1) {
                builder.append(pathName[i]).append(slash);
            }else {
                builder.append(pathName[i]);
            }
        }

        builder.append(slash);
        builder.append(fileName);

        return builder.toString();
    }

    public static String getDirPath (String ...pathName) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0 ; i < pathName.length ; i ++) {
            if (i != pathName.length - 1) {
                builder.append(pathName[i]).append(File.separator);
            } else {
                builder.append(pathName[i]);
            }
        }
        return builder.toString();
    }

    protected static boolean removeRecursively (String targetPath){
        return FileSystemUtils.deleteRecursively(new File(targetPath));
    }

    public static String readTextFile (String filePath) throws Exception {
        String charset = getFileEncodingCharset(new File(filePath));
        StringBuffer sb = new StringBuffer();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath, Charset.forName(charset)))) {

            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
        }

        return sb.toString();
    }

    public static byte[] getFileBytes (String filePath) {
        Path path = new File(filePath).toPath();
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException("File Read Error : " ,e);
        }
    }

    public static String getMimeType (String fileName) {
        String extension = getFileExtension(fileName);
        return switch (extension) {
            case "txt" -> "text/plain";
            case "html", "htm" -> "text/html";
            case "css" -> "text/css";
            case "js" -> "application/javascript";
            case "json" -> "application/json";
            case "xml" -> "application/xml";
            case "csv" -> "text/csv";
            case "pdf" -> "application/pdf";
            case "zip" -> "application/zip";
            case "rar" -> "application/vnd.rar";
            case "7z" -> "application/x-7z-compressed";
            case "jpg","jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "ico" -> "image/x-icon";
            case "svg" -> "image/svg+xml";
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "mp4" -> "video/mp4";
            case "mov" -> "video/quicktime";
            case "avi" -> "video/x-msvideo";
            case "mkv" -> "video/x-matroska";
            case "exe", "bin" -> "application/octet-stream";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "py" -> "text/x-python";
            default -> "application/octet-stream";
        };
    }

    public static void mergeToZip ( Path srcDir, String dirId, ZipOutputStream zos ) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        final String prefix = dirId == null ? "" : DateTimeUtil.convertUuidStringToLocalDateTime(dirId).format(formatter) ;

        try {
            Files.walk(srcDir).forEach(path -> {
                try {
                    Path rel = srcDir.relativize(path);

                    String relName = rel.toString().replace("\\", "/");
                    String entryName = prefix + "/" + relName;

                    if (Files.isDirectory(path)) {
                        ZipEntry dirEntry = new ZipEntry(prefix + "/");
                        dirEntry.setTime(path.toFile().lastModified());
                        zos.putNextEntry(dirEntry);
                        zos.closeEntry();
                    } else {
                        ZipEntry fileEntry = new ZipEntry(entryName);
                        fileEntry.setTime(Files.getLastModifiedTime(path).toMillis());
                        zos.putNextEntry(fileEntry);
                        Files.copy(path, zos); // 스트리밍 복사
                        zos.closeEntry();
                    }
                } catch (IOException e) {
                    log.error("zip merged error : {} ", e.getMessage());
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            log.error("zip file error : {} ", e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public static void retryDelete (String path){
        if (path == null) {
            return; // 혹은 바로 예외
        }

        Path p = Paths.get(path);

        // 1) 애초에 없으면 그냥 성공 처리
        if (Files.notExists(p)) {
            log.info("Directory already deleted or not exists: {}", path);
            return;
        }

        int retry = 0;
        int maxRetry = 30;

        while (!FileUtil.removeFile(path)) {
            if (++retry >= maxRetry) {
                log.error("Failed to delete directory after {} attempts: {}", maxRetry, path);
                throw new RuntimeException("Failed to delete directory: " + path);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // ✅ 현재 스레드에 인터럽트 상태를 설정
                log.error("Thread interrupted while deleting directory: {}", e.getMessage());
                throw new RuntimeException("Thread interrupted while deleting directory: " + path);
            }
        }
    }

    public static long getDirectorySize (Path dirPath) {
        AtomicLong size = new AtomicLong(0);
        try {
            Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (attrs.isRegularFile()) {
                        size.addAndGet(attrs.size()); // 파일 크기 누적
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    // 권한/락 등으로 읽기 실패 시 건너뜀
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error("file size get error : {} ", e.getMessage());
            throw new RuntimeException(e);
        }
        return size.get();
    }

    public static String getTargetRootDriveName (String targetPath) {
        List<File> drives = List.of(File.listRoots());
        String resultDrive = Paths.get(targetPath).toAbsolutePath().getRoot().toString();
        if (drives.stream().anyMatch(d -> d.getAbsolutePath().equals(resultDrive))) {
            return resultDrive.replace(File.separator, "");
        } else {
            log.error("target path is not valid : {}", targetPath);
            throw new RuntimeException("target path is not valid : " + targetPath);
        }
    }

    public static Map<String, String> getMatchedFileExtension (List<MultipartFile> files, List<String> extensions) {
        return files.stream()
                .map(MultipartFile::getOriginalFilename)
                .filter(Objects::nonNull)
                .filter(fn -> extensions.stream().anyMatch(ext -> fn.toLowerCase().endsWith(ext)))
                .collect(Collectors.toMap(
                        fn -> !FileUtil.getFileExtension(fn).toLowerCase().isEmpty() ? "." + FileUtil.getFileExtension(fn).toLowerCase() : "",
                        fn -> FileUtil.getFileNameWithoutExtension(fn),
                        (prev, curr) -> prev // 중복 확장자 들어오면 무시
                ));
    }

    public static Set<String> extractValidExtensions (List<MultipartFile> files, FileExtension fileXtns) {

        if (files == null || files.isEmpty()) return Set.of();

        return files.stream()
                .map(f -> FileUtil.getFileExtension(f.getOriginalFilename()))
                .filter(ext -> fileXtns.getValidExtensions().contains(ext.isEmpty() ? "" : "." + ext.toLowerCase()))
                .collect(Collectors.toSet());
    }


    public static String convertFilePathToUrlPath (String fullPath, String basePath) {
        Path full = Paths.get(fullPath);
        Path base = Paths.get(basePath);

        if (!full.startsWith(base)) {
            throw new RuntimeException("File path is not valid : " + fullPath);
        }

        Path relative = base.relativize(full);

        StringBuilder sb = new StringBuilder();
        sb.append("/");

        for (int i = 0; i < relative.getNameCount(); i++) {
            if ( i > 0 ) sb.append("/");
            String seg = relative.getName(i).toString();
            sb.append(URLEncoder.encode(seg, StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
