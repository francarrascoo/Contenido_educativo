package com.microservicios.contenido_educativo.model;

import lombok.Data;

@Data
public class CursoDTO {
    private Long idCurso;
    private String nombreCurso;
    private String descripcionCurso;
    private int cupoMaximo;
    private int cupoDisponible;
}
