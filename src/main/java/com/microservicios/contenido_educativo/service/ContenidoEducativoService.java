package com.microservicios.contenido_educativo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.microservicios.contenido_educativo.model.ContenidoEducativo;
import com.microservicios.contenido_educativo.model.CursoDTO;
import com.microservicios.contenido_educativo.model.TipoContenido;
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

    public Optional<ContenidoEducativo> buscarContenidoPorId(Long id) {
        return contenidoEducativoRepository.findById(id);
    }

    public ContenidoEducativo crearContenido(ContenidoEducativo nuevoContenido) {
        validarUsuario(nuevoContenido.getIdUsuario());
        validarCursoExistente(nuevoContenido.getIdCurso());
        validarContenidoSegunTipo(nuevoContenido);
        if (nuevoContenido.getContId() != null) {
            throw new IllegalArgumentException("El ID debe ser null al crear contenido nuevo");
        }
        nuevoContenido.setFechaPublicacion(LocalDate.now());
        return contenidoEducativoRepository.save(nuevoContenido);
    }

    public ContenidoEducativo actualizarContenido(ContenidoEducativo contenido) {
        ContenidoEducativo existente = contenidoEducativoRepository.findById(contenido.getContId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Contenido no encontrado con ID: " + contenido.getContId()));
        validarUsuario(contenido.getIdUsuario());
        validarCursoExistente(contenido.getIdCurso());
        existente.setDescripcion(contenido.getDescripcion());
        existente.setTipo(contenido.getTipo());
        validarContenidoSegunTipo(contenido);
        if (contenido.getTipo() == TipoContenido.ENLACE) {
            existente.setUrl(contenido.getUrl());
        } else {
            String urlGenerada = generarUrlAutomaticaParaContenido(contenido);
            existente.setUrl(urlGenerada);
        }
        existente.setFechaPublicacion(LocalDate.now());
        return contenidoEducativoRepository.save(existente);
    }

    public ContenidoEducativo eliminarContenido(Long id) {
        ContenidoEducativo contenido = contenidoEducativoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contenido no encontrado con ID: " + id));
        contenidoEducativoRepository.deleteById(id);
        return contenido;
    }

    private void validarUsuario(int idUsuario) {
        String url = "http://localhost:8081/api/usuarios/" + idUsuario;
        UsuarioDTO usuario = restTemplate.getForObject(url, UsuarioDTO.class);
        if (usuario == null) {
            throw new EntityNotFoundException("Usuario no encontrado con ID: " + idUsuario);
        }
        if (usuario.getTipoUsuario() != TipoUsuario.PROFESOR) {
            throw new IllegalArgumentException("Solo los profesores pueden subir contenido.");
        }
        if (usuario.isActivo() == false) {
            throw new IllegalArgumentException("El usuario no está activo.");
        }
    }

    private void validarCursoExistente(int idCurso) {
        String url = "http://localhost:8083/api/cursos/" + idCurso;
        try {
            CursoDTO curso = restTemplate.getForObject(url, CursoDTO.class);
            if (curso == null || curso.getIdCurso() == null) {
                throw new EntityNotFoundException("Curso no encontrado con ID: " + idCurso);
            }
        } catch (RestClientException e) {
            throw new RuntimeException("Error al consultar el curso: " + e.getMessage());
        }
    }

    private void validarContenidoSegunTipo(ContenidoEducativo contenido) {
        TipoContenido tipo = contenido.getTipo();
        String url = contenido.getUrl();
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo de contenido no puede ser nulo");
        }
        boolean esEnlace = tipo == TipoContenido.ENLACE;
        boolean urlVacia = url == null || url.isEmpty();
        if (esEnlace && urlVacia) {
            throw new IllegalArgumentException("Para contenido tipo ENLACE debe proporcionar una URL.");
        }
        if (!esEnlace && !urlVacia) {
            throw new IllegalArgumentException("El campo URL solo se permite para contenido tipo ENLACE.");
        }
    }

    private String generarUrlAutomaticaParaContenido(ContenidoEducativo contenido) {
        return "https://miservidor.com/contenido/" + contenido.getContId() + "/tipo/"
                + contenido.getTipo().name().toLowerCase();
    }

}