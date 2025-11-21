import { NextRequest, NextResponse } from 'next/server';
import { createReadStream, statSync } from 'fs';
import { join } from 'path';
import { Readable } from 'stream';

export const dynamic = 'force-dynamic';
export const runtime = 'nodejs';
export const maxDuration = 300; // 5 minutes for large file downloads

export async function GET(request: NextRequest) {
  try {
    const filePath = join(process.cwd(), 'public', 'downloads', 'stathis-mobile.apk');
    
    // Check if file exists and get file size
    let fileStats;
    try {
      fileStats = statSync(filePath);
    } catch (error) {
      return NextResponse.json(
        { error: 'APK file not found' },
        { status: 404 }
      );
    }

    const fileSize = fileStats.size;

    // Set proper headers for file download
    const headers = new Headers();
    headers.set('Content-Type', 'application/vnd.android.package-archive');
    headers.set('Content-Disposition', 'attachment; filename="stathis-mobile.apk"');
    headers.set('Accept-Ranges', 'bytes');
    headers.set('Cache-Control', 'public, max-age=3600');

    // Handle range requests for resumable downloads
    const range = request.headers.get('range');
    if (range) {
      const parts = range.replace(/bytes=/, '').split('-');
      const start = parseInt(parts[0], 10);
      const end = parts[1] ? parseInt(parts[1], 10) : fileSize - 1;
      const chunkSize = (end - start) + 1;

      headers.set('Content-Range', `bytes ${start}-${end}/${fileSize}`);
      headers.set('Content-Length', chunkSize.toString());
      
      // Create a readable stream for the chunk
      const fileStream = createReadStream(filePath, { start, end });
      const stream = Readable.toWeb(fileStream) as ReadableStream;
      
      return new NextResponse(stream, {
        status: 206, // Partial Content
        headers,
      });
    }

    // Return full file as stream
    headers.set('Content-Length', fileSize.toString());
    const fileStream = createReadStream(filePath);
    const stream = Readable.toWeb(fileStream) as ReadableStream;
    
    return new NextResponse(stream, {
      status: 200,
      headers,
    });
  } catch (error) {
    console.error('Error serving APK file:', error);
    return NextResponse.json(
      { error: 'Failed to serve APK file' },
      { status: 500 }
    );
  }
}

