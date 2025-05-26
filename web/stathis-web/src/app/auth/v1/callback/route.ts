import { NextResponse } from 'next/server'
import { serverApiClient } from '@/lib/api/server-client'

/**
 * OAuth callback handler for our custom authentication
 * Replaces the Supabase OAuth callback implementation
 */
export async function GET(request: Request) {
  const { searchParams, origin } = new URL(request.url)
  const code = searchParams.get('code')
  const provider = searchParams.get('provider')
  // if "next" is in param, use it as the redirect URL
  const next = searchParams.get('next') ?? '/dashboard'

  if (code) {
    // Exchange the authorization code for a token with our backend
    const { data, error } = await serverApiClient.post('/auth/oauth/callback', {
      code,
      provider
    })
    
    if (!error && data) {
      // Store the token in a cookie or localStorage
      if (typeof window !== 'undefined' && data && typeof data === 'object' && 'token' in data) {
        localStorage.setItem('auth_token', data.token as string)
      }
      
      // Redirect to the next page
      return NextResponse.redirect(`${origin}${next}`)
    } else {
      return NextResponse.redirect(`${origin}${next}`)
    }
  } else {
    return NextResponse.redirect(`${origin}${next}`)
  }
  // return the user to an error page with instructions
  return NextResponse.redirect(`${origin}/auth/auth-code-error`)
}