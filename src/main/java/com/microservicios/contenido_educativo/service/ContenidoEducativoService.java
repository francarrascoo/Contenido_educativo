package com.microservicios.contenido_educativo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.microservicios.contenido_educativo.model.ContenidoEducativo;
import com.microservicios.contenido_educativo.model.TipoUsuario;
import com.microservicios.contenido_educativo.model.UsuarioDTO;
import com.microservicios.contenido_educativo.repository.ContenidoEducativoRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ContenidoEducativoService {
    
    @Autowired
    private ContenidoEducativoRepository contenidoEducativoRepository;

    @Autowired
    private RestTemplate restTemplate;

    public List<ContenidoEducativo> listarContenidos() {
        return contenidoEducativoRepository.findAll();
    }

    public Optional<ContenidoEducativo> buscarContenidoPorId(int id) {
        return contenidoEducativoRepository.findById(id);
    }

    public ContenidoEducativo crearContenido(ContenidoEducativo nuevoContenido) {
        String url_usuarios = "http://localhost:8081/api/usuarios/" + nuevoContenido.getIdUsuario();
        String url_cursos = "http://localhost:8084/api/cursos/" + nuevoContenido.getIdCurso();
        UsuarioDTO usuario = restTemplate.getForObject(url_usuarios, UsuarioDTO.class);
        if (contenidoEducativoRepository.existsById(nuevoContenido.getContId())) {
            throw new IllegalArgumentException("Ya existe contenido con ID: " + nuevoContenido.getContId());    
        }
        if (usuario == null) {
            throw new EntityNotFoundException("Usuario no encontrado con id " + nuevoContenido.getIdUsuario());
        }
        if (usuario.getTipoUsuario() != TipoUsuario.PROFESOR) {
            throw new IllegalArgumentException("Solo los profesores pueden subir contenido.");
        }
        nuevoContenido.setFechaPublicacion(LocalDate.now());
        return contenidoEducativoRepository.save(nuevoContenido);
    }

    public void eliminarContenido(int id) {
        if (!contenidoEducativoRepository.existsById(id)) {
            throw new EntityNotFoundException("Contenido no encontrado con ID: " + id);
        }
        contenidoEducativoRepository.deleteById(id);
    }


}