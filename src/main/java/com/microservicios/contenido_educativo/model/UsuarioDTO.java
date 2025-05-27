package com.microservicios.contenido_educativo.model;

import lombok.Data;

@Data
public class UsuarioDTO {
    private int idUsuario;
    private String nombreUsuario;
    private String apellidoPUsuario;
    private String apellidoMUsuario;
    private String emailInstitucional;
    private TipoUsuario tipoUsuario;
    private boolean activo;
}

