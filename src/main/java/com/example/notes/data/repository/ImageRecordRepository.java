package com.example.notes.data.repository;

import com.example.notes.data.entity.ImageRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRecordRepository extends JpaRepository<ImageRecord, Long> {
}