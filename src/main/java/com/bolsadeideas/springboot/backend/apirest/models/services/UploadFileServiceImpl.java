package com.bolsadeideas.springboot.backend.apirest.models.services;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UploadFileServiceImpl implements IUploadFileService{

    public static final String DIRECTORIO_UPLOADS = "uploads";

    @Override
    public Resource load(String profileImgName) throws MalformedURLException {

        Path pathProfileImg = getPath(profileImgName);
        Resource resource = resource = new UrlResource(pathProfileImg.toUri());
        if (!resource.exists() || !resource.isReadable()){
            pathProfileImg = Paths.get("src/main/resources/static").resolve("not-user.png").toAbsolutePath();
            resource = new UrlResource(pathProfileImg.toUri());
        }
        return resource;

    }

    @Override
    public String copy(MultipartFile profileImg) throws IOException {

        String profileImgName = UUID.randomUUID().toString() + "_" + profileImg.getOriginalFilename().replace(" ", "");
        Path profileImgPath = getPath(profileImgName);
        Files.copy(profileImg.getInputStream(), profileImgPath);
        return profileImgName;

    }

    @Override
    public boolean delete(String profileImgName) {

        if (null != profileImgName && !profileImgName.isEmpty()){
            Path pathProfileImgLast = Paths.get("uploads").resolve(profileImgName).toAbsolutePath();
            File fileProfileImgLast = pathProfileImgLast.toFile();
            if (fileProfileImgLast.exists() && fileProfileImgLast.canRead()){
                return fileProfileImgLast.delete();
            }
        }
        return false;

    }

    @Override
    public Path getPath(String profileImgName) {

        return Paths.get(DIRECTORIO_UPLOADS).resolve(profileImgName).toAbsolutePath();

    }

}