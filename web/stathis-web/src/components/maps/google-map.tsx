'use client';

import { useState } from 'react';

interface GoogleMapProps {
  className?: string;
}

export function GoogleMap({ className = '' }: GoogleMapProps) {
  // CIT-U coordinates
  const latitude = 10.2945;
  const longitude = 123.8811;
  
  return (
    <div className={`w-full h-96 rounded-xl overflow-hidden border border-border shadow-md relative ${className}`}>
      {/* Using a more specific embed URL with zoom level 17 for better visibility */}
      <iframe
        src={`https://maps.google.com/maps?q=${latitude},${longitude}&z=17&output=embed`}
        width="100%"
        height="100%"
        style={{ border: 0 }}
        allowFullScreen={false}
        loading="lazy"
        referrerPolicy="no-referrer-when-downgrade"
        title="Cebu Institute of Technology - University Map"
        aria-label="Map showing the location of Cebu Institute of Technology - University"
      />
      
      {/* Add a visual indicator that this is CIT-U */}
      <div className="absolute bottom-4 left-4 bg-background/90 backdrop-blur-sm p-3 rounded-lg border border-border shadow-md">
        <h4 className="font-medium text-sm flex items-center gap-1">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-primary">
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
            <circle cx="12" cy="10" r="3"></circle>
          </svg>
          CIT-University
        </h4>
        <p className="text-xs text-muted-foreground">N. Bacalso Avenue, Cebu City</p>
      </div>
    </div>
  );
}
