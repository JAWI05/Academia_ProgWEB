package br.com.academiafitness.carga;

import br.com.academiafitness.model.*;
import br.com.academiafitness.repository.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Carga/seed idempotente (mesmo estilo do CargaDepartamentos do exemplo da Aula 16):
 * cada registro é localizado pela chave natural (UNIQUE) e só é inserido se não existir.
 * Ativada por academia.carga.inicial=true. A carga da planilha legada
 * (28_academias_fitness.xlsx) deve ser adicionada aqui no mesmo formato.
 */
@Component
@Order(1)
@ConditionalOnProperty(name = "academia.carga.inicial", havingValue = "true")
public class CargaInicial implements ApplicationRunner {

    private final UnidadeRepository unidades;
    private final EsporteRepository esportes;
    private final PersonalTrainerRepository personais;
    private final ClienteRepository clientes;
    private final AulaRepository aulas;

    public CargaInicial(UnidadeRepository unidades, EsporteRepository esportes,
                        PersonalTrainerRepository personais, ClienteRepository clientes,
                        AulaRepository aulas) {
        this.unidades = unidades;
        this.esportes = esportes;
        this.personais = personais;
        this.clientes = clientes;
        this.aulas = aulas;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Unidade centro = garantirUnidade("Unidade Centro", "São Paulo", "SP");
        garantirUnidade("Unidade Pinheiros", "São Paulo", "SP");

        Esporte musculacao = garantirEsporte("Musculação", StatusEsporte.ATIVO);
        garantirEsporte("Spinning", StatusEsporte.ATIVO);
        garantirEsporte("Natação", StatusEsporte.INATIVO);

        PersonalTrainer carla = garantirPersonal("Carla Mendes", "Hipertrofia", 8, "120.00");
        garantirPersonal("Rafael Lima", "Condicionamento", 4, "90.00");

        Cliente miriam = garantirCliente("Miriam Ester", "miriam@example.com", "Ganho de massa");
        garantirCliente("Alexandre Takeshi", "alexandre@example.com", "Emagrecimento");
        garantirCliente("Júlia Ayumi", "julia@example.com", "Condicionamento");

        if (aulas.count() == 0) {
            LocalDateTime inicio = LocalDateTime.now().plusDays(2)
                    .withHour(10).withMinute(0).withSecond(0).withNano(0);
            Aula aula = new Aula(musculacao, carla, centro, inicio, 60, 2);
            aula.inscrever(miriam);
            aulas.save(aula);
            System.out.println("INSERIDO: aula de demonstração em " + inicio);
        }
        System.out.println("Carga inicial concluída.");
    }

    private Unidade garantirUnidade(String nome, String cidade, String estado) {
        return unidades.findByNome(nome).orElseGet(() -> {
            System.out.printf("INSERIDO: unidade %s%n", nome);
            return unidades.save(new Unidade(nome, cidade, estado));
        });
    }

    private Esporte garantirEsporte(String nome, StatusEsporte status) {
        return esportes.findByNome(nome).orElseGet(() -> {
            System.out.printf("INSERIDO: esporte %s (%s)%n", nome, status);
            return esportes.save(new Esporte(nome, status));
        });
    }

    private PersonalTrainer garantirPersonal(String nome, String especialidade, int anos, String valorHora) {
        return personais.findByNome(nome).orElseGet(() -> {
            System.out.printf("INSERIDO: personal %s%n", nome);
            return personais.save(new PersonalTrainer(nome, especialidade, anos, new BigDecimal(valorHora)));
        });
    }

    private Cliente garantirCliente(String nome, String email, String objetivo) {
        return clientes.findByEmail(email).orElseGet(() -> {
            System.out.printf("INSERIDO: cliente %s%n", nome);
            return clientes.save(new Cliente(nome, email, objetivo));
        });
    }
}
