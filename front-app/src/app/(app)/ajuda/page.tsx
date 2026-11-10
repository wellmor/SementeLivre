'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { ArrowLeft, ChevronDown, CircleHelp } from 'lucide-react';
import legal from '../legal.module.css';
import styles from './ajuda.module.css';

interface Topico {
  id: string;
  titulo: string;
  passos: string[];
  nota?: string;
}

const TOPICOS: Topico[] = [
  {
    id: 'entrar',
    titulo: 'Entrar, criar conta e recuperar a senha',
    passos: [
      'Na tela inicial, preencha E-mail e Senha e toque em Entrar. O botão do olho mostra ou esconde a senha.',
      'Não tem conta? Toque em Cadastre-se e preencha Dados Pessoais, Acesso e Endereço. Ao digitar o CEP, o endereço é preenchido sozinho. No fim, toque em Criar Conta e entre com o e-mail e a senha que você escolheu.',
      'Esqueceu a senha? Toque em Esqueceu sua senha?, informe o E-mail cadastrado e toque em Enviar código de recuperação. Depois digite o Código recebido por e-mail, a Nova senha e a Confirmar nova senha, e toque em Redefinir senha.',
    ],
    nota: 'Com a conta aberta, você também pode trocar a senha em Perfil, no botão Alterar Senha.',
  },
  {
    id: 'propriedades',
    titulo: 'Propriedades e comunidade',
    passos: [
      'Toque em Propriedades no menu de baixo e depois em Nova Propriedade.',
      'Preencha o Nome da propriedade, o Tamanho (hectares), a Comunidade e o Endereço. Toque em Cadastrar Propriedade.',
      'Sua comunidade não aparece na lista? Use Solicitar Nova Comunidade: informe o Nome da Comunidade, a Localidade, o Município e a UF e toque em Enviar Solicitação. Na propriedade, a comunidade aparece como Ativa, Pendente de aprovação ou Rejeitada.',
      'Para ver os dados de uma propriedade, toque nela na lista. Para apagar, use Excluir propriedade.',
    ],
    nota: 'Se a propriedade ainda tem algo ligado a ela, o sistema avisa "Exclusão bloqueada" e mostra o motivo.',
  },
  {
    id: 'produtos',
    titulo: 'Cadastrar produtos (sementes e mudas)',
    passos: [
      'Toque em Produtos no menu de baixo e depois no botão de cadastrar novo produto.',
      'Adicione a Foto do Produto (a foto é obrigatória) e informe o Nome popular. Se quiser, conte a história em Histórico / Descrição.',
      'Em Classificação, escolha o Tipo, a Espécie e o Formato.',
      'Em Estoque Inicial, informe a Quantidade, a Unidade e a Disponibilidade (Para Troca, Para Venda, Para Doação, A Negociar ou Indisponível). Marque Aceito propostas se quiser que quem pede pelo site possa propor venda, troca ou doação.',
      'Toque em Cadastrar Produto. Na lista, use a busca e os filtros de disponibilidade para achar um produto.',
    ],
    nota: 'Para mudar ou apagar um produto, abra-o na lista e use Editar ou Excluir.',
  },
  {
    id: 'estoque',
    titulo: 'Controlar o estoque',
    passos: [
      'Abra o produto na lista e toque em Ajustar Estoque. Escolha Adicionar ou Subtrair, informe a Quantidade e toque em Confirmar. O estoque não pode ficar negativo.',
      'Toque em Ver Movimentações para abrir o Histórico de Estoque, com as entradas, as saídas por venda, troca ou doação e as correções.',
      'Nessa tela você também registra uma nova movimentação, escolhendo o Tipo e a Quantidade.',
    ],
  },
  {
    id: 'pedidos',
    titulo: 'Pedidos: registrar, confirmar e cancelar',
    passos: [
      'Toque em Pedidos no menu de baixo e depois no botão de novo pedido.',
      'Em Tipo de Pedido, escolha Venda, Troca ou Doação. Em Recebedor, informe o Nome completo e o Contato (telefone ou e-mail).',
      'Em Produtos, toque em + Adicionar, escolha o produto e a quantidade. Só aparecem produtos com estoque, e a quantidade não pode passar do que você tem.',
      'Se quiser, escreva Observações e toque em Registrar Pedido. O pedido fica como Pendente.',
      'Para concluir, abra o pedido e toque em Confirmar Pedido: o estoque é atualizado. Para desistir, toque em Cancelar Pedido e confirme.',
      'Na lista, busque por ID ou cliente e filtre por status (Pendente, Confirmado ou Cancelado), tipo, produto e datas. Limpar filtros volta tudo ao normal.',
    ],
    nota: 'Os botões Confirmar Pedido e Cancelar Pedido só aparecem enquanto o pedido está Pendente.',
  },
  {
    id: 'relatorios',
    titulo: 'Relatórios em PDF e CSV',
    passos: [
      'Na tela Início, use o atalho de relatórios.',
      'Em Tipo de Relatório, escolha Estoque ou Pedidos e ajuste os Filtros (disponibilidade, espécie, status, tipo, produto e datas).',
      'Toque em Gerar Relatório para ver a tabela na tela. Cada relatório gerado fica registrado no histórico de relatórios emitidos.',
      'Depois de gerar, use ⬇ PDF ou ⬇ CSV para baixar o arquivo. O CSV abre em planilhas.',
    ],
    nota: 'Se não houver dados com os filtros escolhidos, a tela mostra "Sem dados" e não dá para exportar.',
  },
  {
    id: 'notificacoes',
    titulo: 'Notificações',
    passos: [
      'O sino no alto da tela mostra quantas notificações você ainda não leu.',
      'Toque no sino para abrir a lista. Para dar todas como lidas de uma vez, toque em Marcar todas como lidas.',
    ],
  },
  {
    id: 'offline',
    titulo: 'Usar sem internet',
    passos: [
      'Sem internet, aparece no alto da tela a faixa "Sem internet. O que já foi aberto continua disponível."',
      'As telas que você já abriu neste aparelho continuam abrindo. Uma tela que nunca foi aberta mostra "Sem internet": conecte-se e tente de novo, ou volte ao painel.',
      'Na tela de movimentações do estoque de um produto, o que você registrar sem internet fica guardado no aparelho e é enviado sozinho quando a conexão voltar. Se o sistema recusar (por exemplo, uma saída maior que o saldo), o item fica marcado com o motivo e você pode descartá-lo.',
    ],
    nota: 'Só isso funciona sem conexão. Os dados do servidor (produtos, pedidos, relatórios) não ficam salvos para consulta, e as demais ações, como cadastrar produto, registrar ou confirmar pedido e gerar relatório, precisam de internet. Entrar na conta também.',
  },
  {
    id: 'sair',
    titulo: 'Sair do aplicativo',
    passos: [
      'Toque em Perfil no menu de baixo, role até o fim e toque em Sair do Aplicativo. Confirme em Sair.',
      'Em Perfil você também edita seus dados (Editar Dados), muda a senha e ajusta o tamanho da letra em Acessibilidade.',
    ],
  },
];

