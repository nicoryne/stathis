'use client';

import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Logo } from './logo';
import { Menu, X } from 'lucide-react';
import { motion } from 'framer-motion';
import ThemeSwitcher from './theme-switcher';
import Link from 'next/link';

export function Navbar() {
  const [isOpen, setIsOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 10);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  return (
    <motion.header
      initial={{ y: -100 }}
      animate={{ y: 0 }}
      transition={{ duration: 0.5 }}
      className="fixed top-0 right-0 left-0 z-50 bg-background/95 backdrop-blur-sm shadow-sm border-b border-border"
    >
      <div className="container mx-auto px-4 py-4">
        <div className="flex items-center justify-between">
          <Logo />

          {/* Desktop Navigation */}
          <nav className="hidden items-center gap-6 md:flex">
            <a href="/" className="text-foreground/80 hover:text-primary transition-colors font-medium">
              Home
            </a>
            <a href="#about" className="text-foreground/80 hover:text-primary transition-colors font-medium">
              About
            </a>
            <a href="#features" className="text-foreground/80 hover:text-primary transition-colors font-medium">
              Features
            </a>
            <a href="#contact" className="text-foreground/80 hover:text-primary transition-colors font-medium">
              Contact
            </a>
            <Button className="cta-button font-medium">
              <Link href="/sign-up">Get Started</Link>
            </Button>
            <ThemeSwitcher />
          </nav>

          {/* Mobile Menu Button */}
          <div className="flex gap-4 md:hidden">
            <ThemeSwitcher />
            <button className="text-foreground" onClick={() => setIsOpen(!isOpen)}>
              {isOpen ? <X size={24} /> : <Menu size={24} />}
            </button>
          </div>
        </div>

        {/* Mobile Navigation */}
        {isOpen && (
          <motion.nav
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            transition={{ duration: 0.3 }}
            className="flex flex-col gap-4 pt-4 pb-2 md:hidden"
          >
            <a
              href="/"
              className="text-foreground/80 hover:text-primary py-2 transition-colors font-medium"
              onClick={() => setIsOpen(false)}
            >
              Home
            </a>
            <a
              href="#about"
              className="text-foreground/80 hover:text-primary py-2 transition-colors font-medium"
              onClick={() => setIsOpen(false)}
            >
              About
            </a>
            <a
              href="#features"
              className="text-foreground/80 hover:text-primary py-2 transition-colors font-medium"
              onClick={() => setIsOpen(false)}
            >
              Features
            </a>
            <a
              href="#contact"
              className="text-foreground/80 hover:text-primary py-2 transition-colors font-medium"
              onClick={() => setIsOpen(false)}
            >
              Contact
            </a>
            <Button className="cta-button font-medium w-full">
              <Link href="/sign-up">Get Started</Link>
            </Button>
          </motion.nav>
        )}
      </div>
    </motion.header>
  );
}
