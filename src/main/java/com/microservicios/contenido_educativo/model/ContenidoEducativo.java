package com.microservicios.contenido_educativo.model;

import java.time.LocalDate;

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
    private int contId;

    @Column(length = 1000, nullable = false)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoContenido tipo;
    
    private String url;

    private LocalDate fechaPublicacion;

    private int idCurso;

    private int idUsuario;

}
