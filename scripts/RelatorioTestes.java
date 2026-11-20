import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;
import org.w3c.dom.*;

/**
 * Lê os relatórios do Surefire (target/surefire-reports/TEST-*.xml) e gera um registro em Markdown
 * com o resultado REAL da última execução dos testes.
 *
 * Uso (Java 21, na raiz do projeto):  java scripts/RelatorioTestes.java
 * Saída: docs/evidencias/RESULTADOS_TESTES.md
 */
public class RelatorioTestes {

    record Resultado(String classe, int testes, int falhas, int erros, int ignorados, double segundos) {
    }

    public static void main(String[] args) throws Exception {
        Path pasta = Path.of("target", "surefire-reports");
        if (!Files.isDirectory(pasta)) {
            System.err.println("Pasta target/surefire-reports nao encontrada. Rode antes: ./mvnw test");
            System.exit(1);
        }

        List<Resultado> resultados = new ArrayList<>();
        List<String> problemas = new ArrayList<>();
        DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
        fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder construtor = fabrica.newDocumentBuilder();

        try (Stream<Path> arquivos = Files.list(pasta)) {
            for (Path xml : arquivos.filter(p -> p.getFileName().toString().matches("TEST-.*\\.xml")).sorted().toList()) {
                Element suite = construtor.parse(xml.toFile()).getDocumentElement();
                String classe = suite.getAttribute("name");
                resultados.add(new Resultado(classe,
                        inteiro(suite, "tests"), inteiro(suite, "failures"), inteiro(suite, "errors"),
                        inteiro(suite, "skipped"), decimal(suite, "time")));

                NodeList casos = suite.getElementsByTagName("testcase");
                for (int i = 0; i < casos.getLength(); i++) {
                    Element caso = (Element) casos.item(i);
                    for (String tipo : new String[]{"failure", "error"}) {
                        NodeList falhas = caso.getElementsByTagName(tipo);
                        if (falhas.getLength() > 0) {
                            String msg = ((Element) falhas.item(0)).getAttribute("message");
                            problemas.add("`" + classe + "." + caso.getAttribute("name") + "` — "
                                    + tipo + ": " + (msg.length() > 200 ? msg.substring(0, 200) + "…" : msg));
                        }
                    }
                }
            }
        }

        int testes = resultados.stream().mapToInt(Resultado::testes).sum();
        int falhas = resultados.stream().mapToInt(Resultado::falhas).sum();
        int erros = resultados.stream().mapToInt(Resultado::erros).sum();
        int ignorados = resultados.stream().mapToInt(Resultado::ignorados).sum();
        double tempo = resultados.stream().mapToDouble(Resultado::segundos).sum();
        boolean aprovado = falhas == 0 && erros == 0 && testes > 0;

        StringBuilder md = new StringBuilder();
        md.append("# Resultados da execução dos testes\n\n");
        md.append("> Arquivo gerado automaticamente por `scripts/RelatorioTestes.java` a partir dos relatórios do Surefire. ")
                .append("Não edite à mão: rode os testes novamente para atualizar.\n\n");
        md.append("| Item | Valor |\n|---|---|\n");
        md.append("| Data e hora | ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append(" |\n");
        md.append("| Java | ").append(System.getProperty("java.version")).append(" (").append(System.getProperty("java.vendor")).append(") |\n");
        md.append("| Sistema operacional | ").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.version")).append(" |\n");
        md.append("| **Resultado** | **").append(aprovado ? "APROVADO" : "COM FALHAS").append("** |\n");
        md.append("| Testes executados | ").append(testes).append(" |\n");
        md.append("| Falhas / Erros / Ignorados | ").append(falhas).append(" / ").append(erros).append(" / ").append(ignorados).append(" |\n");
        md.append("| Tempo total | ").append(String.format(Locale.ROOT, "%.1f s", tempo)).append(" |\n\n");

        md.append("## Por classe de teste\n\n| Classe | Testes | Falhas | Erros | Ignorados | Tempo (s) |\n|---|---:|---:|---:|---:|---:|\n");
        for (Resultado r : resultados) {
            md.append("| `").append(r.classe().replace("br.com.academiafitness.", "")).append("` | ")
                    .append(r.testes()).append(" | ").append(r.falhas()).append(" | ").append(r.erros()).append(" | ")
                    .append(r.ignorados()).append(" | ").append(String.format(Locale.ROOT, "%.2f", r.segundos())).append(" |\n");
        }
        md.append("| **Total** | **").append(testes).append("** | **").append(falhas).append("** | **").append(erros)
                .append("** | **").append(ignorados).append("** | **").append(String.format(Locale.ROOT, "%.2f", tempo)).append("** |\n");

        if (!problemas.isEmpty()) {
            md.append("\n## Testes com problema\n\n");
            problemas.forEach(p -> md.append("- ").append(p).append("\n"));
        }

        Path jacoco = Path.of("target", "site", "jacoco", "index.html");
        if (Files.exists(jacoco)) {
            md.append("\nRelatório de cobertura (JaCoCo): `target/site/jacoco/index.html` (gerado com `-Pcobertura`).\n");
        }

        Path saida = Path.of("docs", "evidencias", "RESULTADOS_TESTES.md");
        Files.createDirectories(saida.getParent());
        Files.writeString(saida, md.toString(), StandardCharsets.UTF_8);
        System.out.println("Relatorio gerado em " + saida + " - " + (aprovado ? "APROVADO" : "COM FALHAS")
                + " (" + testes + " testes, " + falhas + " falhas, " + erros + " erros)");
        if (!aprovado) {
            System.exit(2);
        }
    }

    private static int inteiro(Element e, String atributo) {
        String v = e.getAttribute(atributo);
        return v.isEmpty() ? 0 : Integer.parseInt(v);
    }

    private static double decimal(Element e, String atributo) {
        String v = e.getAttribute(atributo).replace(",", "");
        return v.isEmpty() ? 0 : Double.parseDouble(v);
    }
}
