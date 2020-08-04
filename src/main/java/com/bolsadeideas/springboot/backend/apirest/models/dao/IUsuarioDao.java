package com.bolsadeideas.springboot.backend.apirest.models.dao;

import com.bolsadeideas.springboot.backend.apirest.model.entity.Usuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface IUsuarioDao extends CrudRepository<Usuario, Long> {

    // el nombre del metodo determina la query find = select, By = where, Username = colunma del where
    Usuario findByUsername(String username);

    // Utilzando anotacion @Query
    @Query("select u from Usuario u where u.username=?1")
    Usuario findByUsernameQuery(String username);

}