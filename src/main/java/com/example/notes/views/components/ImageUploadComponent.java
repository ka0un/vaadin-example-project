package com.example.notes.views.components;

import com.example.notes.data.entity.User;
import com.example.notes.service.ImageService;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;

import java.io.IOException;
import java.io.InputStream;

public class ImageUploadComponent extends Composite<Div> {

    public ImageUploadComponent(ImageService imageService, User user, Runnable onSuccess) {

        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        
        // Only allow images
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif");
        
        // Limit file size (e.g., 5MB)
        int maxFileSize = 5 * 1024 * 1024;
        upload.setMaxFileSize(maxFileSize);

        upload.addSucceededListener(event -> {
            try {
                String fileName = event.getFileName();
                String contentType = event.getMIMEType();
                InputStream inputStream = buffer.getInputStream(fileName);
                
                imageService.saveImage(fileName, contentType, inputStream, user);
                
                Notification.show("Image '" + fileName + "' uploaded successfully!")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                
                if (onSuccess != null) {
                    onSuccess.run();
                }
            } catch (IOException e) {
                Notification.show("Error saving image: " + e.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        upload.addFileRejectedListener(event -> {
            Notification.show("File rejected: " + event.getErrorMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        upload.addAllFinishedListener(event -> {
            upload.clearFileList();
        });

        getContent().add(upload);
    }
}
