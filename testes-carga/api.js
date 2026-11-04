// Testes de carga, stress e concorrência da API Semente Livre (#130).
//
//   k6 run -e CENARIO=carga        api.js   # carga nominal, com metas (falha se não cumprir)
//   k6 run -e CENARIO=stress       api.js   # degraus de VUs até a degradação
//   k6 run -e CENARIO=concorrencia api.js   # aceites simultâneos sobre o mesmo estoque
//
// Variáveis: BASE_URL (padrão http://localhost:18080), USUARIOS (produtores criados no setup),
// VUS e DURACAO (carga), PEDIDOS_FILA (concorrência). Ver README.md desta pasta.

import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = __ENV.BASE_URL || 'http://localhost:18080';
const CENARIO = __ENV.CENARIO || 'carga';
const N_USUARIOS = Number(__ENV.USUARIOS || 10);
const PEDIDOS_FILA = Number(__ENV.PEDIDOS_FILA || 60);
const SENHA = 'SenhaForte123';
const JSON_H = { 'Content-Type': 'application/json' };
// Cada rodada gera e-mails, CPFs e RGs novos: dá para rodar de novo sobre o mesmo banco
const RODADA = Math.floor(Date.now() / 1000) % 100000;

// Degraus do stress: cada um vira um cenário com a tag "nivel", e o resumo sai por degrau
const DEGRAUS = (__ENV.DEGRAUS || '50,100,200,300,400,600').split(',').map(Number);
const DURACAO_DEGRAU = __ENV.DURACAO_DEGRAU || '1m';

function cenarios() {
    if (CENARIO === 'carga') {
        return {
            carga: { executor: 'constant-vus', vus: Number(__ENV.VUS || 50), duration: __ENV.DURACAO || '3m' },
        };
    }
    if (CENARIO === 'stress') {
        const minutos = parseInt(DURACAO_DEGRAU, 10);
        const s = {};
        DEGRAUS.forEach((vus, i) => {
            s[`degrau_${vus}`] = {
                executor: 'constant-vus', vus, duration: DURACAO_DEGRAU,
                startTime: `${i * minutos}m`, tags: { nivel: String(vus) }, gracefulStop: '5s',
            };
        });
        return s;
    }
    return {
        concorrencia: { executor: 'per-vu-iterations', vus: PEDIDOS_FILA, iterations: 1, maxDuration: '2m' },
    };
}

// Tempo de resposta definido para a carga nominal (SLO): p95 < 500 ms e menos de 1% de erro
const ENDPOINTS = ['catalogo_lista', 'catalogo_detalhe', 'estoque_lista', 'pedidos_lista',
    'auth_login', 'auth_me', 'relatorio_csv', 'pedido_publico'];

function metas() {
    if (CENARIO === 'carga') {
        const t = { http_req_failed: ['rate<0.01'], http_req_duration: ['p(95)<500'], checks: ['rate>0.99'] };
        ENDPOINTS.forEach((ep) => { t[`http_req_duration{ep:${ep}}`] = ['p(95)<500']; });
        return t;
    }
    if (CENARIO === 'stress') {
        // Sem meta: os limiares só fazem o k6 separar as métricas por degrau no resumo
        const t = {};
        DEGRAUS.forEach((vus) => {
            t[`http_req_duration{nivel:${vus}}`] = ['p(95)<600000'];
            t[`http_req_failed{nivel:${vus}}`] = ['rate<=1'];
            t[`http_reqs{nivel:${vus}}`] = ['count>=0'];
        });
        return t;
    }
    return { checks: ['rate==1'] };
}

