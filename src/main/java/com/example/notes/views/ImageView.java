package com.example.notes.views;

import com.example.notes.data.entity.User;
import com.example.notes.data.entity.ImageEntity;
import com.example.notes.data.repository.ImageRepository;
import com.example.notes.data.repository.UserRepository;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

import jakarta.annotation.security.PermitAll;

import org.springframework.security.core.context.SecurityContextHolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Route(value = "images", layout = MainLayout.class)
@PermitAll
public class ImageView extends VerticalLayout {

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    private Div gallery = new Div();


    public ImageView(ImageRepository imageRepository, UserRepository userRepository) {
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;

        createUploadSection();
        add(gallery);
        loadImages();
    }

    private void createUploadSection() {
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);


        upload.setAcceptedFileTypes("image/*");

        upload.addSucceededListener(event -> {
            try {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                buffer.getInputStream().transferTo(output);

                String username = SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName();

                if (username == null || username.equals("anonymousUser")) {
                    return;
                }

                User user = userRepository.findByUsername(username)
                        .orElseThrow();

                ImageEntity imageEntity = new ImageEntity();
                imageEntity.setName(event.getFileName());
                imageEntity.setData(output.toByteArray());
                imageEntity.setUser(user);

                imageRepository.save(imageEntity);

                loadImages();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        add(upload);
    }

    private void loadImages() {
        gallery.removeAll();

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        if (username == null || username.equals("anonymousUser")) {
            return;
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow();

        List<ImageEntity> images = imageRepository.findByUser(user);

        for (ImageEntity img : images) {

            StreamResource resource = new StreamResource(
                    img.getName(),
                    () -> new ByteArrayInputStream(img.getData())
            );

            Image image = new Image(resource, img.getName());
            image.setWidth("100%");
            image.getStyle()
                    .set("height", "150px")
                    .set("object-fit", "cover")
                    .set("border-radius", "8px");

            Button delete = new Button("Delete", e -> {
                imageRepository.delete(img);
                loadImages();
            });

            VerticalLayout card = new VerticalLayout(image, delete);
            card.getStyle()
                    .set("border-radius", "12px")
                    .set("box-shadow", "0 4px 10px rgba(0,0,0,0.1)")
                    .set("padding", "10px")
                    .set("background", "white");

            gallery.add(card);
        }
    }
}