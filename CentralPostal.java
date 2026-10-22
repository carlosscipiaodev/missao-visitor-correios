import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Missao Visitor: exemplo didatico, sem integracao com os Correios.
 * Java 17+: javac -encoding UTF-8 CentralPostal.java && java CentralPostal
 * Cada unidade e um volume independente. Tarifas ficticias, sem impostos.
 */
public class CentralPostal {
    record Destino(String cidade, String pais, double fator) {}

    interface Visitor {
        void visitDocumento(Documento item);
        void visitPacote(Pacote item);
        void visitFragil(Fragil item);
    }

    abstract static class Encomenda {
        final String nome;
        final double peso;
        final String origem;
        final String pais;

        Encomenda(String nome, double peso, String origem, String pais) {
            if (peso <= 0) throw new IllegalArgumentException("Peso deve ser positivo");
            this.nome = nome;
            this.peso = peso;
            this.origem = origem;
            this.pais = pais;
        }

        boolean internacional(Destino destino) {
            return !pais.equals(destino.pais());
        }

        abstract void accept(Visitor visitante);
    }

    static final class Documento extends Encomenda {
        Documento(String nome, double peso, String origem, String pais) {
            super(nome, peso, origem, pais);
        }

        @Override public void accept(Visitor visitante) {
            visitante.visitDocumento(this);
        }
    }

    static final class Pacote extends Encomenda {
        Pacote(String nome, double peso, String origem, String pais) {
            super(nome, peso, origem, pais);
        }

        @Override public void accept(Visitor visitante) {
            visitante.visitPacote(this);
        }
    }

    static final class Fragil extends Encomenda {
        Fragil(String nome, double peso, String origem, String pais) {
            super(nome, peso, origem, pais);
        }

        @Override public void accept(Visitor visitante) {
            visitante.visitFragil(this);
        }
    }

    record Cotacao(String produto, long centavos, String prazo) {}

    static final class FreteVisitor implements Visitor {
        final Destino destino;
        final List<Cotacao> cotacoes = new ArrayList<>();

        FreteVisitor(Destino destino) { this.destino = destino; }

        private void calcular(Encomenda e, double base, double porKg, double cuidado) {
            double valor = (base + e.peso * porKg + cuidado) * destino.fator()
                    + (e.internacional(destino) ? 65 : 0);
            String prazo = e.internacional(destino) ? "12-20 dias uteis" : "3-7 dias uteis";
            cotacoes.add(new Cotacao(e.nome, Math.round(valor * 100), prazo));
        }

        @Override public void visitDocumento(Documento e) { calcular(e, 8, 2, 0); }
        @Override public void visitPacote(Pacote e) { calcular(e, 14, 6, 0); }
        @Override public void visitFragil(Fragil e) { calcular(e, 18, 8, 9); }

        long totalCentavos() { return cotacoes.stream().mapToLong(Cotacao::centavos).sum(); }
    }

    record Etiqueta(String codigo, String produto, String origem, String destino,
                    String rota, String cuidado) {}

    static final class EtiquetaVisitor implements Visitor {
        final Destino destino;
        final List<Etiqueta> etiquetas = new ArrayList<>();

        EtiquetaVisitor(Destino destino) { this.destino = destino; }

        private void gerar(Encomenda e, String cuidado) {
            String codigo = "MV-001-%03d".formatted(etiquetas.size() + 1);
            etiquetas.add(new Etiqueta(codigo, e.nome, e.origem, destino.cidade(),
                    e.internacional(destino) ? "INTERNACIONAL" : "NACIONAL", cuidado));
        }

        @Override public void visitDocumento(Documento e) { gerar(e, "NAO DOBRAR"); }
        @Override public void visitPacote(Pacote e) { gerar(e, "VOLUME PADRAO"); }
        @Override public void visitFragil(Fragil e) { gerar(e, "FRAGIL - MANUSEAR COM CUIDADO"); }
    }

    record LinhaRelatorio(String nome, String tipo, double peso, String origem,
                          String rota, String manuseio) {}

