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
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Base de demonstração com dados fictícios (clientes, personais, unidades, esportes e aulas).
 * Usa semente fixa: a mesma base é gerada sempre (datas relativas a hoje).
 * Só roda quando a base está praticamente vazia, então reiniciar a aplicação não duplica nada.
 * Ligue/desligue com academia.carga.demo=true|false.
 */
@Component
@Order(2)
@ConditionalOnProperty(name = "academia.carga.demo", havingValue = "true")
public class CargaDemonstracao implements ApplicationRunner {

    private static final int TOTAL_CLIENTES = 60;
    private static final int TOTAL_AULAS = 45;
    private static final int[] DURACOES = {45, 60, 60, 90};

    private static final String[] NOMES = {
            "Ana", "Bruno", "Carla", "Daniel", "Eduarda", "Felipe", "Gabriela", "Henrique", "Isabela", "João",
            "Karina", "Lucas", "Mariana", "Nicolas", "Olívia", "Pedro", "Rafaela", "Sérgio", "Tatiana", "Vitor",
            "Beatriz", "Caio", "Débora", "Eduardo", "Fernanda", "Gustavo", "Helena", "Igor", "Juliana", "Leonardo"};
    private static final String[] SOBRENOMES = {
            "Silva", "Santos", "Oliveira", "Souza", "Pereira", "Costa", "Rodrigues", "Almeida", "Nascimento", "Lima",
            "Araújo", "Fernandes", "Carvalho", "Gomes", "Martins", "Rocha", "Ribeiro", "Barbosa", "Cardoso", "Teixeira"};
    private static final String[] OBJETIVOS = {
            "Ganho de massa", "Emagrecimento", "Condicionamento", "Saúde e bem-estar", "Reabilitação",
            "Flexibilidade", "Preparação para corrida", "Redução de estresse"};

    private final UnidadeRepository unidades;
    private final EsporteRepository esportes;
    private final PersonalTrainerRepository personais;
    private final ClienteRepository clientes;
    private final AulaRepository aulas;

    public CargaDemonstracao(UnidadeRepository unidades, EsporteRepository esportes,
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
        if (aulas.count() > 1 || clientes.count() >= 20) {
            System.out.println("Carga de demonstração ignorada: a base já possui dados.");
            return;
        }
        Random rnd = new Random(2026);

        criarUnidades();
        criarEsportes();
        criarPersonais();
        criarClientes(rnd);

        List<Unidade> todasUnidades = unidades.findAll();
        List<Esporte> esportesAtivos = esportes.findAll().stream()
                .filter(e -> e.getStatus() == StatusEsporte.ATIVO).toList();
        List<PersonalTrainer> todosPersonais = personais.findAll();
        List<Cliente> todosClientes = clientes.findAll();

        int criadas = criarAulas(rnd, esportesAtivos, todosPersonais, todasUnidades, todosClientes);

        System.out.printf("Carga de demonstração concluída: %d unidades, %d esportes, %d personais, %d clientes, %d aulas novas.%n",
                unidades.count(), esportes.count(), personais.count(), clientes.count(), criadas);
    }

    // ---------- cadastros

    private void criarUnidades() {
        garantirUnidade("Unidade Moema", "São Paulo", "SP");
        garantirUnidade("Unidade Vila Mariana", "São Paulo", "SP");
        garantirUnidade("Unidade Santo André", "Santo André", "SP");
        garantirUnidade("Unidade Campinas", "Campinas", "SP");
    }

    private void criarEsportes() {
        for (String nome : List.of("Pilates", "Yoga", "Crossfit", "Funcional", "Boxe", "Zumba")) {
            garantirEsporte(nome, StatusEsporte.ATIVO);
        }
        garantirEsporte("Hidroginástica", StatusEsporte.INATIVO);
    }

    private void criarPersonais() {
        garantirPersonal("Marcos Vinícius Prado", "Musculação e hipertrofia", 12, "140.00");
        garantirPersonal("Patrícia Nogueira", "Pilates", 9, "110.00");
        garantirPersonal("Thiago Bastos", "Crossfit", 6, "130.00");
        garantirPersonal("Larissa Duarte", "Yoga", 7, "100.00");
        garantirPersonal("Rodrigo Furtado", "Boxe e lutas", 10, "120.00");
        garantirPersonal("Camila Ribeiro", "Funcional", 5, "95.00");
        garantirPersonal("Fábio Moreira", "Spinning", 8, "105.00");
        garantirPersonal("Aline Cavalcanti", "Zumba e dança", 4, "85.00");
        garantirPersonal("Eduardo Tavares", "Reabilitação e mobilidade", 15, "150.00");
        garantirPersonal("Juliana Prates", "Emagrecimento", 3, "80.00");
    }

