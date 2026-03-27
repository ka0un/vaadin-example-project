package com.example.notes.views.components;

import com.example.notes.data.dto.ImageThumbnailDto;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;

public class ImageCard extends Div{

    private ImageThumbnailDto img;
    private Runnable openImageDialog;

    public ImageCard(ImageThumbnailDto img, Runnable openImageDialog) {
        this.img = img;
        this.openImageDialog = openImageDialog;

        buildUI();
    }

    private void buildUI() {

        this.setWidthFull();
        this.setHeight("350px");

        this.getStyle()
                .set("overflow", "hidden")
                .set("border-radius", "12px")
                .set("cursor", "pointer");

        com.vaadin.flow.component.html.Image image = getImage(img);

        image.getStyle()
                .set("object-fit", "cover")
                .set("transition", "transform 0.3s ease");

        this.getElement().addEventListener("mouseover",
                e -> image.getStyle().set("transform", "scale(1.08)"));

        this.getElement().addEventListener("mouseout",
                e -> image.getStyle().set("transform", "scale(1)"));

        this.addClickListener(e -> openImageDialog.run());

        this.add(image);
    }

    private com.vaadin.flow.component.html.@NonNull Image getImage(ImageThumbnailDto img) {

        if(img.getResource() == null) {
            com.vaadin.flow.component.html.Image image = new com.vaadin.flow.component.html.Image("/Image-not-found.png", "No Image Found");
            image.setWidthFull();
            image.setHeightFull();
            return image;
        }

        StreamResource streamResource = new StreamResource(String.valueOf(img.getId()), () -> {
            try {
                return img.getResource().getInputStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        com.vaadin.flow.component.html.Image image = new com.vaadin.flow.component.html.Image(streamResource, "Thumbnail " + img.getId());

        image.setWidthFull();
        image.setHeightFull();
        return image;
    }

}
