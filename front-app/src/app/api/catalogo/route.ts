import { NextRequest, NextResponse } from 'next/server';

export async function GET(request: NextRequest) {
  try {
    const backendUrl = process.env.BACKEND_URL;

    if (!backendUrl) {
      return NextResponse.json(
        { error: 'BACKEND_URL não configurada.' },
        { status: 500 }
      );
    }

    const params = new URLSearchParams(request.nextUrl.searchParams);

    const response = await fetch(
      `${backendUrl}/catalogo/produtos?${params.toString()}`,
      {
        cache: 'no-store',
      }
    );

    if (!response.ok) {
      return NextResponse.json(
        { error: 'Erro ao consultar o catálogo no backend.' },
        { status: response.status }
      );
    }

    const data = await response.json();

    return NextResponse.json(data);
  } catch {
    return NextResponse.json(
      { error: 'Não foi possível conectar ao backend.' },
      { status: 502 }
    );
  }
}