    private void criarClientes(Random rnd) {
        for (int i = 1; i <= TOTAL_CLIENTES; i++) {
            String nome = NOMES[rnd.nextInt(NOMES.length)];
            String sobrenome = SOBRENOMES[rnd.nextInt(SOBRENOMES.length)];
            String email = (semAcento(nome) + "." + semAcento(sobrenome) + i + "@example.com").toLowerCase();
            if (clientes.findByEmail(email).isPresent()) {
                continue;
            }
            Cliente c = new Cliente(nome + " " + sobrenome, email, OBJETIVOS[rnd.nextInt(OBJETIVOS.length)]);
            c.setDataCadastro(LocalDate.now().minusDays(rnd.nextInt(540)));
            clientes.save(c);
        }
    }

    // ---------- aulas

    private int criarAulas(Random rnd, List<Esporte> esportesAtivos, List<PersonalTrainer> todosPersonais,
                           List<Unidade> todasUnidades, List<Cliente> todosClientes) {
        // horários já ocupados por personal (inclui aulas que já existiam)
        Map<Long, List<LocalDateTime[]>> ocupacao = new HashMap<>();
        for (Aula existente : aulas.findAllByOrderByInicioAsc()) {
            registrar(ocupacao, existente.getPersonal().getId(), existente.getInicio(), existente.getFim());
        }

        LocalDateTime agora = LocalDateTime.now();
        int criadas = 0;
        int tentativas = 0;
        while (criadas < TOTAL_AULAS && tentativas < TOTAL_AULAS * 30) {
            tentativas++;
            Esporte esporte = esportesAtivos.get(rnd.nextInt(esportesAtivos.size()));
            PersonalTrainer personal = todosPersonais.get(rnd.nextInt(todosPersonais.size()));
            Unidade unidade = todasUnidades.get(rnd.nextInt(todasUnidades.size()));

            LocalDate dia = LocalDate.now().plusDays(rnd.nextInt(43) - 21L);
            LocalDateTime inicio = dia.atTime(6 + rnd.nextInt(16), 0);
            int duracao = DURACOES[rnd.nextInt(DURACOES.length)];
            int capacidade = 5 + rnd.nextInt(21);
            LocalDateTime fim = inicio.plusMinutes(duracao);

            if (conflita(ocupacao, personal.getId(), inicio, fim)) {
                continue;
            }
            registrar(ocupacao, personal.getId(), inicio, fim);

            boolean passada = inicio.isBefore(agora);
            Aula aula = new Aula(esporte, personal, unidade, inicio, duracao, capacidade);
            inscreverAleatorios(rnd, aula, todosClientes, passada);
            definirStatus(rnd, aula, passada);
            aulas.save(aula);
            criadas++;
        }
        return criadas;
    }

    private void inscreverAleatorios(Random rnd, Aula aula, List<Cliente> todosClientes, boolean passada) {
        int capacidade = aula.getCapacidade();
        int quantidade = passada
                ? (int) Math.round(capacidade * (0.4 + rnd.nextDouble() * 0.6))
                : rnd.nextInt(capacidade + 1);
        quantidade = Math.min(quantidade, todosClientes.size());

        List<Cliente> embaralhados = new ArrayList<>(todosClientes);
        Collections.shuffle(embaralhados, rnd);
        embaralhados.stream().limit(quantidade).forEach(aula::inscrever);
    }

    /** Passadas: 85% concluídas e 15% canceladas. Futuras: 90% agendadas e 10% canceladas. */
    private void definirStatus(Random rnd, Aula aula, boolean passada) {
        double sorteio = rnd.nextDouble();
        if (passada) {
            if (sorteio < 0.85) {
                aula.transitarPara(StatusAula.EM_ANDAMENTO);
                aula.transitarPara(StatusAula.CONCLUIDA);
            } else {
                aula.transitarPara(StatusAula.CANCELADA);
            }
        } else if (sorteio >= 0.90) {
            aula.transitarPara(StatusAula.CANCELADA);
        }
    }

    private boolean conflita(Map<Long, List<LocalDateTime[]>> ocupacao, Long personalId,
                             LocalDateTime inicio, LocalDateTime fim) {
        return ocupacao.getOrDefault(personalId, List.of()).stream()
                .anyMatch(o -> inicio.isBefore(o[1]) && o[0].isBefore(fim));
    }

    private void registrar(Map<Long, List<LocalDateTime[]>> ocupacao, Long personalId,
                           LocalDateTime inicio, LocalDateTime fim) {
        ocupacao.computeIfAbsent(personalId, k -> new ArrayList<>()).add(new LocalDateTime[]{inicio, fim});
    }

    // ---------- auxiliares (inserem somente se não existir, pela chave única)

    private void garantirUnidade(String nome, String cidade, String estado) {
        if (unidades.findByNome(nome).isEmpty()) {
            unidades.save(new Unidade(nome, cidade, estado));
        }
    }

    private void garantirEsporte(String nome, StatusEsporte status) {
        if (esportes.findByNome(nome).isEmpty()) {
            esportes.save(new Esporte(nome, status));
        }
    }

    private void garantirPersonal(String nome, String especialidade, int anos, String valorHora) {
        if (personais.findByNome(nome).isEmpty()) {
            personais.save(new PersonalTrainer(nome, especialidade, anos, new BigDecimal(valorHora)));
        }
    }

    private String semAcento(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
