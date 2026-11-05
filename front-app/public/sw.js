/*
 * Service Worker do Semente Livre (RNF-04, #234): o app abre sem internet.
 *
 * - Telas (navegação): rede primeiro; sem rede, a última versão salva da tela.
 *   Tela nunca aberta neste aparelho cai em /offline.
 * - Arquivos do build (/_next/static, com hash no nome): cache primeiro.
 * - Ícones, manifest e outros arquivos públicos: cache, atualizado em segundo plano.
 * - NUNCA guarda respostas da API do backend (outra origem) nem das rotas /api do
 *   app (sessão, catálogo): os dados offline são assunto do app, ligados à sessão.
 *
 * Na instalação já baixa as telas principais e os arquivos que elas usam, para o
 * app abrir offline mesmo em telas que a pessoa ainda não visitou.
 */

const VERSAO = 'v1';
const CACHE_PAGINAS = `sl-paginas-${VERSAO}`;
const CACHE_ESTATICOS = `sl-estaticos-${VERSAO}`;
const MAX_ESTATICOS = 400;

const PAGINA_OFFLINE = '/offline';

const PAGINAS = [
  PAGINA_OFFLINE,
  '/entrar',
  '/cadastrar',
  '/recuperar-senha',
  '/dashboard',
  '/produtos',
  '/produtos/nova',
  '/pedidos',
  '/pedidos/novo',
  '/propriedades',
  '/propriedades/nova',
  '/catalogo',
  '/relatorios',
  '/notificacoes',
  '/perfil',
  '/privacidade',
  '/termos',
];

const ARQUIVOS_PUBLICOS = ['/manifest.json', '/icons/icon.svg'];

/** Arquivos do build citados no HTML de uma tela (scripts, CSS, fontes). */
function arquivosDoHtml(html) {
  return [...new Set(html.match(/\/_next\/static\/[^"'\s)\\]+/g) || [])];
}

async function guardarTela(caminho) {
  const resposta = await fetch(caminho, { cache: 'reload', credentials: 'same-origin' });
  if (!resposta.ok) return;
  const html = await resposta.clone().text();
  await (await caches.open(CACHE_PAGINAS)).put(caminho, resposta);

  const estaticos = await caches.open(CACHE_ESTATICOS);
  await Promise.allSettled(
    arquivosDoHtml(html).map(async (arquivo) => {
      if (!(await estaticos.match(arquivo))) await estaticos.add(arquivo);
    })
  );
}

self.addEventListener('install', (evento) => {
  evento.waitUntil(
    (async () => {
      // allSettled: uma tela que falhar não impede as outras de ficarem offline
      await Promise.allSettled(PAGINAS.map(guardarTela));
      const estaticos = await caches.open(CACHE_ESTATICOS);
      await Promise.allSettled(ARQUIVOS_PUBLICOS.map((arquivo) => estaticos.add(arquivo)));
      await self.skipWaiting();
    })()
  );
});

self.addEventListener('activate', (evento) => {
  evento.waitUntil(
    (async () => {
      const atuais = [CACHE_PAGINAS, CACHE_ESTATICOS];
      for (const nome of await caches.keys()) {
        if (nome.startsWith('sl-') && !atuais.includes(nome)) await caches.delete(nome);
      }
      await self.clients.claim();
    })()
  );
});

/** Os arquivos com hash se acumulam a cada deploy: mantém só os mais recentes. */
async function aparar(cache) {
  const chaves = await cache.keys();
  for (const chave of chaves.slice(0, Math.max(0, chaves.length - MAX_ESTATICOS))) {
    await cache.delete(chave);
  }
}

async function telaRedePrimeiro(requisicao) {
  const caminho = new URL(requisicao.url).pathname;
  const paginas = await caches.open(CACHE_PAGINAS);
  try {
    const resposta = await fetch(requisicao);
    // Só guarda tela que abriu de verdade (nada de 404, 500 ou redirecionamento)
    if (resposta.ok && resposta.type === 'basic') await paginas.put(caminho, resposta.clone());
    return resposta;
  } catch {
    const salva = await paginas.match(caminho);
    if (salva) return salva;
    // Redireciona (em vez de servir o HTML de /offline neste endereço): o roteador do
    // Next estranha uma tela num endereço que não é o dela e renderiza outra rota
    if (await paginas.match(PAGINA_OFFLINE)) {
      const destino = new URL(PAGINA_OFFLINE, self.location.origin);
      destino.searchParams.set('de', caminho + new URL(requisicao.url).search);
      return Response.redirect(destino.href, 302);
    }
    return Response.error();
  }
}

async function cachePrimeiro(requisicao) {
  const estaticos = await caches.open(CACHE_ESTATICOS);
  const salvo = await estaticos.match(requisicao);
  if (salvo) return salvo;
  const resposta = await fetch(requisicao);
  if (resposta.ok) {
    await estaticos.put(requisicao, resposta.clone());
    void aparar(estaticos);
  }
  return resposta;
}

async function cacheAtualizandoDepois(requisicao) {
  const estaticos = await caches.open(CACHE_ESTATICOS);
  const salvo = await estaticos.match(requisicao);
  const daRede = fetch(requisicao)
    .then(async (resposta) => {
      if (resposta.ok) await estaticos.put(requisicao, resposta.clone());
      return resposta;
    })
    .catch(() => salvo || Response.error());
  return salvo || daRede;
}

self.addEventListener('fetch', (evento) => {
  const requisicao = evento.request;
  if (requisicao.method !== 'GET') return;

  const url = new URL(requisicao.url);
  // Outra origem (API do backend, ViaCEP, fontes): direto na rede, sem cache
  if (url.origin !== self.location.origin) return;
  // Rotas do servidor do app (sessão, catálogo) e o próprio Service Worker: nunca em cache
  if (url.pathname.startsWith('/api/') || url.pathname === '/sw.js') return;
  // Dados das telas do Next (RSC): se falhar, o Next recarrega a tela, que cai na navegação
  if (requisicao.headers.get('RSC') || url.searchParams.has('_rsc')) return;

  if (requisicao.mode === 'navigate') {
    evento.respondWith(telaRedePrimeiro(requisicao));
  } else if (url.pathname.startsWith('/_next/static/')) {
    evento.respondWith(cachePrimeiro(requisicao));
  } else {
    evento.respondWith(cacheAtualizandoDepois(requisicao));
  }
});
