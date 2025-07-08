package com.microservicios.contenido_educativo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.microservicios.contenido_educativo.assemblers.ContenidoEducativoModelAssembler;
import com.microservicios.contenido_educativo.model.ContenidoEducativo;
import com.microservicios.contenido_educativo.service.ContenidoEducativoService;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/contenido-educativo")

public class ContenidoEducativoController {

    @Autowired
    private ContenidoEducativoService contenidoEducativoService;

    @Autowired
    private ContenidoEducativoModelAssembler contenidoEducativoModelAssembler;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ContenidoEducativo>>> getContenidoEducativo() {
        try {
            List<ContenidoEducativo> contenidos = contenidoEducativoService.listarContenidos();
            if (contenidos.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            List<EntityModel<ContenidoEducativo>> modelos = contenidos.stream()
                    .map(contenidoEducativoModelAssembler::toModel)
                    .toList();
            CollectionModel<EntityModel<ContenidoEducativo>> coleccion = CollectionModel.of(
                    modelos,
                    linkTo(methodOn(ContenidoEducativoController.class).getContenidoEducativo()).withSelfRel());
            return ResponseEntity.ok(coleccion);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ContenidoEducativo>> getContenidoId(@PathVariable Long id) {
        try {
            Optional<ContenidoEducativo> contenido = contenidoEducativoService.buscarContenidoPorId(id);
            if (contenido.isPresent()) {
                EntityModel<ContenidoEducativo> model = contenidoEducativoModelAssembler.toModel(contenido.get());
                return ResponseEntity.ok(model);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<EntityModel<ContenidoEducativo>> crearContenido(
            @Valid @RequestBody ContenidoEducativo contenido) {
        try {
            ContenidoEducativo creado = contenidoEducativoService.crearContenido(contenido);
            EntityModel<ContenidoEducativo> model = contenidoEducativoModelAssembler.toModel(creado);
            return new ResponseEntity<>(model, HttpStatus.CREATED);
        } catch (EntityNotFoundException | IllegalArgumentException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (RuntimeException ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarContenido(@PathVariable Long id,
            @Valid @RequestBody ContenidoEducativo contenido) {
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
    public ResponseEntity<EntityModel<ContenidoEducativo>> eliminarContenido(@PathVariable Long id) {
        try {
            ContenidoEducativo eliminado = contenidoEducativoService.eliminarContenido(id);
            EntityModel<ContenidoEducativo> model = contenidoEducativoModelAssembler.toModel(eliminado);
            return ResponseEntity.ok(model);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
