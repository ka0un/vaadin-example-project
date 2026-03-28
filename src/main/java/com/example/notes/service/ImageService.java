package com.example.notes.service;

import com.example.notes.data.entity.ImageRecord;
import com.example.notes.data.repository.ImageRecordRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ImageService {

    private final ImageRecordRepository repository;

    public ImageService(ImageRecordRepository repository) {
        this.repository = repository;
    }

    public void saveImage(String fileName, String mimeType, byte[] data) {
        ImageRecord image = new ImageRecord();
        image.setFileName(fileName);
        image.setMimeType(mimeType);
        image.setData(data);
        repository.save(image);
    }

    public List<ImageRecord> getAllImages() {
        return repository.findAll(); // Fetches all images for the gallery
    }
}