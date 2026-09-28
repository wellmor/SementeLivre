# Mapa de Sementes (RF-08)

Mapa interativo com a localização **aproximada** das comunidades que têm bancos de sementes ativos.

## Por que Leaflet e não Google Maps

| | Leaflet + OpenStreetMap | Google Maps |
|---|---|---|
| Chave de API | não precisa | precisa |
| Conta de faturamento | não precisa | precisa cartão de crédito, mesmo na cota grátis |
| Custo | gratuito | gratuito até a cota, depois cobrado |

Como o RF-08 pede localização aproximada, não há necessidade dos recursos pagos do Google. Se a equipe decidir trocar, só o arquivo `components/mapa-sementes.tsx` muda.

## Arquivos

| Arquivo | Responsabilidade |
|---|---|
| `lib/mapa/municipios.ts` | tabela de coordenadas por município/UF e a função `coordenadaDe()` |
| `lib/mapa/pontos.ts` | `montarPontos()`, que junta comunidades e sementes em pontos do mapa |
| `components/mapa-sementes.tsx` | o mapa em si; **recebe os pontos prontos, não busca dados** |
| `app/mapa/page.tsx` | a página `/mapa`, que busca os dados e usa o componente |

## O que falta para a integração real

**1. Trocar a fonte dos dados.** Em `app/mapa/page.tsx` há um comentário marcando o ponto exato. Hoje ele lê o catálogo em memória do próprio site:

```ts
fetch("/api/comunidades")
fetch("/api/catalog")
```

Quando o backend expuser o catálogo público (issue #93) e as comunidades, troque essas duas chamadas. O componente do mapa não muda, desde que `montarPontos()` continue devolvendo `PontoMapa[]`.

**2. Cadastrar as coordenadas dos municípios reais.** A tabela em `municipios.ts` tem só alguns lugares, incluindo Rio Pomba/MG. Uma comunidade cuja localização não for reconhecida **não aparece no mapa**, de propósito, para não colocar um pino no lugar errado. Use `localizacoesNaoReconhecidas()` para descobrir quais faltam.

**3. Decidir a granularidade.** Hoje o ponto é a comunidade. Se a equipe quiser por município, agrupe em `montarPontos()`.

**4. Ligar a página ao site.** A página `/mapa` existe, mas ainda **não há link para ela** no cabeçalho. Isso foi deixado de fora de propósito, para não conflitar com outras alterações em andamento no `public-header.tsx`.

## Cuidado com privacidade

O RF-08 pede localização aproximada, e isso não é detalhe: as propriedades são de famílias de uma comunidade quilombola. O mapa **não deve** mostrar o endereço exato nem coordenada de propriedade individual. Por isso o ponto é a comunidade, e as coordenadas são fixas por município.
