package com.example.notes.views.components;

import com.example.notes.data.entity.ImageRecord;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;

public class ImageCard extends VerticalLayout {

    public ImageCard(ImageRecord record) {
        // Convert the database byte array back into a visual image stream
        StreamResource resource = new StreamResource(record.getFileName(),
                () -> new ByteArrayInputStream(record.getData()));

        Image image = new Image(resource, record.getFileName());
        image.setWidth("100%");
        image.setHeight("200px");

        // CSS properties to make the image fit nicely without stretching
        image.getStyle().set("object-fit", "cover");
        image.getStyle().set("border-radius", "8px");

        Span name = new Span(record.getFileName());
        name.getStyle().set("font-size", "14px");
        name.getStyle().set("font-weight", "500");

        add(image, name);

        // Styling the overall card frame
        getStyle().set("border", "1px solid #e0e0e0");
        getStyle().set("border-radius", "12px");
        getStyle().set("padding", "8px");
        getStyle().set("box-shadow", "0 4px 6px rgba(0,0,0,0.1)");
        setAlignItems(Alignment.CENTER);
    }
}