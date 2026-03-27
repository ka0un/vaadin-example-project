package com.example.notes.views.components;

import com.example.notes.data.dto.ImageDto;
import com.example.notes.data.dto.ImageThumbnailDto;
import com.example.notes.data.entity.Image;
import com.example.notes.data.entity.User;
import com.example.notes.service.ImageService;
import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;
import jakarta.persistence.EntityNotFoundException;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;

public class ImageDialog extends Dialog {

    private final List<ImageThumbnailDto> images;
    private int currentIndex;
    private final ImageService imageService;
    private final User currentUser;
    private final Runnable refreshGallery;

    public ImageDialog(List<ImageThumbnailDto> images,
                       int startIndex,
                       ImageService imageService,
                       User currentUser,
                       Runnable refreshGallery) {

        this.images = images;
        this.currentIndex = startIndex;
        this.imageService = imageService;
        this.currentUser = currentUser;
        this.refreshGallery = refreshGallery;

        buildUI();
    }

    private void buildUI() {
        removeAll(); // important when navigating

        if (currentIndex < 0 || currentIndex >= images.size()) return;

        ImageThumbnailDto imageThumbnailDto = images.get(currentIndex);
        Long imageId = imageThumbnailDto.getId();

        ImageDto imageDto = imageService.getImageForUser(imageId);
        Image imageEntity = imageDto.getImage();

        Div mainDiv = new Div();
        mainDiv.addClassName("responsive-main");

        Div divA = new Div();

        divA.add(addImage(imageDto));

        Div divB = new Div();

        divB.getStyle()
                .setBackgroundColor("white")
                .set("max-width", "300px");

        VerticalLayout metaAndActionsLayout = new VerticalLayout(addMetaData(imageEntity), addActionButtons(imageEntity.getId()));

        divB.add(metaAndActionsLayout);

        mainDiv.add(divA, divB);

        VerticalLayout wrapper = new VerticalLayout(addNavButtons(), mainDiv);
        wrapper.setPadding(false);
        wrapper.setSpacing(false);
        wrapper.setWidthFull();

        this.add(wrapper);

        this.open();
    }

    private Component createMetaRow(String labelText, String valueText) {

        Span label = new Span(labelText);
        label.getStyle()
                .set("font-size", "12px")
                .set("color", "#777")
                .set("font-family", "Helvetica, Arial, sans-serif");

        Span value = new Span(valueText);
        value.getStyle()
                .set("font-size", "14px")
                .set("font-weight", "600")
                .set("font-family", "Helvetica, Arial, sans-serif");

        VerticalLayout row = new VerticalLayout(label, value);
        row.setSpacing(false);
        row.setPadding(false);

        return row;
    }

    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return (size / 1024) + " KB";
        return String.format("%.2f MB", size / (1024.0 * 1024));
    }

    private HorizontalLayout addNavButtons(){

        Button leftBtn = new Button(FontAwesome.Solid.ARROW_LEFT.create());
        leftBtn.addClassName("arrow-btn");
        Button rightBtn = new Button(FontAwesome.Solid.ARROW_RIGHT.create());
        rightBtn.addClassName("arrow-btn");

        leftBtn.addClickListener(e -> {
            currentIndex--;
            buildUI();
        });
        rightBtn.addClickListener(e -> {
            currentIndex++;
            buildUI();
        });

        HorizontalLayout navBar = new HorizontalLayout();
        navBar.setWidthFull();

        Div spacer = new Div();
        spacer.getStyle().set("flex-grow", "1");

        boolean hasPrevious = currentIndex > 0;
        boolean hasNext = currentIndex < images.size() - 1;

        if (hasPrevious) {
            leftBtn.addClickShortcut(Key.ARROW_LEFT);
            navBar.add(leftBtn);
        }

        navBar.add(spacer); // always present

        if (hasNext) {
            rightBtn.addClickShortcut(Key.ARROW_RIGHT);
            navBar.add(rightBtn);
        }
        navBar.getStyle()
                .set("border-bottom", "1px solid rgba(0,0,0,0.1)")
                .set("padding-bottom", "8px")
                .set("margin-bottom", "10px");

        return navBar;
    }

    private com.vaadin.flow.component.html.Image addImage(ImageDto imageDto){

        com.vaadin.flow.component.html.Image image;

        if(imageDto.getImageResource() == null) {

            image = new com.vaadin.flow.component.html.Image("/Image-not-found.png", "No Image Found");

        }else {

            Long imageId = imageDto.getImage().getId();

            StreamResource streamResource = new StreamResource(String.valueOf(imageId), () -> {
                try {
                    return imageDto.getImageResource().getInputStream();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            image = new com.vaadin.flow.component.html.Image(streamResource, String.valueOf(imageId));

        }

        image.getStyle()
                .set("max-height", "500px")
                .set("width", "auto")
                .setMaxWidth("400px")
                .set("height", "auto")
                .set("display", "block");

        return  image;
    }

    private VerticalLayout addMetaData(Image imageEntity){
        VerticalLayout meta = new VerticalLayout(
                createMetaRow("Name", imageEntity.getFileName()),
                createMetaRow("Size", formatFileSize(imageEntity.getFileSize())),
                createMetaRow("Resolution", imageEntity.getWidth() + "x" + imageEntity.getHeight()),
                createMetaRow("Format", imageEntity.getFormat().toUpperCase())
        );
        meta.setPadding(false);

        return meta;
    }

    private HorizontalLayout addActionButtons(Long imageEntityId){
        Button cropBtn = new Button("Crop");
        Button deleteBtn = new Button("Delete");

        deleteBtn.getStyle().set("color", "red");
        deleteBtn.addClickListener(e -> {
            try {
                imageService.deleteImage(imageEntityId, currentUser);

                this.close();
                refreshGallery.run();

                Notification.show("Image deleted successfully", 2000, Notification.Position.BOTTOM_START);

            } catch (AccessDeniedException ex) {
                Notification.show("You are not allowed to delete this image.");

            } catch (EntityNotFoundException ex) {
                Notification.show("Image not found.");

            } catch (IOException ex) {
                Notification.show("Failed to delete image file.");

            } catch (Exception ex) {
                Notification.show("Unexpected error occurred.");
            }
        });

        HorizontalLayout actions = new HorizontalLayout(cropBtn, deleteBtn);
        actions.getStyle()
                .setWidth("100%")
                .set("border-top", "1px solid rgba(0,0,0,0.3)")
                .set("padding-top", "10px")
                .set("margin-top", "10px");

        return actions;
    }

}
