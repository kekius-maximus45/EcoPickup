package com.ecopickup.controller;

import com.ecopickup.service.ItemImageStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class ItemImageController {
    private final ItemImageStorageService storage;
    public ItemImageController(ItemImageStorageService storage){this.storage=storage;}

    @GetMapping("/uploads/items/{filename:.+}")
    public ResponseEntity<Resource> image(@PathVariable String filename){
        Resource image=storage.load(filename);
        String extension=filename.substring(filename.lastIndexOf('.')+1).toLowerCase();
        MediaType type=switch(extension){case "png"->MediaType.IMAGE_PNG;case "webp"->MediaType.parseMediaType("image/webp");default->MediaType.IMAGE_JPEG;};
        return ResponseEntity.ok().contentType(type).cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS)).body(image);
    }
}
