package com.microservicios.contenido_educativo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservicios.contenido_educativo.model.ContenidoEducativo;
import com.microservicios.contenido_educativo.model.TipoContenido;
import com.microservicios.contenido_educativo.service.ContenidoEducativoService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContenidoEducativoController.class)
public class ContenidoEducativoControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContenidoEducativoService contenidoEducativoService;

    @Autowired
    private ObjectMapper objectMapper;

    private ContenidoEducativo contenidoMock() {
        return new ContenidoEducativo(
            1L,
            "Descripción de prueba",
            TipoContenido.ENLACE,
            "https://url.com",
            LocalDate.now(),
            1,
            1
        );
    }

    @Test
    void testListarContenidos_ConDatos() throws Exception {
        when(contenidoEducativoService.listarContenidos()).thenReturn(Arrays.asList(contenidoMock()));
        mockMvc.perform(get("/api/contenido-educativo"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].contId").value(1));
    }

    @Test
    void testListarContenidos_Vacio() throws Exception {
        when(contenidoEducativoService.listarContenidos()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/contenido-educativo"))
            .andExpect(status().isNoContent());
    }

    @Test
    void testListarContenidos_ErrorInterno() throws Exception {
        when(contenidoEducativoService.listarContenidos()).thenThrow(new RuntimeException("Fallo"));
        mockMvc.perform(get("/api/contenido-educativo"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Error al obtener contenido")));
    }

    @Test
    void testBuscarPorId() throws Exception {
        when(contenidoEducativoService.buscarContenidoPorId(1L)).thenReturn(Optional.of(contenidoMock()));
        mockMvc.perform(get("/api/contenido-educativo/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.contId").value(1));
    }

    @Test
    void testBuscarPorId_NoExistente() throws Exception {
        when(contenidoEducativoService.buscarContenidoPorId(1L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/contenido-educativo/1"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testBuscarPorId_ErrorInterno() throws Exception {
        when(contenidoEducativoService.buscarContenidoPorId(1L)).thenThrow(new RuntimeException("Fallo"));
        mockMvc.perform(get("/api/contenido-educativo/1"))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void testBuscarPorId_RuntimeException() throws Exception {
        when(contenidoEducativoService.buscarContenidoPorId(1L))
            .thenThrow(new RuntimeException("Error inesperado"));
        mockMvc.perform(get("/api/contenido-educativo/1"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Error al buscar contenido: Error inesperado")));
    }

    @Test
    void testCrearContenido_OK() throws Exception {
        ContenidoEducativo input = contenidoMock();
        input.setContId(null);
        when(contenidoEducativoService.crearContenido(any(ContenidoEducativo.class))).thenReturn(contenidoMock());
        mockMvc.perform(post("/api/contenido-educativo")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.contId").value(1));
    }

    @Test
    void testCrearContenido_BadRequest() throws Exception {
        ContenidoEducativo input = contenidoMock();
        input.setContId(null);
        when(contenidoEducativoService.crearContenido(any(ContenidoEducativo.class)))
            .thenThrow(new RuntimeException("Error genérico"));
        mockMvc.perform(post("/api/contenido-educativo")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testCrearContenido_EntityNotFoundException() throws Exception {
        ContenidoEducativo input = contenidoMock();
        input.setContId(null); // para crear contenido nuevo el id debe ser null
        when(contenidoEducativoService.crearContenido(any(ContenidoEducativo.class)))
            .thenThrow(new jakarta.persistence.EntityNotFoundException("Usuario o curso no encontrado"));
        mockMvc.perform(post("/api/contenido-educativo")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input)))
            .andExpect(status().isNotFound())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Error: Usuario o curso no encontrado")));
    }

    @Test
    void testActualizarContenido_OK() throws Exception {
        ContenidoEducativo input = contenidoMock();
        when(contenidoEducativoService.actualizarContenido(any(ContenidoEducativo.class))).thenReturn(input);
        mockMvc.perform(put("/api/contenido-educativo/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.descripcion").value("Descripción de prueba"));
    }

    @Test
    void testActualizarContenido_NotFound() throws Exception {
        ContenidoEducativo input = contenidoMock();
        when(contenidoEducativoService.actualizarContenido(any(ContenidoEducativo.class)))
            .thenThrow(new jakarta.persistence.EntityNotFoundException("No encontrado"));
        mockMvc.perform(put("/api/contenido-educativo/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input)))
            .andExpect(status().isNotFound());
    }

    @Test
    void testActualizarContenido_BadRequest() throws Exception {
        ContenidoEducativo input = contenidoMock();
        when(contenidoEducativoService.actualizarContenido(any(ContenidoEducativo.class)))
            .thenThrow(new IllegalArgumentException("Dato inválido"));
        mockMvc.perform(put("/api/contenido-educativo/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testActualizarContenido_RuntimeException() throws Exception {
        ContenidoEducativo input = contenidoMock();
        when(contenidoEducativoService.actualizarContenido(any(ContenidoEducativo.class)))
            .thenThrow(new RuntimeException("Error inesperado"));
        mockMvc.perform(put("/api/contenido-educativo/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input)))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Error: Error inesperado")));
    }

    @Test
    void testEliminarContenido_OK() throws Exception {
        mockMvc.perform(delete("/api/contenido-educativo/1"))
            .andExpect(status().isOk())
            .andExpect(content().string("Contenido eliminado correctamente"));
    }

    @Test
    void testEliminarContenido_NotFound() throws Exception {
        doThrow(new jakarta.persistence.EntityNotFoundException("No existe"))
            .when(contenidoEducativoService).eliminarContenido(1L);
        mockMvc.perform(delete("/api/contenido-educativo/1"))
            .andExpect(status().isInternalServerError());
    }

}
