package com.storage_service.storage.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "File")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class File extends Auditable{
    @Id
    @Column(name = "file_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String fileId;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "file_type")
    private String fileType;
    @Column(name = "file_size")
    private String fileSize;
    @Column(name = "file_path")
    private String filePath;
    @Column(name = "visibility")
    private boolean visibility;
    @Column(name = "version")
    private String version;
    @Column(name = "owner")
    private String owner;
}
