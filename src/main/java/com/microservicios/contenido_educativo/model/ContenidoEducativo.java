package com.microservicios.contenido_educativo.model;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ContenidoEducativo {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long contId;

    @Column(length = 1000, nullable = false)
    private String descripcion;

    @NotNull(message = "El tipo de contenido es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoContenido tipo;

    @NotNull(message = "La URL es obligatoria para el tipo ENLACE")
    @Column(nullable = true)
    private String url;

    @NotNull(message = "La fecha de publicación es obligatoria")
    @Column(nullable = false)
    private LocalDate fechaPublicacion;

    @NotNull(message = "El ID del curso es obligatorio")
    @Column(nullable = false)
    private int idCurso;

    @NotNull(message = "El ID del usuario es obligatorio")
    @Column(nullable = false)
    private int idUsuario;

}
