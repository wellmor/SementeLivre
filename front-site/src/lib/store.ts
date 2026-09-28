import type {
  Proprietario, Property, Plantio,
  Species, Estoque, Colheita,
  Comunidade, SolicitacaoCadastro, ContaProdutor,
  Pedido, Notificacao,
} from "./types";

interface DbSchema {
  proprietario: Proprietario & { senha: string };
  properties: Property[];
  plantios: Plantio[];
  species: Species[];
  estoque: Estoque[];
  colheitas: Colheita[];
  comunidades: Comunidade[];
  solicitacoes: SolicitacaoCadastro[];
  contasProdutores: ContaProdutor[];
  pedidos: Pedido[];
  notificacoes: Notificacao[];
}

declare global {
  // eslint-disable-next-line no-var
  var __sementesDb_v7: DbSchema | undefined;
}

// Key is versioned — bump when schema changes to avoid stale cached objects
const db: DbSchema =
  globalThis.__sementesDb_v7 ??
  (globalThis.__sementesDb_v7 = {
    proprietario: {
      nome: "Administrador",
      telefone: "(11) 99999-9999",
      cpf: "123.456.789-00",
      senha: "adm1234",
    },
    properties: [
      {
        id_propriedade: "prop-1",
        nome: "Sítio Boa Esperança",
        endereco: "Estrada Municipal km 5, Quilombo dos Coelhos",
        area_total: 12.5,
      },
      {
        id_propriedade: "prop-2",
        nome: "Chácara São João",
        endereco: "Rua das Mangueiras, 45, Quilombo dos Coelhos",
        area_total: 8.0,
      },
    ],
    plantios: [
      {
        id_plantio: "plant-1",
        id_propriedade: "prop-1",
        id_especie: "esp-1",
        data_inicio: "2026-03-10",
        previsao_colheita: "2026-07-15",
        area_plantada: 3.0,
        talhao: "Talhão A",
        status: "ativo",
      },
      {
        id_plantio: "plant-2",
        id_propriedade: "prop-2",
        id_especie: "esp-2",
        data_inicio: "2025-11-01",
        previsao_colheita: "2026-02-20",
        area_plantada: 5.0,
        talhao: "Talhão Norte",
        status: "concluido",
      },
    ],
    species: [
      {
        id_especie: "esp-1",
        nome_popular: "Feijão Crioulo",
        nome_cientifico: "Phaseolus vulgaris",
        familia_botanica: "Fabaceae",
        descricao:
          "Variedade tradicional de feijão cultivada há gerações no quilombo, adaptada ao clima local e com alto valor nutricional.",
        foto: "/sementes/feijao-crioulo.png",
        status: "exchange",
        id_comunidade: "com-1",
        tipoSemente: "LEGUMINOSA",
        quantidadeEstoque: 50,
        preco: 15,
        unidadePesagem: "KG",
      },
      {
        id_especie: "esp-2",
        nome_popular: "Milho Caiano",
        nome_cientifico: "Zea mays",
        familia_botanica: "Poaceae",
        descricao:
          "Milho crioulo de ciclo curto, resistente à seca e com grãos amarelo-claros ideais para fubá e canjica.",
        foto: "/sementes/milho-caiano.png",
        status: "sale",
        id_comunidade: "com-1",
        tipoSemente: "CEREAL",
        quantidadeEstoque: 30,
        preco: 20,
        unidadePesagem: "KG",
      },
      {
        id_especie: "esp-3",
        nome_popular: "Abóbora Cabotiá",
        nome_cientifico: "Cucurbita maxima",
        familia_botanica: "Cucurbitaceae",
        descricao:
          "Abóbora de polpa firme e adocicada, excelente para doces e pratos salgados. Produção abundante na região.",
        foto: "/sementes/abobora-cabotia.png",
        status: "donation",
        id_comunidade: "com-1",
        tipoSemente: "HORTALICA",
        quantidadeEstoque: 100,
        unidadePesagem: "UNIDADE",
      },
      {
        id_especie: "esp-4",
        nome_popular: "Quiabo Vermelho",
        nome_cientifico: "Abelmoschus esculentus",
        familia_botanica: "Malvaceae",
        descricao:
          "Variedade rara de quiabo com frutos avermelhados, rica em antioxidantes. Estoque temporariamente esgotado.",
        foto: "/sementes/quiabo-vermelho.png",
        status: "unavailable",
        id_comunidade: "com-1",
        tipoSemente: "HORTALICA",
        quantidadeEstoque: 0,
        unidadePesagem: "KG",
      },
      {
        id_especie: "esp-5",
        nome_popular: "Maxixe do Norte",
        nome_cientifico: "Cucumis anguria",
        familia_botanica: "Cucurbitaceae",
        descricao:
          "Fruto nativo de sabor levemente amargo, muito usado em saladas e refogados na culinária regional nordestina.",
        foto: "/sementes/maxixe-do-norte.png",
        status: "sale",
        id_comunidade: "com-2",
        tipoSemente: "HORTALICA",
        quantidadeEstoque: 80,
        preco: 5,
        unidadePesagem: "KG",
      },
    ],
    estoque: [],
    colheitas: [],

    // ── Multi-community ────────────────────────────────────────────────────
    comunidades: [
      {
        id_comunidade: "com-1",
        nome: "Quilombo dos Coelhos",
        localizacao: "Pernambuco - PE",
        status: "ativa",
      },
      {
        id_comunidade: "com-2",
        nome: "Quilombo Terra Livre",
        localizacao: "Bahia - BA",
        status: "ativa",
      },
      {
        id_comunidade: "com-3",
        nome: "Comunidade Zumbi dos Palmares",
        localizacao: "Alagoas - AL",
        status: "ativa",
      },
    ],
    solicitacoes: [
      {
        id_solicitacao: "sol-1",
        nome_responsavel: "Maria Santos",
        email: "maria@quilomboterra.org",
        senha: "terra1234",
        nome_comunidade: "Quilombo Terra Livre",
        localizacao: "Bahia - BA",
        documento_nome: "certidao_terra_livre.pdf",
        documento_base64: "",
        status: "aprovada",
        data_solicitacao: "2026-05-20",
        observacao: "",
      },
      {
        id_solicitacao: "sol-2",
        nome_responsavel: "João Palmares",
        email: "joao@zumbi.org",
        senha: "zumbi1234",
        nome_comunidade: "Comunidade Zumbi dos Palmares",
        localizacao: "Alagoas - AL",
        documento_nome: "reconhecimento_palmares.pdf",
        documento_base64: "",
        status: "aprovada",
        data_solicitacao: "2026-06-03",
        observacao: "",
      },
      {
        id_solicitacao: "sol-3",
        nome_responsavel: "Ana Conceição",
        email: "ana@kalunga.org",
        senha: "kalunga1234",
        nome_comunidade: "Quilombo Kalunga",
        localizacao: "Goiás - GO",
        documento_nome: "portaria_kalunga.pdf",
        documento_base64: "",
        status: "pendente",
        data_solicitacao: "2026-06-07",
        observacao: "",
      },
    ],
    contasProdutores: [
      {
        id_conta: "conta-1",
        email: "maria@quilomboterra.org",
        senha: "terra1234",
        nome: "Maria Santos",
        id_comunidade: "com-2",
      },
      {
        id_conta: "conta-2",
        email: "joao@zumbi.org",
        senha: "zumbi1234",
        nome: "João Palmares",
        id_comunidade: "com-3",
      },
    ],
    pedidos: [
      {
        id_pedido: "ped-demo-1",
        id_especie: "esp-1",
        id_comunidade: "com-3",
        tipoPedido: "TROCA",
        status: "PENDENTE",
        nomeRecebedor: "João Palmares",
        contatoRecebedor: "(82) 98888-7777",
        mensagemOpcional: "Gostaríamos de trocar por sementes de abacá",
        quantidade: 5,
        dataPedido: "2026-06-05T10:00:00.000Z",
      },
    ],
    notificacoes: [],
  });

export { db };

