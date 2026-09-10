package com.sementelivre.backend.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Contrato base para os repositórios de domínio. Estende
 * {@link JpaRepository}, oferecendo CRUD e paginação prontos. Os repositórios
 * concretos devem estender esta interface em vez de {@code JpaRepository}
 * diretamente, para centralizar futuros métodos comuns a todos os domínios.
 *
 * @param <T>  tipo da entidade
 * @param <ID> tipo do identificador da entidade
 */
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {
}