export const options = {
    scenarios: cenarios(),
    thresholds: metas(),
    setupTimeout: '10m',
    summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

// ---------- dados de teste ----------

function cpfValido(n) {
    const d = String(n).padStart(9, '0').slice(-9).split('').map(Number);
    const dv = (len, peso) => {
        let soma = 0;
        for (let i = 0; i < len; i++) soma += d[i] * (peso - i);
        const resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    };
    d.push(dv(9, 10));
    d.push(dv(10, 11));
    return d.join('');
}

function post(url, corpo, token, tags) {
    const headers = token ? Object.assign({ Authorization: `Bearer ${token}` }, JSON_H) : JSON_H;
    return http.post(`${BASE}${url}`, JSON.stringify(corpo), { headers, tags });
}

function get(url, token, tags) {
    const headers = token ? { Authorization: `Bearer ${token}` } : {};
    return http.get(`${BASE}${url}`, { headers, tags });
}

function exigir(res, status, oQue) {
    if (res.status !== status) {
        throw new Error(`${oQue}: esperado ${status}, veio ${res.status} — ${String(res.body).slice(0, 300)}`);
    }
    return res.json();
}

function criarProdutor(i) {
    const email = `carga${RODADA}_${i}@carga.test`;
    exigir(post('/auth/cadastrar', {
        nome: `Produtor Carga ${i}`, tipoDocumento: 'CPF', documento: cpfValido(RODADA * 1000 + i),
        telefone: '32999990000', email, senha: SENHA, rg: `C${RODADA}${i}`, exibirNoSitePublico: true,
        endereco: { logradouro: 'Rua da Carga', numero: String(i), bairro: 'Centro', municipio: 'Rio Pomba', uf: 'MG' },
    }), 201, `cadastro ${email}`);
    const token = exigir(post('/auth/login', { email, senha: SENHA }), 200, `login ${email}`).accessToken;
    const id = exigir(get('/auth/me', token), 200, 'perfil').id;
    return { email, token, id };
}

function criarOferta(produtor, quantidade, nome) {
    const produto = exigir(post('/produtos', {
        nomePopular: nome, urlFoto: '/uploads/produtos/carga.png',
        tipo: 'LEGUMINOSA', especie: 'FEIJAO', formato: 'SEMENTE',
    }, produtor.token), 200, `produto ${nome}`);
    const estoque = exigir(post('/estoques', {
        proprietarioId: produtor.id, produtoId: produto.id, preco: 5.0, quantidade,
        tipoPesagem: 'KG', disponibilidade: 'PARA_VENDA', tipoMovimentacao: 'ENTRADA', aceitaNegociacao: true,
    }, produtor.token), 200, `estoque ${nome}`);
    return { produtoId: produto.id, estoqueId: estoque.id };
}

function pedidoPublico(estoqueId, quantidade, email, tags) {
    return post('/catalogo/pedidos', {
        estoqueId, quantidade, nome: 'Comprador Carga', email, telefone: '32988887777',
    }, null, tags);
}

export function setup() {
    if (CENARIO === 'concorrencia') {
        // Um produtor com 100 kg e uma fila de pedidos de 5 kg que somam mais que o estoque
        const dono = criarProdutor(0);
        const oferta = criarOferta(dono, 100, `Feijão Concorrência ${RODADA}`);
        const pedidos = [];
        for (let i = 0; i < PEDIDOS_FILA; i++) {
            pedidos.push(exigir(pedidoPublico(oferta.estoqueId, 5, `fila${RODADA}_${i}@carga.test`),
                201, `pedido da fila ${i}`).id);
        }
        return { dono, oferta, pedidos };
    }
    const produtores = [];
    for (let i = 0; i < N_USUARIOS; i++) {
        const p = criarProdutor(i);
        // Estoque grande: os pedidos públicos da carga não podem esgotar a oferta no meio do teste
        Object.assign(p, criarOferta(p, 100000000, `Feijão Carga ${RODADA}-${i}`));
        produtores.push(p);
    }
    return { produtores };
}

// ---------- tráfego ----------

function iteracaoMista(dados) {
    const lista = dados.produtores;
    const p = lista[Math.floor(Math.random() * lista.length)];
    const r = Math.random();
    let res;
    let ok = [200];

    if (r < 0.30) {
        res = get('/catalogo/produtos', null, { ep: 'catalogo_lista' });
    } else if (r < 0.45) {
        res = get(`/catalogo/produtos/${p.produtoId}`, null, { ep: 'catalogo_detalhe' });
    } else if (r < 0.60) {
        res = get('/estoques', p.token, { ep: 'estoque_lista' });
    } else if (r < 0.72) {
        res = get(`/pedidos?proprietarioId=${p.id}`, p.token, { ep: 'pedidos_lista' });
    } else if (r < 0.80) {
        res = post('/auth/login', { email: p.email, senha: SENHA }, null, { ep: 'auth_login' });
    } else if (r < 0.85) {
        res = get(`/relatorios/estoque/csv?proprietarioId=${p.id}`, p.token, { ep: 'relatorio_csv' });
    } else if (r < 0.95) {
        // E-mail único por iteração: mede o pedido, não o limite por e-mail de destino
        res = pedidoPublico(p.estoqueId, 1, `comprador_${RODADA}_${__VU}_${__ITER}@carga.test`, { ep: 'pedido_publico' });
        ok = [201];
    } else {
        res = get('/auth/me', p.token, { ep: 'auth_me' });
    }

    check(res, { 'status esperado': (x) => ok.includes(x.status) });
}

function aceitarDaFila(dados) {
    const pedidoId = dados.pedidos[__VU - 1];
    const res = http.patch(`${BASE}/pedidos/${pedidoId}/confirmar`, null, {
        headers: { Authorization: `Bearer ${dados.dono.token}` }, tags: { ep: 'pedido_confirmar' },
    });
    // 200 aceito; 422 sem saldo; 409 já recusado por um aceite anterior. Nunca 5xx.
    check(res, { 'aceite sem erro de servidor': (x) => [200, 409, 422].includes(x.status) });
}

export default function (dados) {
    if (CENARIO === 'concorrencia') {
        aceitarDaFila(dados);
        return;
    }
    iteracaoMista(dados);
    sleep(CENARIO === 'stress' ? 0.5 : 1);
}

export function teardown(dados) {
    if (CENARIO !== 'concorrencia') return;
    const { dono, oferta } = dados;
    const estoque = exigir(get(`/estoques/${oferta.estoqueId}`, dono.token), 200, 'estoque final');
    const pedidos = exigir(get(`/pedidos?proprietarioId=${dono.id}&produtoId=${oferta.produtoId}`, dono.token),
        200, 'pedidos finais');
    const confirmados = pedidos.filter((p) => p.status === 'CONFIRMADO').length;
    const pendentes = pedidos.filter((p) => p.status === 'PENDENTE').length;
    console.log(`concorrência: estoque final=${estoque.quantidade} kg, confirmados=${confirmados}, `
        + `pendentes=${pendentes}, total=${pedidos.length}`);
    check(estoque, {
        'estoque nunca fica negativo': (e) => e.quantidade >= 0,
        'baixa bate com os aceites (100 - 5 x confirmados)': (e) => e.quantidade === 100 - 5 * confirmados,
        'exatamente 20 aceites cabem em 100 kg': () => confirmados === 20,
        'nenhum pedido ficou pendente na fila': () => pendentes === 0,
    });
}
