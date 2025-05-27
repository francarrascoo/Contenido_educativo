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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservicios.contenido_educativo.model.ContenidoEducativo;
import com.microservicios.contenido_educativo.service.ContenidoEducativoService;

@RestController
@RequestMapping ("api/contenido-educativo")

public class ContenidoEducativoController {
    
    @Autowired
    private ContenidoEducativoService contenidoEducativoService;

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
    public ResponseEntity<?> getContenidoId(@PathVariable int id) {
        try {
            Optional<ContenidoEducativo> contenido = contenidoEducativoService.buscarContenidoPorId(id);
            if (contenido.isPresent()) {
                return new ResponseEntity<>(contenido.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Contenido no encontradp con ID: " + id, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error al buscar contenido: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<?> crearContenido(@RequestBody ContenidoEducativo nuevoContenido) {
        try {
            ContenidoEducativo contenidoCreado = contenidoEducativoService.crearContenido(nuevoContenido);
            return new ResponseEntity<>(contenidoCreado, HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>("Error: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarContenido(@PathVariable int id) {
        try {
            contenidoEducativoService.eliminarContenido(id);
            return new ResponseEntity<>("Contenido eliminado correctamente", HttpStatus.OK);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>("Error al eliminar el contenido: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
