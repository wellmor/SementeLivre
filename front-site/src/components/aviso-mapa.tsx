/** Transparência (LGPD, art. 9º): o que o mapa mostra e quem recebe dados de quem o abre. */
export default function AvisoMapa({ className = "" }: { className?: string }) {
  return (
    <p className={`text-xs text-muted-foreground leading-relaxed ${className}`}>
      A localização é aproximada: cada comunidade aparece na sede do seu município, nunca no
      endereço das famílias ou das propriedades. O fundo do mapa vem do{" "}
      <a
        href="https://wiki.osmfoundation.org/wiki/Privacy_Policy"
        target="_blank"
        rel="noopener noreferrer"
        className="underline text-primary-dark"
      >
        OpenStreetMap
      </a>
      : ao abrir o mapa, seu navegador baixa as imagens dos servidores da OpenStreetMap
      Foundation, que recebem o seu endereço IP.
    </p>
  );
}
