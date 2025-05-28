package com.microservicios.contenido_educativo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.microservicios.contenido_educativo.model.ContenidoEducativo;
import com.microservicios.contenido_educativo.model.CursoDTO;
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
        UsuarioDTO usuario = restTemplate.getForObject(url_usuarios, UsuarioDTO.class);
        if (usuario == null) {
            throw new EntityNotFoundException("Usuario no encontrado con id " + nuevoContenido.getIdUsuario());
        }
        if (usuario.getTipoUsuario() != TipoUsuario.PROFESOR) {
            throw new IllegalArgumentException("Solo los profesores pueden subir contenido.");
        }
        CursoDTO curso;
        try {
            String url_cursos = "http://localhost:8083/api/cursos/" + nuevoContenido.getIdCurso();
            curso = restTemplate.getForObject(url_cursos, CursoDTO.class);
            if (curso == null || curso.getIdCurso() == null) {
                throw new IllegalArgumentException("Curso no encontrado con ID: " + nuevoContenido.getIdCurso());
            }
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Curso no encontrado con ID: " + nuevoContenido.getIdCurso());
        } catch (RestClientException e) {
            throw new RuntimeException("Error al consultar el curso: " + e.getMessage());
        }
        if (contenidoEducativoRepository.existsById(nuevoContenido.getContId())) {
            throw new IllegalArgumentException("Ya existe contenido con ID: " + nuevoContenido.getContId());
    }
    nuevoContenido.setFechaPublicacion(LocalDate.now());
    return contenidoEducativoRepository.save(nuevoContenido);
    }

    public ContenidoEducativo actualizarContenido(ContenidoEducativo contenido) {
        ContenidoEducativo existente = contenidoEducativoRepository.findById(contenido.getContId())
            .orElseThrow(() -> new EntityNotFoundException("Contenido no encontrado con id " + contenido.getContId()));
        String urlUsuario = "http://localhost:8081/api/usuarios/" + contenido.getIdUsuario();
        UsuarioDTO usuario = restTemplate.getForObject(urlUsuario, UsuarioDTO.class);
        if (usuario == null) {
            throw new EntityNotFoundException("Usuario no encontrado con id " + contenido.getIdUsuario());
        }
        if (usuario.getTipoUsuario() != TipoUsuario.PROFESOR) {
            throw new IllegalArgumentException("Solo los profesores pueden subir contenido.");
        }
        try {
            String urlCurso = "http://localhost:8083/api/cursos/" + contenido.getIdCurso();
            CursoDTO curso = restTemplate.getForObject(urlCurso, CursoDTO.class);
            if (curso == null || curso.getIdCurso() == null) {
                throw new EntityNotFoundException("Curso no encontrado con id " + contenido.getIdCurso());
            }
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Curso no encontrado con id " + contenido.getIdCurso());
        } catch (RestClientException e) {
            throw new RuntimeException("Error al consultar el curso: " + e.getMessage());
        }
        existente.setDescripcion(contenido.getDescripcion());
        existente.setTipo(contenido.getTipo());
        existente.setUrl(contenido.getUrl());
        existente.setIdCurso(contenido.getIdCurso());
        existente.setIdUsuario(contenido.getIdUsuario());
        existente.setFechaPublicacion(LocalDate.now());
        return contenidoEducativoRepository.save(existente);     
    }

    public void eliminarContenido(int id) {
        if (!contenidoEducativoRepository.existsById(id)) {
            throw new EntityNotFoundException("Contenido no encontrado con ID: " + id);
        }
        contenidoEducativoRepository.deleteById(id);
    }


}