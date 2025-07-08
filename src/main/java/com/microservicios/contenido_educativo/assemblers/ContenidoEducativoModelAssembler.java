package com.microservicios.contenido_educativo.assemblers;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.stereotype.Component;

import com.microservicios.contenido_educativo.controller.ContenidoEducativoController;
import com.microservicios.contenido_educativo.model.ContenidoEducativo;


@Component
public class ContenidoEducativoModelAssembler implements RepresentationModelAssembler<ContenidoEducativo, EntityModel<ContenidoEducativo>> {

    @Override
    @org.springframework.lang.NonNull
    public EntityModel<ContenidoEducativo> toModel(ContenidoEducativo contenido) {
        return EntityModel.of(contenido,
            linkTo(methodOn(ContenidoEducativoController.class).getContenidoId(contenido.getContId())).withSelfRel(),
            linkTo(methodOn(ContenidoEducativoController.class).getContenidoEducativo()).withRel("todos"),
            linkTo(methodOn(ContenidoEducativoController.class).eliminarContenido(contenido.getContId())).withRel("eliminar")
        );
    }
}
