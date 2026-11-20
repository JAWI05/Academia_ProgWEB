package br.com.academiafitness.repository;

import br.com.academiafitness.filtro.ClienteFiltro;
import br.com.academiafitness.filtro.SituacaoCliente;
import br.com.academiafitness.model.Aula;
import br.com.academiafitness.model.Cliente;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/** Monta a consulta de clientes combinando só os critérios informados. */
public final class ClienteSpecs {

    private ClienteSpecs() {
    }

    public static Specification<Cliente> filtrar(ClienteFiltro f) {
        Specification<Cliente> spec = (root, query, cb) -> cb.conjunction();

        if (f.temTexto()) {
            String padrao = AulaSpecs.padraoLike(f.getQ());
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.<String>get("nome")), padrao, '\\'),
                    cb.like(cb.lower(root.<String>get("email")), padrao, '\\'),
                    cb.like(cb.lower(root.<String>get("objetivo")), padrao, '\\')));
        }
        if (f.temObjetivo()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.<String>get("objetivo"), f.getObjetivo().trim()));
        }
        if (f.getCadastroDe() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.<LocalDate>get("dataCadastro"), f.getCadastroDe()));
        }
        if (f.getCadastroAte() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.<LocalDate>get("dataCadastro"), f.getCadastroAte()));
        }
        if (f.getSituacao() != SituacaoCliente.TODOS) {
            boolean comInscricao = f.getSituacao() == SituacaoCliente.COM_INSCRICAO;
            spec = spec.and((root, query, cb) -> {
                Predicate existe = cb.exists(subqueryInscricao(root, query, cb));
                return comInscricao ? existe : cb.not(existe);
            });
        }
        return spec;
    }

    /** "existe alguma aula na qual este cliente está inscrito". */
    private static Subquery<Long> subqueryInscricao(Root<Cliente> cliente,
                                                    jakarta.persistence.criteria.CriteriaQuery<?> query,
                                                    jakarta.persistence.criteria.CriteriaBuilder cb) {
        Subquery<Long> sub = query.subquery(Long.class);
        Root<Aula> aula = sub.from(Aula.class);
        Join<Aula, Cliente> inscrito = aula.join("inscritos");
        sub.select(aula.<Long>get("id"))
                .where(cb.equal(inscrito.get("id"), cliente.get("id")));
        return sub;
    }
}
