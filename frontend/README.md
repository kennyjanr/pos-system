# POS System Frontend

React + TypeScript + Tailwind CSS frontend for the Point of Sale System.

## Prerequisites

- Node.js 18+ and npm

## Installation

```bash
cd frontend
npm install
```

## Development

```bash
npm run dev
```

The frontend will be available at `http://localhost:5173`

## Build

```bash
npm run build
```

This creates an optimized production build in the `dist/` directory.

## Preview Production Build

```bash
npm run preview
```

## Environment Variables

Create a `.env` file in the frontend directory (or use `.env.local` for local overrides):

```
VITE_API_BASE_URL=http://localhost:8080/api
```

This configures the API endpoint for axios. See `src/services/api.ts` for implementation.

## Project Structure

```
src/
├── pages/          # Page components
├── components/     # Reusable components
├── services/       # API services
├── App.tsx         # Main app component
├── main.tsx        # Entry point
└── index.css       # Global styles (Tailwind)
```

## Running with Docker Compose

From the root directory:

```bash
docker-compose up --build
```

The frontend will be served at `http://localhost:5173`
