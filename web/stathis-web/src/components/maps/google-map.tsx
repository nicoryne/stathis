'use client';

import { useState } from 'react';

interface GoogleMapProps {
  className?: string;
}

export function GoogleMap({ className = '' }: GoogleMapProps) {
  return (
    <div className={`w-full h-96 rounded-xl overflow-hidden border border-border shadow-md ${className}`}>
      <iframe 
        src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3925.5381431330187!2d123.88289687584302!3d10.315701989760893!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x33a999a3f9684d7d%3A0x9e5b2e9f85ae3cc8!2sCebu%20Institute%20of%20Technology%20-%20University!5e0!3m2!1sen!2sph!4v1695309412345!5m2!1sen!2sph" 
        width="100%" 
        height="100%" 
        style={{ border: 0 }} 
        allowFullScreen={false} 
        loading="lazy" 
        referrerPolicy="no-referrer-when-downgrade"
        title="Cebu Institute of Technology - University Map"
        aria-label="Map showing the location of Cebu Institute of Technology - University"
      />
    </div>
  );
}
