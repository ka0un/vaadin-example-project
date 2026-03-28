package com.example.notes.views;

import com.example.notes.service.ImageInfo;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ImageCard extends VerticalLayout {

    public ImageCard(Image image, ImageInfo imageInfo, Runnable onDelete) {
        setSpacing(false);
        setPadding(true);
        setWidth("280px");
        setHeight("380px");

        // Push action buttons toward the bottom so all cards stay visually balanced
        setJustifyContentMode(JustifyContentMode.BETWEEN);

        // Card container styling
        getStyle()
                .set("border", "1px solid #ddd")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.08)")
                .set("background", "white")
                .set("max-width", "100%")
                .set("transition", "transform 0.2s ease, box-shadow 0.2s ease");

        // Add a small hover lift effect for better UI feedback
        getElement().executeJs(
                "this.addEventListener('mouseenter', () => this.style.transform='translateY(-5px)');" +
                        "this.addEventListener('mouseleave', () => this.style.transform='translateY(0px)');"
        );

        // Keep all preview images at a consistent size inside the card
        image.setWidth("100%");
        image.setHeight("180px");
        image.getStyle()
                .set("border-radius", "8px")
                .set("object-fit", "cover");

        // Display original user-friendly file name
        Paragraph name = new Paragraph(imageInfo.getDisplayName());
        name.getStyle()
                .set("word-break", "break-word")
                .set("margin", "10px 0 6px 0")
                .set("font-weight", "600")
                .set("min-height", "48px");

        // Show image metadata such as size and upload time
        Span meta = new Span("Size: " + imageInfo.getSizeKb() + " KB | Uploaded: " + imageInfo.getUploadedAt());
        meta.getStyle()
                .set("font-size", "12px")
                .set("color", "#666")
                .set("margin-bottom", "10px")
                .set("display", "block")
                .set("line-height", "1.4")
                .set("word-break", "break-word");

        Button viewButton = new Button("View");

        // Open a larger preview dialog when user clicks View
        viewButton.addClickListener(event -> openPreviewDialog(image, imageInfo.getDisplayName()));

        Button deleteButton = new Button("Delete");

        // Show confirmation dialog before deleting to avoid accidental removal
        deleteButton.addClickListener(event -> {
            Dialog confirmDialog = new Dialog();
            confirmDialog.setHeaderTitle("Confirm Delete");
            confirmDialog.add(new Paragraph("Are you sure you want to delete this image?"));

            Button yesButton = new Button("Yes", e -> {
                onDelete.run();
                confirmDialog.close();
                Notification.show("Image deleted");
            });

            Button cancelButton = new Button("Cancel", e -> confirmDialog.close());

            HorizontalLayout confirmActions = new HorizontalLayout(yesButton, cancelButton);
            confirmActions.setSpacing(true);

            confirmDialog.add(confirmActions);
            confirmDialog.open();
        });

        // Action buttons row
        HorizontalLayout actions = new HorizontalLayout(viewButton, deleteButton);
        actions.setSpacing(true);

        add(image, name, meta, actions);
    }

    private void openPreviewDialog(Image originalImage, String displayName) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(displayName);

        // Reuse the same image source but show it in a much larger preview
        Image previewImage = new Image(originalImage.getSrc(), displayName);
        previewImage.getStyle()
                .set("max-width", "90vw")
                .set("max-height", "80vh")
                .set("object-fit", "contain");

        dialog.add(previewImage);

        Button closeButton = new Button("Close", event -> dialog.close());
        dialog.getFooter().add(closeButton);

        dialog.open();
    }
}