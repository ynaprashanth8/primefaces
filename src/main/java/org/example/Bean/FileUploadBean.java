package org.example.Bean;

import lombok.Getter;
import lombok.Setter;
import org.example.service.S3Service;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named
@ViewScoped
@Component
public class FileUploadBean implements Serializable {

    @Autowired
    private S3Service s3Service;

    private final RestTemplate restTemplate = new RestTemplate();

    @Getter
    @Setter
    private UploadedFile file;

    public void upload() {
        if (file != null) {
            try {
                // Here you can process the file
                FacesMessage message = new FacesMessage("Success", file.getFileName() + " is uploaded.");
                FacesContext.getCurrentInstance().addMessage(null, message);
            } catch (Exception e) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "File upload failed: " + e.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, message);
            }
        }
    }

    public void uploadS3() {
        if (file != null) {
            try {
                // Generate pre-signed URL
                String presignedUrl = s3Service.generatePresignedUrl(file.getFileName());

                // Prepare headers for S3 upload
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentLength(file.getContent().length);

                // Create request entity with file content and headers
                HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getContent(), headers);

                // Upload to S3 using pre-signed URL
                restTemplate.exchange(presignedUrl, HttpMethod.PUT, requestEntity, Void.class);

                // Success message
                String message = String.format("File %s uploaded successfully to S3", file.getFileName());
                FacesMessage fmsg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", message);
                FacesContext.getCurrentInstance().addMessage(null, fmsg);

            } catch (Exception e) {
                String message = String.format("Failed to upload %s: %s",
                        file.getFileName(), e.getMessage());
                FacesMessage fmsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message);
                FacesContext.getCurrentInstance().addMessage(null, fmsg);
            }
        }
    }
}
