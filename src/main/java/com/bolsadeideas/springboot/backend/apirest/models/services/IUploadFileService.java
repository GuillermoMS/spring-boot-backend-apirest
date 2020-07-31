package com.bolsadeideas.springboot.backend.apirest.models.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;

public interface IUploadFileService {

    public Resource load(String profileImgName) throws MalformedURLException;
    public String copy(MultipartFile profileImg) throws IOException;
    public boolean delete(String profileImgName);
    public Path getPath(String profileImgName);

}
