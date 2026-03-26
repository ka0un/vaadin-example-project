package com.example.notes.views.components;

import com.example.notes.data.entity.UserImage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;
import java.util.Set;
import java.util.function.Consumer;

public class ImageCardComponent extends Div {

    public ImageCardComponent(UserImage userImage, Set<Long> selectedIds, Consumer<Boolean> onToggle) {
        addClassName("image-card");

        StreamResource resource = new StreamResource(
                userImage.getFileName(),
                () -> new ByteArrayInputStream(userImage.getData())
        );

        Image image = new Image(resource, userImage.getFileName());
        image.addClassName("image-card-img");

        // toggle selection by id — no equals/hashCode dependency
        image.addClickListener(event -> {
            Long id = userImage.getId();
            if (selectedIds.contains(id)) {
                selectedIds.remove(id);
                removeClassName("image-card--selected");
                onToggle.accept(false);
            } else {
                selectedIds.add(id);
                addClassName("image-card--selected");
                onToggle.accept(true);
            }
        });

        add(image);
    }
}