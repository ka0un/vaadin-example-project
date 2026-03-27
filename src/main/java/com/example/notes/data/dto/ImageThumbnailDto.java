package com.example.notes.data.dto;

import org.springframework.core.io.Resource;

public class ImageThumbnailDto {

    private final Long id;
    private final Resource resource;

    public ImageThumbnailDto(Long id, Resource resource) {
        this.id = id;
        this.resource = resource;
    }

    public Long getId() {
        return id;
    }

    public Resource getResource() {
        return resource;
    }
}
