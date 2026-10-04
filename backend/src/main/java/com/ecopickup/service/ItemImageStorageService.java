package com.ecopickup.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Service
public class ItemImageStorageService {
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final Map<String,String> EXTENSIONS = Map.of(
            "image/jpeg",".jpg",
            "image/png",".png",
            "image/webp",".webp"
    );
    private final Path storageDirectory;

    public ItemImageStorageService(@Value("${ecopickup.upload-dir:./uploads/items}") String uploadDirectory){
        this.storageDirectory=Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    @PostConstruct
    void initialize(){
        try{Files.createDirectories(storageDirectory);}catch(IOException ex){throw new IllegalStateException("Could not create the item image directory",ex);}
    }

    public String store(MultipartFile image){
        String extension=EXTENSIONS.get(image.getContentType());
        if(extension==null)throw new IllegalArgumentException("Item photo must be a JPG, PNG, or WebP image");
        if(image.getSize()>MAX_FILE_SIZE)throw new IllegalArgumentException("Item photo must be 5 MB or smaller");
        String filename=UUID.randomUUID()+extension;
        Path destination=storageDirectory.resolve(filename).normalize();
        if(!destination.startsWith(storageDirectory))throw new IllegalArgumentException("Invalid image filename");
        try{
            byte[] data=image.getBytes();
            if(!hasExpectedSignature(image.getContentType(),data))throw new IllegalArgumentException("The uploaded file is not a valid image");
            Files.write(destination,data);
            return "/uploads/items/"+filename;
        }catch(IOException ex){throw new IllegalStateException("Could not save the item photo");}
    }

    public Resource load(String filename){
        if(filename==null||filename.contains("/")||filename.contains("\\"))throw new IllegalArgumentException("Invalid image filename");
        Path file=storageDirectory.resolve(filename).normalize();
        if(!file.startsWith(storageDirectory)||!Files.isRegularFile(file))throw new IllegalArgumentException("Item photo not found");
        try{return new UrlResource(file.toUri());}catch(MalformedURLException ex){throw new IllegalArgumentException("Item photo not found");}
    }

    private boolean hasExpectedSignature(String contentType,byte[] data){
        if("image/jpeg".equals(contentType))return data.length>=3&&(data[0]&255)==0xff&&(data[1]&255)==0xd8&&(data[2]&255)==0xff;
        if("image/png".equals(contentType))return data.length>=8&&(data[0]&255)==0x89&&data[1]==0x50&&data[2]==0x4e&&data[3]==0x47&&data[4]==0x0d&&data[5]==0x0a&&data[6]==0x1a&&data[7]==0x0a;
        if("image/webp".equals(contentType))return data.length>=12&&data[0]=='R'&&data[1]=='I'&&data[2]=='F'&&data[3]=='F'&&data[8]=='W'&&data[9]=='E'&&data[10]=='B'&&data[11]=='P';
        return false;
    }
}
