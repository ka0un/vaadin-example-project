package com.example.notes.data.dto;

import com.example.notes.data.entity.Image;
import org.springframework.core.io.Resource;

public class ImageDto {

    private Image image;
    private Resource imageResource;

    public ImageDto(Image image, Resource imageResource) {
        this.image = image;
        this.imageResource = imageResource;
    }
    public Image getImage() {
        return image;
    }
    public Resource getImageResource() {
        return imageResource;
    }
}
