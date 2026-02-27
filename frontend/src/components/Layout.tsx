import { ReactNode } from 'react'

interface LayoutProps {
  children: ReactNode
}

export default function Layout({ children }: LayoutProps) {
  return (
    <div className="flex flex-col min-h-screen">
      <header className="bg-blue-900 text-white shadow-md">
        <nav className="container mx-auto px-4 py-4">
          <h1 className="text-2xl font-bold">POS System</h1>
        </nav>
      </header>
      <main className="flex-grow">
        {children}
      </main>
      <footer className="bg-gray-800 text-white text-center py-4 mt-8">
        <p>&copy; 2024 POS System. All rights reserved.</p>
      </footer>
    </div>
  )
}
