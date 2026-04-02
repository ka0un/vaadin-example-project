package com.example.notes.views.components;

import com.example.notes.service.ImageService;
import com.flowingcode.vaadin.addons.imagecrop.ImageCrop;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

public class ImageCropComponent extends Dialog {

    private final ImageService imageService;
    private final Resource imageResource;
    private final Long imageId;

    private Div croppedResultDiv = new Div();

    public ImageCropComponent(Long imageId, Resource imageResource, ImageService imageService) {

        this.imageService = imageService;
        this.imageResource = imageResource;
        this.imageId = imageId;

        buildUI();
    }

    private void buildUI() {

        VerticalLayout layout = new VerticalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        if (imageResource == null) {
            layout.add(new Span("Image source is unavailable, so cropping cannot continue."));
            this.add(layout);
            return;
        }

        layout.add(new Span("Select a portion of the picture to crop: "));

        byte[] imageBytes;
        try (var inputStream = imageResource.getInputStream()) {
            imageBytes = inputStream.readAllBytes();
        } catch (IOException e) {
            Notification.show("Failed to load image for cropping.");
            close();
            return;
        }

        StreamResource resource = new StreamResource(
                "image.png",
                () -> new ByteArrayInputStream(imageBytes));

        String url = VaadinSession.getCurrent()
                .getResourceRegistry()
                .registerResource(resource)
                .getResourceUri()
                .toString();

        ImageCrop imageCrop = new ImageCrop(url);
        imageCrop.addClassName("my-cropper");
        layout.add(imageCrop);

        Button getCropButton = new Button("Get Cropped Image");

        croppedResultDiv.setId("result-cropped-image-div");
        croppedResultDiv.setMinHeight("250px");

        getCropButton.addClickListener(e -> {
            croppedResultDiv.removeAll();
            croppedResultDiv.add(new Image(imageCrop.getCroppedImageDataUri(), "cropped image"));
        });

        Button saveButton = getSaveButton(imageCrop);

        layout.add(getCropButton, new Span("Crop Result:"), croppedResultDiv, saveButton);

        this.add(layout);
    }

    private byte[] dataUriToBytes(String dataUri) {
        String base64 = dataUri.split(",")[1];
        return Base64.getDecoder().decode(base64);
    }

    private @NonNull Button getSaveButton(ImageCrop imageCrop) {
        Button saveButton = new Button("Save");

        saveButton.addClickListener(e -> {
            String dataUri = imageCrop.getCroppedImageDataUri();

            if (dataUri == null || !dataUri.contains(",")) {
                Notification.show("Invalid cropped image");
                return;
            }

            byte[] imageBytes = dataUriToBytes(dataUri);

            try {
                imageService.updateImage(imageId, imageBytes);
                this.close();
                Notification.show("Image updated successfully");
            } catch (Exception ex) {
                Notification.show("Error saving image");
            }
        });
        return saveButton;
    }

}
