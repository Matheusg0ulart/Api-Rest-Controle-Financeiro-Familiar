package br.senac.fianceiro.categoria.repository;

import br.senac.fianceiro.categoria.entity.Categoria;
import br.senac.fianceiro.categoria.entity.TipoCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    @Query("SELECT c FROM Categoria c WHERE " +
            "c.usuario.id = :usuarioId OR (" +
            "   c.escopo = 'FAMILIAR' AND c.familia.id IN (" +
            "       SELECT fm.familia.id FROM FamiliaMembro fm WHERE fm.usuario.id = :usuarioId AND fm.status = 'ATIVO'" +
            "   )" +
            ") ORDER BY c.nome ASC")
    List<Categoria> findCategoriasAcessiveis(@Param("usuarioId") Long usuarioId);

    @Query("SELECT c FROM Categoria c WHERE (" +
            "c.usuario.id = :usuarioId OR (" +
            "   c.escopo = 'FAMILIAR' AND c.familia.id IN (" +
            "       SELECT fm.familia.id FROM FamiliaMembro fm WHERE fm.usuario.id = :usuarioId AND fm.status = 'ATIVO'" +
            "   )" +
            ")) AND c.tipo = :tipo ORDER BY c.nome ASC")
    List<Categoria> findCategoriasAcessiveisPorTipo(@Param("usuarioId") Long usuarioId, @Param("tipo") TipoCategoria tipo);
}
