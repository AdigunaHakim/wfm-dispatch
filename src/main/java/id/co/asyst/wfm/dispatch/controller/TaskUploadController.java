package id.co.asyst.wfm.dispatch.controller;

import id.co.asyst.wfm.dispatch.service.TaskListService;
import id.co.asyst.wfm.dispatch.util.ParsingResponse;
import id.co.asyst.wfm.dispatch.util.UploadFileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dispatch/upload")
public class TaskUploadController {

    private static final Logger logger = LoggerFactory.getLogger(TaskUploadController.class);

    @Autowired
    private TaskListService fileStorageService;

    @PostMapping("/uploadFile2003")
    public UploadFileResponse uploadFile2003(@RequestParam("file") MultipartFile file, @RequestParam("employeeGroup") String group) {
        ParsingResponse parsingResponse = fileStorageService.storeFile2003(file, group);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(parsingResponse.getFilename())
                .toUriString();

        return new UploadFileResponse(parsingResponse.getFilename(), fileDownloadUri,
                file.getContentType(), file.getSize(), parsingResponse.getTotalRecord(), parsingResponse.getSuccessRecord(), parsingResponse.getFailedRecord());
    }

    @PostMapping("/uploadFile2007")
    public UploadFileResponse uploadFile2007(@RequestParam("file") MultipartFile file, @RequestParam("employeeGroup") String group) {
        ParsingResponse parsingResponse = fileStorageService.storeFile2007(file, group);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(parsingResponse.getFilename())
                .toUriString();

        return new UploadFileResponse(parsingResponse.getFilename(), fileDownloadUri,
                file.getContentType(), file.getSize(), parsingResponse.getTotalRecord(), parsingResponse.getSuccessRecord(), parsingResponse.getFailedRecord());
    }

    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
