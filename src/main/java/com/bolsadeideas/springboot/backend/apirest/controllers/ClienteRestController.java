package com.bolsadeideas.springboot.backend.apirest.controllers;

import com.bolsadeideas.springboot.backend.apirest.model.entity.Cliente;
import com.bolsadeideas.springboot.backend.apirest.model.entity.Region;
import com.bolsadeideas.springboot.backend.apirest.models.services.IClienteService;
import com.bolsadeideas.springboot.backend.apirest.models.services.IUploadFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:4200"})
@RestController
@RequestMapping("/api")
public class ClienteRestController {

    private static final Integer PAGE_SIZE = 4;

    @Autowired
    private IClienteService clienteService;

    @Autowired
    private IUploadFileService uploadFileService;

    @GetMapping("/clientes")
    public List<Cliente> index() {
        return clienteService.findAll();
    }

    @GetMapping("/clientes/page/{page}")
    public Page<Cliente> index(@PathVariable Integer page) {
        return clienteService.findAll(PageRequest.of(page, PAGE_SIZE));
    }

    @GetMapping("/clientes/{id}")
    public ResponseEntity<?> show(@PathVariable Long id) {
        Cliente cliente = null;
        Map<String, Object> response = new HashMap<>();
        HttpStatus httpStatus = null;
        try {
            cliente = clienteService.findById(id);
            if (null == cliente) {
                response.put("mensaje", "El cliente no existe.");
                httpStatus = HttpStatus.NOT_FOUND;
            } else {
                return new ResponseEntity<Cliente>(cliente, HttpStatus.OK);
            }
        } catch (DataAccessException e) {
            response.put("mensaje", "Error interno inesperado.");
            response.put("error", e.getMessage() + ": " + e.getMostSpecificCause().getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new ResponseEntity<Map<String, Object>>(response, httpStatus);
    }

    @PostMapping("/clientes")
    public ResponseEntity<?> create(@Valid @RequestBody Cliente cliente, BindingResult result) {
        Cliente clienteNuevo = null;
        Map<String, Object> response = new HashMap<>();
        HttpStatus httpStatus = null;

        if (result.hasErrors()) {

            List<String> errors = result.getFieldErrors().stream().map(err -> "El campo '" + err.getField() + "' " + err.getDefaultMessage()).collect(Collectors.toList());
            response.put("mensaje", "Error de validacion.");
            response.put("validateError", errors);
            httpStatus = HttpStatus.BAD_REQUEST;

        } else {

            try {
                clienteNuevo = clienteService.save(cliente);
                response.put("mensaje", "El cliente ha sido creado con existo!");
                response.put("cliente", clienteNuevo);
                httpStatus = HttpStatus.CREATED;
            } catch (DataAccessException e) {
                response.put("mensaje", "Error interno inesperado.");
                response.put("error", e.getMessage() + ": " + e.getMostSpecificCause().getMessage());
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            }

        }
        return new ResponseEntity<Map<String, Object>>(response, httpStatus);
    }

    @PutMapping("/clientes/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody Cliente cliente, BindingResult result, @PathVariable Long id) {
        Cliente clienteUpdate = null;
        Map<String, Object> response = new HashMap<>();
        HttpStatus httpStatus = null;


        if (result.hasErrors()) {

            List<String> errors = result.getFieldErrors().stream().map(err -> "El campo '" + err.getField() + "' " + err.getDefaultMessage()).collect(Collectors.toList());
            response.put("mensaje", "Error de validacion.");
            response.put("validateError", errors);
            httpStatus = HttpStatus.BAD_REQUEST;

        } else {

            try {
                clienteUpdate = clienteService.findById(id);

                if (null == clienteUpdate) {
                    response.put("mensaje", "El cliente no existe.");
                    httpStatus = HttpStatus.NOT_FOUND;
                } else {
                    clienteUpdate.setNombre(cliente.getNombre());
                    clienteUpdate.setApellido(cliente.getApellido());
                    clienteUpdate.setEmail(cliente.getEmail());
                    clienteUpdate.setCreateAt(new Date());
                    clienteUpdate.setRegion(cliente.getRegion());
                    clienteUpdate = clienteService.save(clienteUpdate);

                    if (null == clienteUpdate) {
                        response.put("mensaje", "El cliente no actualizado.");
                        httpStatus = HttpStatus.NOT_FOUND;
                    } else {
                        response.put("mensaje", "El cliente ha sido actualizado con existo!");
                        response.put("cliente", clienteUpdate);
                        httpStatus = HttpStatus.CREATED;
                    }
                }

            } catch (DataAccessException e) {
                response.put("mensaje", "Error interno inesperado.");
                response.put("error", e.getMessage() + ": " + e.getMostSpecificCause().getMessage());
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            }

        }

        return new ResponseEntity<Map<String, Object>>(response, httpStatus);

    }

    @DeleteMapping("/clientes/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        Map<String, Object> response = new HashMap<>();
        HttpStatus httpStatus = null;
        try {
            Cliente cliente = clienteService.findById(id);
            uploadFileService.delete(cliente.getProfileImg());
            clienteService.delete(id);
            response.put("mensaje", "Cliente eliminado con exito.");
            httpStatus = HttpStatus.OK;
        } catch (DataAccessException e) {
            response.put("mensaje", "Error interno inesperado.");
            response.put("error", e.getMessage() + ": " + e.getMostSpecificCause().getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new ResponseEntity<Map<String, Object>>(response, httpStatus);

    }

    @PostMapping("clientes/upload")
    public ResponseEntity<?> upload(@RequestParam("profileImg") MultipartFile profileImg, @RequestParam("id") Long id) {

        Map<String, Object> response = new HashMap<>();
        HttpStatus httpStatus = null;
        String profileImgName = null;

        try {

            if (!profileImg.isEmpty()) {
                profileImgName = uploadFileService.copy(profileImg);
                uploadFileService.delete(profileImgName);
                Cliente cliente = clienteService.findById(id);
                cliente.setProfileImg(cliente.getProfileImg());
                cliente.setProfileImg(profileImgName);
                clienteService.save(cliente);
                response.put("cliente", cliente);
                response.put("mensaje", "Has subido correctamente la imagen: " + profileImgName);
                httpStatus = HttpStatus.OK;
            } else {
                response.put("mensaje", "No se ha podido subir la imagen, intentelo de nuevo más tarde!");
                httpStatus = HttpStatus.BAD_REQUEST;
            }

        } catch (DataAccessException dataEx) {
            response.put("mensaje", "Error interno inesperado.");
            response.put("error", dataEx.getMessage() + ": " + dataEx.getMostSpecificCause().getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (IOException ioException) {
            response.put("mensaje", "Error interno inesperado.");
            response.put("error", ioException.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<Map<String, Object>>(response, httpStatus);
    }

    @GetMapping("/uploads/img/{profileImgName:.+}")
    public ResponseEntity<Resource> getProfileImg(@PathVariable String profileImgName) {
        Resource resource = null;

        try {
            resource = uploadFileService.load(profileImgName);
        } catch (MalformedURLException malformedURLE) {
            malformedURLE.printStackTrace();
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"");
        return new ResponseEntity<Resource>(resource, httpHeaders, HttpStatus.OK);
    }

    @GetMapping("/clientes/regiones")
    public List<Region> listarRegiones() {
        return clienteService.findAllRegiones();
    }

}