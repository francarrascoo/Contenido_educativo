package com.microservicios.contenido_educativo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservicios.contenido_educativo.model.ContenidoEducativo;
import com.microservicios.contenido_educativo.service.ContenidoEducativoService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping ("api/contenido-educativo")

public class ContenidoEducativoController {
    
    @Autowired ContenidoEducativoService contenidoEducativoService;

    @GetMapping
    public ResponseEntity<?> getContenidoEducativo() {
        try {
            List<ContenidoEducativo> contenido = contenidoEducativoService.listarContenidos();
            if (!contenido.isEmpty()) {
                return new ResponseEntity<>(contenido, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("No hay contenido registrado", HttpStatus.NO_CONTENT);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error al obtener contenido: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getContenidoId(@PathVariable Long id) {
        try {
            Optional<ContenidoEducativo> contenido = contenidoEducativoService.buscarContenidoPorId(id);
            if (contenido.isPresent()) {
                return new ResponseEntity<>(contenido.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Contenido no encontrado con ID: " + id, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error al buscar contenido: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<?> crearContenido(@Valid @RequestBody ContenidoEducativo nuevoContenido) {
        try {
            ContenidoEducativo contenidoCreado = contenidoEducativoService.crearContenido(nuevoContenido);
            return new ResponseEntity<>(contenidoCreado, HttpStatus.OK);
        }  catch (EntityNotFoundException | IllegalArgumentException ex) {
            return new ResponseEntity<>("Error: " + ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (RuntimeException ex) {
            return new ResponseEntity<>("Error: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarContenido(@PathVariable Long id, @Valid @RequestBody ContenidoEducativo contenido) {
        try {
            contenido.setContId(id);
            ContenidoEducativo contenidoActualizado = contenidoEducativoService.actualizarContenido(contenido);
            return new ResponseEntity<>(contenidoActualizado, HttpStatus.OK);
        } catch (EntityNotFoundException ex) {
            return new ResponseEntity<>("Error: " + ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>("Error: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException ex) {
            return new ResponseEntity<>("Error: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarContenido(@PathVariable Long id) {
        try {
            contenidoEducativoService.eliminarContenido(id);
            return new ResponseEntity<>("Contenido eliminado correctamente", HttpStatus.OK);
        } catch (EntityNotFoundException ex) {
            return new ResponseEntity<>("Error al eliminar el contenido: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
