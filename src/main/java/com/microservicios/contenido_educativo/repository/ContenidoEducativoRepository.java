package com.microservicios.contenido_educativo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.microservicios.contenido_educativo.model.ContenidoEducativo;

@Repository
public interface ContenidoEducativoRepository extends JpaRepository<ContenidoEducativo, Long> {
    
}
