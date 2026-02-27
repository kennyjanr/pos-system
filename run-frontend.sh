#!/bin/bash

# POS System Frontend Startup Script
# Runs Vite dev server on port 5173

export VITE_API_BASE_URL='http://localhost:8080/api'

cd "$(dirname "$0")/frontend"

if [ ! -d node_modules ]; then
    echo "Installing npm dependencies..."
    npm install
fi

echo "Starting POS System Frontend..."
echo "API Base URL: $VITE_API_BASE_URL"
echo "Dev Server: http://localhost:5173"
echo "---"

npm run dev
