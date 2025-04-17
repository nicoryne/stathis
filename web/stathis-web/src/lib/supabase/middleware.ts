import { createServerClient, type CookieOptions } from '@supabase/ssr';
import { type NextRequest, NextResponse } from 'next/server';

const loggedInRoutes = ['/', '/login', '/register', '/forgot-password']

export const updateSession = async (request: NextRequest) => {
  try {
    let response = NextResponse.next({
      request: {
        headers: request.headers
      }
    });

    const supabase = createServerClient(
      process.env.NEXT_PUBLIC_SUPABASE_URL!,
      process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!,
      {
        cookies: {
          getAll() {
            return request.cookies.getAll();
          },
          setAll(cookiesToSet) {
            cookiesToSet.forEach(({ name, value }) => request.cookies.set(name, value));
            response = NextResponse.next({
              request
            });
            cookiesToSet.forEach(({ name, value, options }) =>
              response.cookies.set(name, value, options)
            );
          }
        }
      }
    );

    const user = await supabase.auth.getUser();
    const currentPath = request.nextUrl.pathname;

    if (currentPath.startsWith('/_next')) {
      return NextResponse.rewrite(new URL('/404', request.url));
    }

    // protected routes
    if (user.error && currentPath.startsWith('/dashboard')) {
      return NextResponse.redirect(new URL('/login', request.url));
    }

    if (!user.error && loggedInRoutes.includes(currentPath)) {
      return NextResponse.redirect(new URL('/dashboard', request.url));
    }

    return response;
  } catch (e) {
    return NextResponse.next({
      request: {
        headers: request.headers
      }
    });
  }
};

