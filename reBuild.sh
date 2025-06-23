#!/bin/bash

# Step 1: Build the Spring Boot JAR
echo "🔨 Building Spring Boot application..."
./mvnw clean package -DskipTests || { echo "❌ Maven build failed"; exit 1; }

# Step 2: Rebuild Docker image
echo "🐳 Rebuilding Docker containers..."
docker-compose build || { echo "❌ Docker build failed"; exit 1; }

# Step 3: Restart Docker containers
echo "🔁 Restarting containers..."
docker-compose down
docker-compose up

echo "✅ Application rebuilt and running in Docker!"