export default function AjudaPage() {
  const router = useRouter();
  const [abertos, setAbertos] = useState<Record<string, boolean>>({});

  const alternar = (id: string) => setAbertos((atual) => ({ ...atual, [id]: !atual[id] }));

  return (
    <div className={legal.page}>
      <button type="button" className={legal.back} onClick={() => router.back()}>
        <ArrowLeft size={16} strokeWidth={2.5} />
        <span>Voltar</span>
      </button>

      <div className={legal.intro}>
        <h1 className={legal.introTitle}>
          <CircleHelp size={20} strokeWidth={2.5} style={{ verticalAlign: '-3px', marginRight: 8 }} aria-hidden="true" />
          Ajuda
        </h1>
        <p className={legal.introText}>
          Passo a passo para usar o Semente Livre. Toque no assunto para ver como fazer.
        </p>
      </div>

      <div className={styles.lista}>
        {TOPICOS.map((t) => {
          const aberto = !!abertos[t.id];
          return (
            <section key={t.id} className={styles.item}>
              <h2 className={styles.titulo}>
                <button
                  type="button"
                  id={`ajuda-${t.id}-botao`}
                  className={styles.gatilho}
                  aria-expanded={aberto}
                  aria-controls={`ajuda-${t.id}-painel`}
                  onClick={() => alternar(t.id)}
                >
                  <span>{t.titulo}</span>
                  <ChevronDown
                    size={18}
                    strokeWidth={2.5}
                    aria-hidden="true"
                    className={`${styles.seta} ${aberto ? styles.setaAberta : ''}`}
                  />
                </button>
              </h2>
              <div
                id={`ajuda-${t.id}-painel`}
                role="region"
                aria-labelledby={`ajuda-${t.id}-botao`}
                hidden={!aberto}
                className={styles.painel}
              >
                <ol className={styles.passos}>
                  {t.passos.map((p) => (
                    <li key={p}>{p}</li>
                  ))}
                </ol>
                {t.nota && <p className={styles.nota}>{t.nota}</p>}
              </div>
            </section>
          );
        })}
      </div>
    </div>
  );
}
