package org.paulohenrique.repository;

import org.paulohenrique.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LivroRepository extends JpaRepository<Livro, Long> {
    @Query("SELECT l FROM Livro l WHERE " +
            "(:autor IS NULL OR l.autor = :autor) AND " +
            "(:ano IS NULL OR l.ano = :ano) AND " +
            "(:disponivel IS NULL OR l.disponivel = :disponivel)")
    List<Livro> findLivrosByAutorAndAnoAndDisponivel(String autor, Integer ano, Boolean disponivel);
}
