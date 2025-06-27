package com.microservicios.contenido_educativo.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.microservicios.contenido_educativo.model.ContenidoEducativo;
import com.microservicios.contenido_educativo.model.CursoDTO;
import com.microservicios.contenido_educativo.model.TipoContenido;
import com.microservicios.contenido_educativo.model.TipoUsuario;
import com.microservicios.contenido_educativo.model.UsuarioDTO;
import com.microservicios.contenido_educativo.repository.ContenidoEducativoRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class ContenidoEducativoServiceTest {

    @Mock
    private ContenidoEducativoRepository contenidoEducativoRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ContenidoEducativoService contenidoEducativoService;

    @Test
    void testListarContenidos(){
        ContenidoEducativo contenido1 = new ContenidoEducativo(1L, "Video introducción", TipoContenido.VIDEO, "https://example.com/video", LocalDate.now(), 1, 1);
        ContenidoEducativo contenido2 = new ContenidoEducativo(2L, "Documento de estudio", TipoContenido.DOCUMENTO, "https://example.com/documento", LocalDate.now(), 1, 1);
        List<ContenidoEducativo> contenidos = Arrays.asList(contenido1, contenido2);
        when(contenidoEducativoRepository.findAll()).thenReturn(contenidos);
        List<ContenidoEducativo> resultado = contenidoEducativoService.listarContenidos();
        assertThat(resultado).hasSize(2).contains(contenido1, contenido2);
        verify(contenidoEducativoRepository).findAll();
    }

    @Test
    void testBuscarContenidoPorId() {
        ContenidoEducativo contenido = new ContenidoEducativo(1L, "Video introducción", TipoContenido.VIDEO, "https://example.com/video", LocalDate.now(), 1, 1);
        when(contenidoEducativoRepository.findById(1L)).thenReturn(Optional.of(contenido));
        Optional<ContenidoEducativo> resultado = contenidoEducativoService.buscarContenidoPorId(1L);
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getContId()).isEqualTo(1);
        verify(contenidoEducativoRepository).findById(1L);
    }

    @Test
    void testBuscarContenidoPorId_NoExistente() {
        Long idBuscado = 999L;
        when(contenidoEducativoRepository.findById(idBuscado)).thenReturn(Optional.empty());
        Optional<ContenidoEducativo> resultado = contenidoEducativoService.buscarContenidoPorId(idBuscado);
        assertThat(resultado).isNotPresent();
        verify(contenidoEducativoRepository).findById(idBuscado);
    }

    // ----- Tests para crear contenido -----

    @Test
    void testCrearContenidoEnlace() {
        ContenidoEducativo nuevo = new ContenidoEducativo(null, "Video introducción", TipoContenido.ENLACE, "https://example.com", null, 1, 1);
        ContenidoEducativo guardado = new ContenidoEducativo(1L, "Video introducción", TipoContenido.ENLACE, "https://example.com", LocalDate.now(), 1, 1);
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setIdUsuario(1L);
        usuarioDTO.setTipoUsuario(TipoUsuario.PROFESOR);
        usuarioDTO.setActivo(true);
        CursoDTO cursoDTO = new CursoDTO();
        cursoDTO.setIdCurso(1L);
        when(restTemplate.getForObject("http://localhost:8081/api/usuarios/1", UsuarioDTO.class)).thenReturn(usuarioDTO);
        when(restTemplate.getForObject("http://localhost:8083/api/cursos/1", CursoDTO.class)).thenReturn(cursoDTO);
        when(contenidoEducativoRepository.save(nuevo)).thenReturn(guardado);
        ContenidoEducativo resultado = contenidoEducativoService.crearContenido(nuevo);
        assertThat(resultado.getContId()).isEqualTo(1L);
        assertThat(resultado.getUrl()).isEqualTo("https://example.com");
        assertThat(resultado.getFechaPublicacion()).isEqualTo(LocalDate.now());
        verify(contenidoEducativoRepository).save(nuevo);
    }

    @Test
    void testCrearContenidoDocumento() {
        ContenidoEducativo nuevo = new ContenidoEducativo(null, "Documento importante", TipoContenido.DOCUMENTO, null, null, 1, 1);
        ContenidoEducativo guardado = new ContenidoEducativo(1L, "Documento importante", TipoContenido.DOCUMENTO, null, LocalDate.now(), 1, 1);
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setIdUsuario(1L);
        usuarioDTO.setTipoUsuario(TipoUsuario.PROFESOR);
        usuarioDTO.setActivo(true);
        CursoDTO cursoDTO = new CursoDTO();
        cursoDTO.setIdCurso(1L);
        when(restTemplate.getForObject("http://localhost:8081/api/usuarios/1", UsuarioDTO.class)).thenReturn(usuarioDTO);
        when(restTemplate.getForObject("http://localhost:8083/api/cursos/1", CursoDTO.class)).thenReturn(cursoDTO);
        when(contenidoEducativoRepository.save(nuevo)).thenReturn(guardado);
        ContenidoEducativo resultado = contenidoEducativoService.crearContenido(nuevo);
        assertThat(resultado.getContId()).isEqualTo(1L);
        assertThat(resultado.getUrl()).isEqualTo(null);
        assertThat(resultado.getFechaPublicacion()).isEqualTo(LocalDate.now());
        verify(contenidoEducativoRepository).save(nuevo);
    }

    @Test
    void testCrearContenido_UsuarioNoEncontrado() {
        ContenidoEducativo nuevo = new ContenidoEducativo(null, "Video introducción", TipoContenido.VIDEO, null, null, 1, 1);
        when(restTemplate.getForObject("http://localhost:8081/api/usuarios/1", UsuarioDTO.class)).thenReturn(null);
        assertThatThrownBy(() -> contenidoEducativoService.crearContenido(nuevo))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Usuario no encontrado con ID: 1");
        verify(contenidoEducativoRepository, never()).save(any(ContenidoEducativo.class));
    }

    @Test
    void testCrearContenido_UsuarioNoProfesor() {
        ContenidoEducativo nuevo = new ContenidoEducativo(null, "Video introducción", TipoContenido.VIDEO, null, null, 1, 1);
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setIdUsuario(1L);
        usuarioDTO.setTipoUsuario(TipoUsuario.ESTUDIANTE);
        when(restTemplate.getForObject("http://localhost:8081/api/usuarios/1", UsuarioDTO.class)).thenReturn(usuarioDTO);
        assertThatThrownBy(() -> contenidoEducativoService.crearContenido(nuevo))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Solo los profesores pueden subir contenido.");
        verify(contenidoEducativoRepository, never()).save(any(ContenidoEducativo.class));
    }

    @Test
    void testCrearContenido_UsuarioInactivo() {
        ContenidoEducativo nuevo = new ContenidoEducativo(null, "Video introducción", TipoContenido.VIDEO, null, null, 1, 1);
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setIdUsuario(1L);
        usuarioDTO.setTipoUsuario(TipoUsuario.PROFESOR);
        usuarioDTO.setActivo(false);
        when(restTemplate.getForObject("http://localhost:8081/api/usuarios/1", UsuarioDTO.class)).thenReturn(usuarioDTO);
        assertThatThrownBy(() -> contenidoEducativoService.crearContenido(nuevo))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("El usuario no está activo.");
        verify(contenidoEducativoRepository, never()).save(any(ContenidoEducativo.class));
    }

    @Test
    void testCrearContenido_CursoNoEncontrado() {
        ContenidoEducativo nuevo = new ContenidoEducativo(null, "Video introducción", TipoContenido.VIDEO, null, null, 1, 1);
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setIdUsuario(1L);
        usuarioDTO.setTipoUsuario(TipoUsuario.PROFESOR);
        usuarioDTO.setActivo(true);
        CursoDTO curso = new CursoDTO();
        when(restTemplate.getForObject("http://localhost:8081/api/usuarios/1", UsuarioDTO.class)).thenReturn(usuarioDTO);
        when(restTemplate.getForObject("http://localhost:8083/api/cursos/1", CursoDTO.class)).thenReturn(curso);
        assertThatThrownBy(() -> contenidoEducativoService.crearContenido(nuevo))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Curso no encontrado con ID: 1");
        verify(contenidoEducativoRepository, never()).save(any(ContenidoEducativo.class));
    }

    @Test
    void testCrearContenido_EnlaceSinURL() {
        ContenidoEducativo nuevo = new ContenidoEducativo(null, "Video introducción", TipoContenido.ENLACE, null, null, 1, 1);
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setIdUsuario(1L);
        usuarioDTO.setTipoUsuario(TipoUsuario.PROFESOR);
        usuarioDTO.setActivo(true);
        CursoDTO cursoDTO = new CursoDTO();
        cursoDTO.setIdCurso(1L);
        when(restTemplate.getForObject("http://localhost:8081/api/usuarios/1", UsuarioDTO.class)).thenReturn(usuarioDTO);
        when(restTemplate.getForObject("http://localhost:8083/api/cursos/1", CursoDTO.class)).thenReturn(cursoDTO);
        assertThatThrownBy(() -> contenidoEducativoService.crearContenido(nuevo))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Para contenido tipo ENLACE debe proporcionar una URL.");
        verify(contenidoEducativoRepository, never()).save(any(ContenidoEducativo.class));
    }

    @Test
    void testCrearContenido_ArchivoConURL() {
        ContenidoEducativo contenido = new ContenidoEducativo(null, "Guía PDF", TipoContenido.DOCUMENTO, "https://documento.com/archivo.pdf", null, 1, 1);
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setIdUsuario(1L);
        usuario.setTipoUsuario(TipoUsuario.PROFESOR);
        usuario.setActivo(true);
        CursoDTO curso = new CursoDTO();
        curso.setIdCurso(1L);
        when(restTemplate.getForObject("http://localhost:8081/api/usuarios/1", UsuarioDTO.class)).thenReturn(usuario);
        when(restTemplate.getForObject("http://localhost:8083/api/cursos/1", CursoDTO.class)).thenReturn(curso);
        assertThatThrownBy(() -> contenidoEducativoService.crearContenido(contenido))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("El campo URL solo se permite para contenido tipo ENLACE.");
        verify(contenidoEducativoRepository, never()).save(any(ContenidoEducativo.class));
    }

    @Test
    void testCrearContenido_RestClientException() {
        ContenidoEducativo contenido = new ContenidoEducativo(
            null, "Content", TipoContenido.DOCUMENTO, 
            null, null, 1, 1);
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setIdUsuario(1L);
        usuarioDTO.setTipoUsuario(TipoUsuario.PROFESOR);
        usuarioDTO.setActivo(true);
        when(restTemplate.getForObject(anyString(), eq(UsuarioDTO.class)))
            .thenReturn(usuarioDTO);
        when(restTemplate.getForObject(anyString(), eq(CursoDTO.class)))
            .thenThrow(new RestClientException("Service unavailable"));
        assertThatThrownBy(() -> contenidoEducativoService.crearContenido(contenido))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Error al consultar el curso");
    }

    // ----- Tests para actualizar contenido -----

    @Test
    void testActualizarContenido_Enlace() {
        ContenidoEducativo contenidoExistente = new ContenidoEducativo(1L, "Antigua descripción", TipoContenido.ENLACE, "url_antigua", LocalDate.now(), 1, 1);
        contenidoExistente.setDescripcion("Nueva descripción");
        contenidoExistente.setUrl("url_nueva");
        contenidoExistente.setTipo(TipoContenido.ENLACE);
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setIdUsuario(1L);
        usuarioDTO.setTipoUsuario(TipoUsuario.PROFESOR);
        usuarioDTO.setActivo(true);
        CursoDTO cursoDTO = new CursoDTO();
        cursoDTO.setIdCurso(1L);
        when(contenidoEducativoRepository.findById(1L)).thenReturn(Optional.of(contenidoExistente));
        when(restTemplate.getForObject("http://localhost:8081/api/usuarios/1", UsuarioDTO.class)).thenReturn(usuarioDTO);
        when(restTemplate.getForObject("http://localhost:8083/api/cursos/1", CursoDTO.class)).thenReturn(cursoDTO);
        when(contenidoEducativoRepository.save(contenidoExistente)).thenReturn(contenidoExistente);
        ContenidoEducativo resultado = contenidoEducativoService.actualizarContenido(contenidoExistente);
        assertThat(resultado.getDescripcion()).isEqualTo("Nueva descripción");
        assertThat(resultado.getTipo()).isEqualTo(TipoContenido.ENLACE);
        assertThat(resultado.getUrl()).isEqualTo("url_nueva");
        assertThat(resultado.getFechaPublicacion()).isEqualTo(LocalDate.now());
        verify(contenidoEducativoRepository).save(contenidoExistente);
    }

    @Test
    void testActualizarContenido_UsuarioNoEncontrado() {
        ContenidoEducativo contenido = new ContenidoEducativo(1L, "Descripción", TipoContenido.ENLACE, "http://example.com", LocalDate.now(), 1, 1);
        when(contenidoEducativoRepository.findById(1L)).thenReturn(Optional.of(contenido));
        when(restTemplate.getForObject("http://localhost:8081/api/usuarios/1", UsuarioDTO.class)).thenReturn(null);
        assertThatThrownBy(() -> contenidoEducativoService.actualizarContenido(contenido))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Usuario no encontrado con ID: 1");
        verify(contenidoEducativoRepository, never()).save(any());
    }

    @Test
    void testActualizarContenido_UsuarioNoProfesor() {
        ContenidoEducativo contenido = new ContenidoEducativo(1L, "Descripción", TipoContenido.ENLACE, "http://example.com", LocalDate.now(), 1, 1);
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setIdUsuario(1L);
        usuarioDTO.setTipoUsuario(TipoUsuario.ESTUDIANTE);
        when(contenidoEducativoRepository.findById(1L)).thenReturn(Optional.of(contenido));
        when(restTemplate.getForObject("http://localhost:8081/api/usuarios/1", UsuarioDTO.class)).thenReturn(usuarioDTO);
        assertThatThrownBy(() -> contenidoEducativoService.actualizarContenido(contenido))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Solo los profesores pueden subir contenido.");
        verify(contenidoEducativoRepository, never()).save(any());
    }

    @Test
    void testActualizarContenido_UsuarioInactivo() {
        ContenidoEducativo contenido = new ContenidoEducativo(1L, "Descripción", TipoContenido.ENLACE, "http://example.com", LocalDate.now(), 1, 1);
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setIdUsuario(1L);
        usuarioDTO.setTipoUsuario(TipoUsuario.PROFESOR);
        usuarioDTO.setActivo(false);
        when(contenidoEducativoRepository.findById(1L)).thenReturn(Optional.of(contenido));
        when(restTemplate.getForObject("http://localhost:8081/api/usuarios/1", UsuarioDTO.class)).thenReturn(usuarioDTO);
        assertThatThrownBy(() -> contenidoEducativoService.actualizarContenido(contenido))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("El usuario no está activo.");
        verify(contenidoEducativoRepository, never()).save(any());
    }

    @Test
    void testActualizarContenido_CursoNoEncontrado() {
        ContenidoEducativo contenido = new ContenidoEducativo (1L, "Descripción", TipoContenido.ENLACE, "http://example.com", LocalDate.now(), 1, 1);
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setIdUsuario(1L);
        usuarioDTO.setTipoUsuario(TipoUsuario.PROFESOR);
        usuarioDTO.setActivo(true);
        CursoDTO cursoDTO = new CursoDTO();
        cursoDTO.setIdCurso(1L);
        when(contenidoEducativoRepository.findById(1L)).thenReturn(Optional.of(contenido));
        when(restTemplate.getForObject("http://localhost:8081/api/usuarios/1", UsuarioDTO.class)).thenReturn(usuarioDTO);
        when(restTemplate.getForObject("http://localhost:8083/api/cursos/1", CursoDTO.class)).thenReturn(null);
        assertThatThrownBy(() -> contenidoEducativoService.actualizarContenido(contenido))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Curso no encontrado con ID: 1");
        verify(contenidoEducativoRepository, never()).save(any());
    }

    @Test
    void testActualizarContenido_EnlaceSinURL() {
        ContenidoEducativo contenidoExistente = new ContenidoEducativo(1L, "Desc", TipoContenido.ENLACE, "url", LocalDate.now(), 1, 1);
        contenidoExistente.setDescripcion("Desc nueva");
        contenidoExistente.setTipo(TipoContenido.ENLACE);
        contenidoExistente.setUrl(null);
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setIdUsuario(1L);
        usuarioDTO.setTipoUsuario(TipoUsuario.PROFESOR);
        usuarioDTO.setActivo(true);
        CursoDTO cursoDTO = new CursoDTO();
        cursoDTO.setIdCurso(1L);
        when(contenidoEducativoRepository.findById(1L)).thenReturn(Optional.of(contenidoExistente));
        when(restTemplate.getForObject("http://localhost:8081/api/usuarios/1", UsuarioDTO.class)).thenReturn(usuarioDTO);
        when(restTemplate.getForObject("http://localhost:8083/api/cursos/1", CursoDTO.class)).thenReturn(cursoDTO);
        assertThatThrownBy(() -> contenidoEducativoService.actualizarContenido(contenidoExistente))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Para contenido tipo ENLACE debe proporcionar una URL.");
        verify(contenidoEducativoRepository, never()).save(any());
    }

    @Test
    void testActualizarContenido_ArchivoConURL() {
        ContenidoEducativo contenidoExistente = new ContenidoEducativo(1L, "Desc", TipoContenido.DOCUMENTO, "http://documento.com/archivo.pdf", LocalDate.now(), 1, 1);
        contenidoExistente.setDescripcion("Desc nueva");
        contenidoExistente.setTipo(TipoContenido.IMAGEN);
        contenidoExistente.setUrl("http://imagen.com/archivo.jpg");
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setIdUsuario(1L);
        usuarioDTO.setTipoUsuario(TipoUsuario.PROFESOR);
        usuarioDTO.setActivo(true);
        CursoDTO cursoDTO = new CursoDTO();
        cursoDTO.setIdCurso(1L);
        when(contenidoEducativoRepository.findById(1L)).thenReturn(Optional.of(contenidoExistente));
        when(restTemplate.getForObject("http://localhost:8081/api/usuarios/1", UsuarioDTO.class)).thenReturn(usuarioDTO);
        when(restTemplate.getForObject("http://localhost:8083/api/cursos/1", CursoDTO.class)).thenReturn(cursoDTO);
        assertThatThrownBy(() -> contenidoEducativoService.actualizarContenido(contenidoExistente))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("El campo URL solo se permite para contenido tipo ENLACE.");
        verify(contenidoEducativoRepository, never()).save(any());
    }

    @Test
    void testActualizarContenido_NoExistente() {
        ContenidoEducativo contenido = new ContenidoEducativo();
        contenido.setContId(999L);
        when(contenidoEducativoRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> contenidoEducativoService.actualizarContenido(contenido))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Contenido no encontrado con ID: 999");
        verify(contenidoEducativoRepository, never()).save(any());
    }

    // ----- Tests para eliminar contenido -----

    @Test
    void testEliminarContenido() {
        when(contenidoEducativoRepository.existsById(1L)).thenReturn(true);
        contenidoEducativoService.eliminarContenido(1L);
        verify(contenidoEducativoRepository).deleteById(1L);
    }

    @Test
    void testEliminarContenido_NoExistente() {
        Long idNoExistente = 999L;
        when(contenidoEducativoRepository.existsById(idNoExistente)).thenReturn(false);
        assertThatThrownBy(() -> contenidoEducativoService.eliminarContenido(idNoExistente))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Contenido no encontrado con ID: " + idNoExistente);
        verify(contenidoEducativoRepository, never()).deleteById(idNoExistente);
    }

}