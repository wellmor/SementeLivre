/**
 * toast.ts — Dispara toasts fora de componentes React (ex.: no cliente HTTP).
 * O ToastProvider escuta o evento e mostra a mensagem.
 */

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export const TOAST_EVENT = 'sl:toast';

export interface ToastEventDetail {
  message: string;
  type: ToastType;
}

export function emitirToast(message: string, type: ToastType = 'error'): void {
  if (typeof window === 'undefined') return;
  window.dispatchEvent(new CustomEvent<ToastEventDetail>(TOAST_EVENT, { detail: { message, type } }));
}
