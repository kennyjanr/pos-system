export default function Home() {
  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-blue-100">
      <div className="container mx-auto px-4 py-16">
        <h1 className="text-4xl font-bold text-center text-blue-900 mb-4">
          Point of Sale System
        </h1>
        <p className="text-center text-blue-700 mb-8">
          Welcome to the POS System. More features coming soon.
        </p>
        <div className="bg-white rounded-lg shadow-md p-8 max-w-2xl mx-auto">
          <h2 className="text-2xl font-semibold text-gray-800 mb-4">Getting Started</h2>
          <p className="text-gray-600 mb-4">
            This is a placeholder page. The POS system is currently under development.
          </p>
          <ul className="list-disc list-inside text-gray-600 space-y-2">
            <li>Dashboard (coming soon)</li>
            <li>Products & Inventory (coming soon)</li>
            <li>Sales & Transactions (coming soon)</li>
            <li>Reports & Analytics (coming soon)</li>
          </ul>
        </div>
      </div>
    </div>
  )
}
