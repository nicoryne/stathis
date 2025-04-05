import { SunIcon, MoonIcon } from 'lucide-react';
import { useTheme } from 'next-themes';
export default function ThemeSwitcher() {
  const { setTheme, resolvedTheme } = useTheme();

  return (
    <>
      <button onClick={() => setTheme(resolvedTheme === 'dark' ? 'light' : 'dark')}>
        {resolvedTheme === 'dark' ? (
          <MoonIcon className="h-auto w-6" />
        ) : (
          <SunIcon className="h-auto w-6" />
        )}
      </button>
    </>
  );
}
