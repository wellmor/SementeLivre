```mermaid
classDiagram
    direction TB

    class Pessoa {
        -String idPessoa
        -String tipoDocumento
        -String documento
        -String nome
        -String telefone
        -String email
        -String senhaHash
        -Logradouro logradouro
        -DateTime dataCadastro
        -DateTime dataUltimaAlteracao
        +cadastrar() void
        +alterar() void
        +excluir() void
        +validarEmailUnico(email: String) Boolean
        +validarDocumentoUnico(documento: String) Boolean
        +alterarSenha(senhaAtual: String, novaSenha: String) Boolean
    }

    class Usuario {
        -String idUsuario
        +consultar(parametros: Map) List~Usuario~
    }

    class Proprietario {
        -String idProprietario
        -String rg
        -Boolean exibirNoSitePublico
        +consultar(parametros: Map) List~Proprietario~
        +validarRGUnico(rg: String) Boolean
    }

    class Admin {
        -String idAdmin
        -String nivelAcesso
        +aprovarComunidade(id: String) void
        +recusarComunidade(id: String) void
        +listarComunidadesPendentes() List~Comunidade~
    }

    class Logradouro {
        -String logradouro
        -String numero
        -String complemento
        -String bairro
        -String municipio
        -String uf
        -String cep
    }

    class Comunidade {
        -String idComunidade
        -String nome
        -Logradouro logradouro
        -StatusComunidade status
        -DateTime dataSolicitacao
        -DateTime dataAprovacao
        +cadastrar() void
        +aprovar() void
        +rejeitar() void
        +listar(parametros: Map) List~Comunidade~
        +verificarSimilaridade(nome: String) Boolean
    }

    class StatusComunidade {
        <<enumeration>>
        ATIVA
        PENDENTE_APROVACAO
        REJEITADA
    }

    class Propriedade {
        -String idPropriedade
        -String nome
        -Double tamanhoHectares
        -Logradouro logradouro
        -DateTime dataCadastro
        -DateTime dataUltimaAlteracao
        +cadastrar() void
        +alterar() void
        +excluir() void
        +consultar(parametros: Map) List~Propriedade~
        +verificarDependencias() Boolean
    }

    class Produto {
        -String idProduto
        -String nomePopular
        -String nomeCientifico
        -String historico
        -String urlFoto
        -TipoProduto tipo
        -EspecieGeral especie
        -FormatoProduto tipoProduto
        -DateTime dataInclusao
        -DateTime dataUltimaAlteracao
        +cadastrar() void
        +consultar(parametros: Map) List~Produto~
        +uploadFoto(foto: Bytes) String
        +validarCamposObrigatorios() Boolean
    }

    class TipoProduto {
        <<enumeration>>
        HORTALICA
        FRUTIFERA
        FORRAGEIRA
        CEREAL
        LEGUMINOSA
        OUTRAS
    }

    class EspecieGeral {
        <<enumeration>>
        FEIJAO
        MILHO
        ABOBORA
        ALFACE
        ARROZ
        CEBOLA
        ALHO
        OUTRAS
    }

    class FormatoProduto {
        <<enumeration>>
        MUDA
        SEMENTE
    }

    class Pesagem {
        <<enumeration>>
        SACA
        KG
        GRAMA
        MG
        UNIDADE
    }

    class DisponibilidadeProduto {
        <<enumeration>>
        PARA_TROCA
        PARA_VENDA
        PARA_DOACAO
        INDISPONIVEL
    }

    class Estoque {
        -String idEstoque
        -String idProprietario
        -String idProduto
        -String descricao
        -Double preco
        -Double quantidade
        -Pesagem tipoPesagem
        -DisponibilidadeProduto disponibilidade
        -TipoMovimentacao tipo
        -DateTime dataMovimentacao
        -DateTime dataUltimaAtualizacaoEstoque
        +cadastrarEstoque(qtd: Double, unidade: String) void
        +alterarEstoque(delta: Double) void
        +zerarEstoque() void
        +verificarDisponibilidade(qtd: Double) Boolean
        +recalcularAposPedido(qtd: Double, operacao: String) void
    }

    class TipoMovimentacao {
        <<enumeration>>
        ENTRADA
        SAIDA_VENDA
        SAIDA_TROCA
        SAIDA_DOACAO
        CORRECAO
        ZERAMENTO
    }

    class Pedido {
        -String idPedido
        -TipoPedido tipoPedido
        -String mensagemOpcional
        -DateTime dataPedido
        -StatusPedido status
        +cadastrar() void
        +alterar() void
        +excluir() void
        +consultar(parametros: Map) List~Pedido~
        +validarEstoque() Boolean
        +atualizarEstoque() void
    }

    class Itens {
        -String idItem
        -Double quantidade
        -Double precoUnitario
    }

    class TipoPedido {
        <<enumeration>>
        VENDA
        TROCA
        DOACAO
    }

    class StatusPedido {
        <<enumeration>>
        PENDENTE
        CONFIRMADO
        CANCELADO
    }

    class Notificacao {
        -String idNotificacao
        -String titulo
        -String mensagem
        -Boolean lida
        -DateTime dataGeracao
        -DateTime dataLeitura
        +gerar(pedido: Pedido) Notificacao
        +marcarComoLida() void
        +listarHistorico(idProprietario: String) List~Notificacao~
        +acessarPedidoRelacionado() Pedido
    }

    class Relatorio {
        -String idRelatorio
        -TipoRelatorio tipo
        -Map filtrosUtilizados
        -DateTime dataGeracao
        -List dados
        +processarDados() void
    }

    class TipoRelatorio {
        <<enumeration>>
        ESTOQUE_SEMENTES
        PEDIDOS_REALIZADOS
    }
    
    %% Herança Papéis (Sem Pessoa Física e Jurídica)
    Pessoa <|-- Usuario
    Pessoa <|-- Proprietario
    Pessoa <|-- Admin

    %% Logradouro
    Pessoa --> Logradouro : possui localização
    Propriedade --> Logradouro : possui localização
    Comunidade --> Logradouro : possui localização

    %% Proprietario
    Proprietario "1" --> "1..*" Propriedade : gerencia
    Propriedade "1..*" --> "1" Comunidade : pertence
    Comunidade --> StatusComunidade : status

    %% Admin
    Admin --> Comunidade : gerencia aprovação

    %% Produto e Estoque (Estoque como elo central)
    Proprietario "1" --> "0..*" Estoque : gerencia ofertas
    Estoque "0..*" --> "1" Produto : refere-se a
    Produto "0..*" --> "1" Comunidade : origem

    Produto --> TipoProduto : classificada
    Produto --> EspecieGeral : especie
    Produto --> FormatoProduto : formato
    Estoque --> DisponibilidadeProduto : status
    Estoque --> Pesagem : pesada em
    Estoque --> TipoMovimentacao : tipo

    %% Pedido
    Pedido "1" *-- "1..*" Itens : contem
    Itens "0..*" --> "1" Produto : refere-se a
    Pedido "0..*" --> "1" Usuario : realizado por
    Pedido "0..*" --> "1" Proprietario : recebido por
    Pedido --> TipoPedido : tipo
    Pedido --> StatusPedido : status
    Pedido "1" --> "0..*" Notificacao : dispara alerta
    Notificacao "0..*" --> "1" Proprietario : pertence a

    %% Relatorio
    Proprietario "1" --> "0..*" Relatorio : solicita
    Relatorio --> TipoRelatorio : classificado por
```