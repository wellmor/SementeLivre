# Mapa de Sementes (RF-08)

Mapa com a localização **aproximada** das comunidades que têm bancos de sementes ativos: página `/mapa` e botão "Ver no mapa" em cada card da home.

## Como funciona

1. O catálogo público do backend já devolve, em cada oferta, a comunidade e o **município e UF da comunidade** (do logradouro da comunidade, não do produtor).
2. A rota `/api/mapa` (servidor) agrupa as ofertas por comunidade e põe cada uma na **sede do município**, pela tabela do IBGE. Município não reconhecido cai no centro da UF; UF não reconhecida fica fora do mapa.
3. O navegador recebe só os pontos prontos (`PontoMapa`), nunca a tabela de coordenadas.

| Arquivo | Responsabilidade |
|---|---|
| `coordenadas-ibge.json` | sedes dos 5.570 municípios e centros das 27 UFs |
| `coordenadas.ts` | `coordenadaDe(municipio, uf)` |
| `montar-pontos.ts` | `montarPontos(ofertas)`: um ponto por comunidade (servidor) |
| `pontos.ts` | tipo `PontoMapa` |
| `carregar-pontos.ts` | busca `/api/mapa` uma vez por visita (navegador) |
| `components/mapa-sementes.tsx` | o mapa Leaflet; recebe os pontos prontos |
| `components/botao-ver-no-mapa.tsx` | botão do card, abre o mapa numa janela |
| `app/mapa/page.tsx` | página `/mapa`, com o mapa e a lista em texto |

## LGPD

- **Minimização:** o pino é a sede do município, não o endereço da comunidade nem da propriedade. Não há geocodificação de endereço, e o zoom máximo é 12.
- **Nada de produtor:** `PontoMapa` só tem o que o catálogo público já mostra (nome da comunidade, município, UF e nome e foto dos produtos, exibidos como miniaturas no balão). O e2e `mapa.spec.ts` confere isso.
- **Sem texto livre de localização:** município e UF vêm do cadastro estruturado da comunidade. A proposta anterior pedia ao produtor um campo de texto livre, que exibiria o endereço por extenso se alguém o digitasse.
- **XSS:** o balão do pino é montado com nós DOM (`textContent`, `alt`, `title`). Os nomes vêm do cadastro no app e nunca entram como HTML.
- **OpenStreetMap:** as imagens do mapa vêm de `tile.openstreetmap.org`, e a OpenStreetMap Foundation (Reino Unido) recebe o IP de quem abre o mapa. O botão do card não baixa nada antes do clique. A tela só diz "A localização é aproximada" (decisão da equipe); se a gestão quiser citar o OpenStreetMap, o lugar é uma política de privacidade do site.
- **Transparência:** a política de privacidade do app (seção 5) diz que comunidade, município e UF aparecem no catálogo e no mapa.

## Atualizar a tabela de coordenadas

Fonte: [kelvins/municipios-brasileiros](https://github.com/kelvins/municipios-brasileiros) (MIT), que traz as sedes municipais do IBGE. Para regenerar a partir dos CSVs `municipios.csv` e `estados.csv`, a chave de cada município é o nome normalizado por `normalizar()` (`coordenadas.ts`), agrupado por UF:

```json
{ "estados": { "MG": [-18.1, -44.38] }, "municipios": { "MG": { "rio pomba": [-21.2712, -43.1696] } } }
```

## Uso do OpenStreetMap

A [política de uso dos tiles](https://operations.osmfoundation.org/policies/tiles/) permite uso leve com atribuição visível, que o Leaflet já mostra. Se o tráfego crescer, troque a URL em `mapa-sementes.tsx` por um provedor próprio ou pago.
