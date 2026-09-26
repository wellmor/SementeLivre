import { NextRequest, NextResponse } from 'next/server';

type Context = { params: Promise<{ path: string[] }> };

/**
 * Repassa /api/backend/<caminho> para <BACKEND_URL>/<caminho>, com método,
 * corpo e header Authorization. Evita CORS e um handler por endpoint.
 */
async function repassar(request: NextRequest, context: Context) {
  try {
    const backendUrl = process.env.BACKEND_URL;

    if (!backendUrl) {
      return NextResponse.json(
        { message: 'BACKEND_URL não configurada.' },
        { status: 500 }
      );
    }

    const { path } = await context.params;
    const url = `${backendUrl}/${path.map(encodeURIComponent).join('/')}${request.nextUrl.search}`;

    const headers = new Headers();
    const contentType = request.headers.get('content-type');
    const authorization = request.headers.get('authorization');
    if (contentType) headers.set('Content-Type', contentType);
    if (authorization) headers.set('Authorization', authorization);

    const temCorpo = request.method !== 'GET' && request.method !== 'HEAD';

    const response = await fetch(url, {
      method: request.method,
      headers,
      body: temCorpo ? await request.text() : undefined,
      cache: 'no-store',
    });

    const corpo = await response.text();

    return new NextResponse(corpo || null, {
      status: response.status,
      headers: {
        'Content-Type': response.headers.get('content-type') ?? 'application/json',
      },
    });
  } catch {
    return NextResponse.json(
      { message: 'Não foi possível conectar ao backend.' },
      { status: 502 }
    );
  }
}

export const GET = repassar;
export const POST = repassar;
export const PUT = repassar;
export const DELETE = repassar;
