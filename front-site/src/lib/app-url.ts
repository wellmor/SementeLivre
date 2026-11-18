// Endereço do app dos produtores (front-app). A raiz do app manda para /entrar ou,
// com sessão salva, para o /dashboard. Em produção, defina NEXT_PUBLIC_APP_URL no build.
export const APP_URL = process.env.NEXT_PUBLIC_APP_URL ?? "http://localhost:3000";