    static final class RelatorioVisitor implements Visitor {
        final Destino destino;
        final List<LinhaRelatorio> linhas = new ArrayList<>();

        RelatorioVisitor(Destino destino) { this.destino = destino; }

        private void registrar(Encomenda e, String tipo, String manuseio) {
            linhas.add(new LinhaRelatorio(e.nome, tipo, e.peso, e.origem,
                    e.internacional(destino) ? "Internacional" : "Nacional", manuseio));
        }

        @Override public void visitDocumento(Documento e) {
            registrar(e, "Documento", "Envelope rigido");
        }
        @Override public void visitPacote(Pacote e) {
            registrar(e, "Pacote", "Embalagem padrao");
        }
        @Override public void visitFragil(Fragil e) {
            registrar(e, "Fragil", "Protecao reforcada");
        }
    }

    // O cliente apenas percorre os elementos: nao precisa de instanceof.
    static void visitar(List<Encomenda> pedido, Visitor visitante) {
        for (Encomenda item : pedido) {
            item.accept(visitante);
        }
    }

    // Strategy e opcional: troca o algoritmo de UMA tarefa.
    interface EstrategiaFrete { long calcular(List<Encomenda> pedido, Destino destino); }

    static final class FreteEconomico implements EstrategiaFrete {
        @Override public long calcular(List<Encomenda> pedido, Destino destino) {
            FreteVisitor frete = new FreteVisitor(destino);
            visitar(pedido, frete);
            return frete.totalCentavos();
        }
    }

    static final class FreteExpresso implements EstrategiaFrete {
        @Override public long calcular(List<Encomenda> pedido, Destino destino) {
            return Math.round(new FreteEconomico().calcular(pedido, destino) * 1.5);
        }
    }

    static final class Calculadora {
        private EstrategiaFrete estrategia;
        final Destino destino;

        Calculadora(EstrategiaFrete estrategia, Destino destino) {
            this.estrategia = estrategia;
            this.destino = destino;
        }

        void setEstrategia(EstrategiaFrete estrategia) { this.estrategia = estrategia; }
        long calcular(List<Encomenda> pedido) { return estrategia.calcular(pedido, destino); }
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.forLanguageTag("pt-BR"));
        Destino destino = new Destino("Fortaleza", "BR", 1.0);
        List<Encomenda> pedido = List.of(
                new Pacote("Livro", 0.6, "Sao Paulo - BR", "BR"),
                new Fragil("Caneca", 0.4, "Curitiba - BR", "BR"),
                new Documento("Documentos", 0.05, "Fortaleza - BR", "BR"),
                new Fragil("Camera", 0.8, "Toquio - JP", "JP")
        );

        FreteVisitor frete = new FreteVisitor(destino);
        EtiquetaVisitor etiqueta = new EtiquetaVisitor(destino);
        RelatorioVisitor relatorio = new RelatorioVisitor(destino);
        visitar(pedido, frete);
        visitar(pedido, etiqueta);
        visitar(pedido, relatorio);

        System.out.println("FRETE (SIMULACAO)");
        frete.cotacoes.forEach(c -> System.out.printf("%s: R$ %.2f | %s%n",
                c.produto(), c.centavos() / 100.0, c.prazo()));
        System.out.printf("Total: R$ %.2f%n%n", frete.totalCentavos() / 100.0);
        System.out.println("ETIQUETAS - SEM VALOR POSTAL");
        etiqueta.etiquetas.forEach(System.out::println);
        System.out.println("\nRELATORIO");
        relatorio.linhas.forEach(System.out::println);

        // As estrategias reutilizam o Visitor: os padroes podem coexistir.
        Calculadora calculadora = new Calculadora(new FreteEconomico(), destino);
        System.out.printf("%nStrategy economico: R$ %.2f%n", calculadora.calcular(pedido) / 100.0);
        calculadora.setEstrategia(new FreteExpresso());
        System.out.printf("Strategy expresso: R$ %.2f%n", calculadora.calcular(pedido) / 100.0);
    }
}
