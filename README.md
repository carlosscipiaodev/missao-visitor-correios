# Design Pattern Visitor (Visitante)

**Missão Visitor — Central de Encomendas**  
Projeto educacional de Carlos Vinicius.

[Jogar online](https://missao-visitor-correios.fabledpony9.chatgpt.site) · [Código Java completo](CentralPostal.java)

## O que é

Visitor é um padrão de projeto **comportamental** que separa operações dos objetos sobre os quais elas atuam. No contexto deste projeto, as encomendas representam os dados do pedido, enquanto visitantes diferentes calculam o frete, geram etiquetas e produzem relatórios.

Os elementos oferecem `accept(visitante)`. Cada visitante implementa uma operação para cada tipo de elemento. Depois que essa estrutura existe, é possível acrescentar outra operação criando um visitante, sem colocar a nova lógica nas classes das encomendas.

## O problema

Imagine uma central de encomendas inspirada no funcionamento dos Correios. Um pedido pode conter documentos, pacotes comuns e produtos frágeis. Além de armazenar informações dos volumes, o sistema precisa calcular fretes, gerar etiquetas e emitir um relatório.

Uma implementação direta poderia distribuir todas essas responsabilidades pelas classes:

```java
// Esquema da abordagem tradicional: assinaturas ilustrativas.
class Documento {
    // calcularFrete(), gerarEtiqueta(), gerarRelatorio()
}
class Pacote {
    // calcularFrete(), gerarEtiqueta(), gerarRelatorio()
}
class Fragil {
    // calcularFrete(), gerarEtiqueta(), gerarRelatorio()
}
```

Nessa organização, incluir uma quarta operação, como uma auditoria de embalagem, exigiria alterar cada uma das três classes. Também seria possível concentrar as operações em condicionais com `instanceof`, mas a identificação dos tipos acabaria repetida em diferentes partes do sistema.

O desafio deste exemplo é **acrescentar operações sobre tipos de encomenda que já existem**, mantendo a lógica de cada operação reunida. Não há uma contagem exponencial de subclasses como no problema das combinações de roupas do Decorator.

## A solução

O Visitor organiza o exemplo em três grupos:

| Papel | No projeto | Responsabilidade |
| --- | --- | --- |
| Elemento base | `Encomenda` | Compartilha dados e declara `accept(Visitor)` |
| Elementos concretos | `Documento`, `Pacote`, `Fragil` | Direcionam a visita ao método correspondente ao seu tipo |
| Contrato dos visitantes | `Visitor` | Declara `visitDocumento`, `visitPacote` e `visitFragil` |
| Visitantes concretos | `FreteVisitor`, `EtiquetaVisitor`, `RelatorioVisitor` | Implementam as três operações |
| Cliente | Central postal e lista do pedido | Escolhe o visitante e percorre os volumes |

No exemplo Java, o contrato `Visitor` é uma interface. `Encomenda` é uma classe abstrata, e as três categorias herdam dela.

O catálogo possui oito produtos, mas eles se agrupam em apenas três tipos de elemento:

| Produtos | Tipo |
| --- | --- |
| Documentos | `Documento` |
| Livro, camiseta, headphone e tênis | `Pacote` |
| Caneca, câmera e vaso | `Fragil` |

A origem internacional é uma informação da encomenda. Ela não exige uma classe `Internacional`: a rota depende da comparação entre o país de origem do volume e o país de destino.

## Exemplo em Java

Os trechos abaixo pertencem ao exemplo completo `CentralPostal.java`, disponível no projeto e no botão **Código completo** do jogo. Eles mostram a estrutura central; não são um programa independente.

### Contrato dos visitantes

```java
interface Visitor {
    void visitDocumento(Documento item);
    void visitPacote(Pacote item);
    void visitFragil(Fragil item);
}
```

### Aceitação em um elemento concreto

```java
static final class Pacote extends Encomenda {
    Pacote(String nome, double peso, String origem, String pais) {
        super(nome, peso, origem, pais);
    }

    @Override
    public void accept(Visitor visitante) {
        visitante.visitPacote(this);
    }
}
```

`Documento` e `Fragil` seguem a mesma estrutura, chamando `visitDocumento(this)` e `visitFragil(this)`.

### Regras específicas dentro de FreteVisitor

```java
@Override public void visitDocumento(Documento e) {
    calcular(e, 8, 2, 0);
}
@Override public void visitPacote(Pacote e) {
    calcular(e, 14, 6, 0);
}
@Override public void visitFragil(Fragil e) {
    calcular(e, 18, 8, 9);
}
```

Esses métodos são membros de `FreteVisitor`. O método auxiliar `calcular` recebe o valor base, o preço por quilograma e o adicional de cuidado. Ele aplica o fator do destino e um adicional fictício de R$ 65,00 quando os países de origem e destino são diferentes.

### Aplicando visitantes ao mesmo pedido

```java
static void visitar(List<Encomenda> pedido, Visitor visitante) {
    for (Encomenda item : pedido) {
        item.accept(visitante);
    }
}

// Dentro de main():
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
```

O método `visitar` permanece igual para as três operações. Neste Java, os métodos do visitante retornam `void` e acumulam seus resultados em listas internas. O cliente consulta essas listas ao final. Na implementação JavaScript do jogo, as visitas retornam resultados que a interface reúne para exibição.

## Saída do programa

Ao executar o exemplo Java completo com o pedido acima, a seção de frete apresenta:

```text
FRETE (SIMULACAO)
Livro: R$ 17,60 | 3-7 dias uteis
Caneca: R$ 30,20 | 3-7 dias uteis
Documentos: R$ 8,10 | 3-7 dias uteis
Camera: R$ 98,40 | 12-20 dias uteis
Total: R$ 154,30
```

Os dois primeiros volumes, livro e caneca, formam o pedido inicial do jogo e somam R$ 47,80. Ao adicionar documentos e câmera, mantendo Fortaleza como destino, o total passa a R$ 154,30.

O programa também imprime as etiquetas e as linhas do relatório. O visitante de etiquetas atribui cuidados diferentes: documentos recebem “não dobrar”; pacotes, “volume padrão”; itens frágeis, “manusear com cuidado”.

Todos os valores e prazos são inventados para o exercício. Cada unidade é um volume independente. O total soma os fretes dos volumes, sem consolidação de embalagens e sem tributos de importação.

## Como funcionam accept(), visit() e Double Dispatch

Quando o cliente executa `item.accept(visitante)`, a implementação de `accept` é escolhida pelo tipo real da encomenda. Se `item` referencia um `Pacote`, o método executado é o de `Pacote`.

Dentro desse método, a chamada `visitante.visitPacote(this)` encaminha o pacote ao visitante. A implementação executada depende do visitante concreto recebido: `FreteVisitor`, `EtiquetaVisitor` ou `RelatorioVisitor`.

Assim, o comportamento depende de **dois tipos**: o da encomenda e o do visitante. Essa colaboração caracteriza o Double Dispatch do exemplo. Os métodos receberam nomes explícitos, como `visitPacote`; não é necessário que todos se chamem literalmente `visit`.

| Encomenda | Visitante recebido | Operação executada |
| --- | --- | --- |
| `Pacote` | `FreteVisitor` | Calcula a tarifa de pacote |
| `Pacote` | `EtiquetaVisitor` | Produz a identificação de volume padrão |
| `Fragil` | `FreteVisitor` | Calcula a tarifa com cuidado adicional |
| `Fragil` | `EtiquetaVisitor` | Produz a identificação de item frágil |

A lista pode armazenar todos os elementos pelo tipo base `Encomenda`. O cliente não precisa testar cada tipo com `instanceof` para selecionar a operação.

## O que o exemplo mostra

As encomendas continuam representando os mesmos produtos enquanto visitantes diferentes realizam tarefas sobre elas. No jogo, esses visitantes apenas consultam os elementos: calcular frete não altera seu peso, e gerar uma etiqueta não transforma o tipo do objeto.

Uma nova operação, como `AuditoriaVisitor`, pode implementar os três métodos do contrato existente. Os elementos que já oferecem `accept` não precisam receber um método de auditoria.

Já um novo tipo, como `Perecivel`, exige outra mudança: acrescentar `visitPerecivel` à interface e implementá-lo nos visitantes existentes. Essa assimetria é um dos principais critérios para escolher o padrão.

## Relação com o exemplo do Refactoring.Guru

No exemplo Java do Refactoring.Guru, elementos representam formas geométricas, e um visitante exporta suas informações para XML. A lógica de exportação fica fora das classes das formas.

Missão Visitor adapta essa organização para uma central postal. As formas são substituídas por documentos, pacotes e itens frágeis; a exportação é substituída por cálculo de frete, etiquetas e relatórios. A correspondência é didática: o cenário dos Correios foi criado para este projeto e não é o exemplo original do site.

## Breve explicação sobre o jogo

**Missão Visitor — Central de Encomendas** é um laboratório educacional interativo. O jogador monta um pedido com produtos de diferentes origens, ajusta quantidades e escolhe o destino comum dos volumes.

A central possui três operações:

- **Frete:** calcula a tarifa de cada volume e apresenta o total e as regras aplicadas.
- **Etiqueta:** gera uma identificação didática para cada volume, com origem, destino e cuidados de manuseio.
- **Relatório:** reúne quantidade, peso, categorias, rotas e necessidades de embalagem.

Uma esteira animada acompanha a execução em três etapas: `accept(visitante)`, chamada ao método específico do visitante e resultado. O painel de código destaca os trechos correspondentes.

O jogador pode concluir quatro missões: montar um pedido com pelo menos dois tipos de elemento e executar os três visitantes. Ao alterar produtos, quantidades ou destino, os resultados anteriores e as missões de operação são reiniciados, para que reflitam o pedido atual.

O jogo oferece oito produtos, cinco destinos, limite de vinte volumes, velocidade de animação ajustável, exportação dos resultados e download do HTML para uso offline. Também apresenta comparações conceituais com a abordagem tradicional e com Strategy; essas comparações não são modos separados de execução do simulador.

A interface é feita com HTML, CSS e JavaScript. O material Java 17 é uma implementação didática complementar, executável no terminal; o navegador não executa esse código Java.

## Comparação com Strategy

Visitor e Strategy são padrões comportamentais, mas organizam variações diferentes. Neste projeto, a distinção pode ser observada nas perguntas que cada um ajuda a responder:

| Aspecto | Visitor | Strategy |
| --- | --- | --- |
| Pergunta | Qual operação aplicar a cada tipo de encomenda? | Qual algoritmo utilizar para calcular o frete? |
| Variações do exemplo | Frete, etiqueta e relatório | Econômico e expresso |
| Colaboração | `accept` encaminha o elemento ao visitante | A calculadora delega à estratégia selecionada |
| Critério principal | Operações diferentes sobre tipos conhecidos | Algoritmos intercambiáveis para uma tarefa |

O código Java inclui uma demonstração complementar:

```java
Calculadora calculadora = new Calculadora(new FreteEconomico(), destino);
long economico = calculadora.calcular(pedido);

calculadora.setEstrategia(new FreteExpresso());
long expresso = calculadora.calcular(pedido);
```

No exercício, o expresso aplica um multiplicador fictício de 1,5 sobre o total econômico. Para o pedido de R$ 154,30, ele resulta em R$ 231,45. Essa seleção existe na demonstração Java; a interface do jogo não oferece modalidades comerciais de envio.

Os padrões também podem coexistir: no exemplo Java, as estratégias reutilizam o `FreteVisitor` para obter o cálculo por tipo de encomenda.

## Por que usar Visitor neste exemplo?

- Reunir as regras de uma operação em um visitante.
- Aplicar operações a uma lista com tipos diferentes de encomenda.
- Acrescentar operações sem distribuir novos métodos pelos elementos já preparados.
- Acumular resultados durante a visita aos volumes.
- Mostrar explicitamente a colaboração entre objetos e visitantes.

## Quando usar

O cenário é favorável quando os tipos de elemento mudam pouco, mas novas operações aparecem com frequência. Na central postal, isso significa manter as categorias Documento, Pacote e Fragil enquanto surgem novos relatórios, verificações ou formas de exportação.

A mesma organização pode ser aplicada, conforme o problema, ao processamento de nós de uma árvore de sintaxe, à exportação de documentos ou a operações sobre diferentes elementos de uma estrutura.

## Cuidados

Introduzir Visitor em classes que ainda não possuem esse suporte exige uma adaptação inicial para disponibilizar `accept`. A possibilidade de acrescentar operações sem alterar os elementos vale depois que essa colaboração está estabelecida.

Adicionar um tipo de encomenda exige atualizar o contrato e os visitantes. Portanto, o padrão pode ser trabalhoso quando a hierarquia de elementos muda com frequência.

Visitantes também precisam acessar os dados necessários às suas operações. Em uma aplicação real, isso deve ser organizado com interfaces de consulta e cuidado com o encapsulamento.

Os visitantes Java deste projeto acumulam resultados. Eles são criados novamente para cada operação; reutilizar a mesma instância sem limpar suas listas somaria visitas anteriores.

O Visitor não garante, por si só, que um elemento nunca será alterado. A ausência de alterações nos dados das encomendas é uma decisão desta implementação.

Em problemas pequenos, com poucas operações estáveis, a estrutura adicional pode ser desnecessária. E os cálculos, etiquetas e códigos visuais deste jogo não substituem serviços postais reais.

## Conclusão

Missão Visitor transforma uma central de encomendas em uma demonstração prática: o pedido reúne objetos de diferentes tipos, e cada visitante realiza uma operação sobre essa mesma lista.

O ponto central é separar os dados das encomendas das operações de frete, etiqueta e relatório. A animação e o código mostram essa colaboração enquanto o jogador escolhe os produtos e acompanha os resultados.

## Acesso ao projeto

- [Jogar online — Missão Visitor](https://missao-visitor-correios.fabledpony9.chatgpt.site)
- [Consultar a implementação Java completa](CentralPostal.java)
- Para jogar offline, baixe `index.html` e abra-o em um navegador. Os estilos, ilustrações e scripts estão incorporados ao HTML.
- Para executar o exemplo Java 17 ou superior: `java CentralPostal.java`.

## Referências

- Refactoring.Guru. [Visitor](https://refactoring.guru/pt-br/design-patterns/visitor).
- Refactoring.Guru. [Visitor em Java: exportando formas para XML](https://refactoring.guru/pt-br/design-patterns/visitor/java/example).
- Refactoring.Guru. [Visitor and Double Dispatch](https://refactoring.guru/design-patterns/visitor-double-dispatch).
- Refactoring.Guru. [Strategy](https://refactoring.guru/pt-br/design-patterns/strategy).

**Observação:** o ChatGPT foi utilizado como ferramenta de apoio no desenvolvimento do protótipo, na organização da explicação e na revisão do texto.
