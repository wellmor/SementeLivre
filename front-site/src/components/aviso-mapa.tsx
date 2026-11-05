/** Aviso de que o pino não é o endereço de ninguém: fica na sede do município. */
export default function AvisoMapa({ className = "" }: { className?: string }) {
  return <p className={`text-xs text-muted-foreground ${className}`}>A localização é aproximada.</p>;
}
