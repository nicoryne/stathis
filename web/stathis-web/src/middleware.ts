import { type NextRequest } from 'next/server';
import { updateSession } from '@/lib/api/middleware';

// Use the API middleware as the main middleware implementation
export async function middleware(request: NextRequest) {
  return await updateSession(request);
}

// Configure which routes middleware runs on
export const config = {
  matcher: [
    /*
     * Match all request paths except:
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     * - downloads folder (for APK and other large files)
     * - images - .svg, .png, .jpg, .jpeg, .gif, .webp
     * - APK files - .apk
     */
    '/((?!_next/static|_next/image|favicon.ico|downloads|.*\.(?:svg|png|jpg|jpeg|gif|webp|apk)$).*)'
  ]
};