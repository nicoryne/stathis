# 🌐 Stathis Web

Welcome to the **Stathis** web directory! This directory contains the **Next.js** web application, designed to provide an intuitive and responsive interface for teachers to monitor their students' conditions.

## 📁 Directory Structure

```
/web
├── public/              # Static assets (images, icons, etc.)
├── src/
│   ├── components/      # Reusable UI components
│   ├── app/             # Next.js app (routes)
│   ├── hooks/           # Custom React hooks
│   ├── context/         # Context API for state management
│   ├── services/        # API services for backend communication
│   ├── styles/          # Global and component-specific styles
|   |── lib/             # Utility library
├── .env.example         # Environment variable template
├── package.json         # Dependencies and scripts
├── next.config.js       # Next.js configuration
├── tailwind.config.js   # Tailwind CSS configuration
├── README.md            # Web documentation
```

## 🛠️ Setup & Installation

> **Prerequisites:**
>
> - Node.js & npm installed
> - Supabase account & API setup
> - TailwindCSS installed
> - ShadCN/UI installed

### 1️⃣ Clone the Repository

```sh
git clone https://github.com/nicoryne/stathis.git
cd sarismart/web
```

### 2️⃣ Install Dependencies

```sh
npm install
```

### 3️⃣ Configure Environment Variables

```sh
cp .env.example .env
```

- Fill in the required Supabase credentials (API keys, database URL, etc.)

### 4️⃣ Run the Development Server

```sh
npm run dev
```

- Open `http://localhost:3000` in your browser.

## 🎨 UI & Styling

- **Framework:** Next.js (React-based SSR & SSG)
- **Styling:** Tailwind CSS for modern UI design
- **Component Library:** shadcn/ui
- **Authentication:** Supabase Auth (JWT-based login system)

## 🛡️ Security & Best Practices

- Store sensitive credentials in environment variables.
- Sanitize and validate user inputs to prevent security vulnerabilities.
- Optimize images and assets for better performance.
- Regularly update dependencies to prevent security issues.

## 📌 Contributing

We welcome contributions! Feel free to open issues, submit pull requests, or reach out to the team.
