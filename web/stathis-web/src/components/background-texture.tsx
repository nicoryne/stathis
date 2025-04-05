'use client';

import { useEffect, useRef } from 'react';

export function BackgroundTexture() {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const resizeCanvas = () => {
      canvas.width = window.innerWidth;
      canvas.height = window.innerHeight;
      drawTexture();
    };

    const drawTexture = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height);

      // Create gradient
      const gradient = ctx.createLinearGradient(0, 0, canvas.width, canvas.height);
      gradient.addColorStop(0, 'rgba(147, 52, 234, 0.03)'); // Purple with low opacity
      gradient.addColorStop(1, 'rgba(37, 172, 164, 0.03)'); // Teal with low opacity
      ctx.fillStyle = gradient;
      ctx.fillRect(0, 0, canvas.width, canvas.height);

      // Draw dots
      const dotCount = Math.floor((canvas.width * canvas.height) / 15000);

      for (let i = 0; i < dotCount; i++) {
        const x = Math.random() * canvas.width;
        const y = Math.random() * canvas.height;
        const size = Math.random() * 2 + 1;

        // Alternate between purple and teal
        ctx.fillStyle = i % 2 === 0 ? 'rgba(147, 52, 234, 0.1)' : 'rgba(37, 172, 164, 0.1)';

        ctx.beginPath();
        ctx.arc(x, y, size, 0, Math.PI * 2);
        ctx.fill();
      }
    };

    resizeCanvas();
    window.addEventListener('resize', resizeCanvas);

    return () => {
      window.removeEventListener('resize', resizeCanvas);
    };
  }, []);

  return (
    <canvas ref={canvasRef} className="pointer-events-none fixed inset-0 -z-10 h-full w-full" />
  );
}
