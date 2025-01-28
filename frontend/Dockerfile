# Stage 1: Build Angular app
FROM node:22 AS build
WORKDIR /app

# Install Angular CLI globally
RUN npm install -g @angular/cli

COPY package.json package-lock.json ./
RUN npm install
COPY . .

# Build the Angular app
RUN ng build --configuration production

# Stage 2: Serve Angular app with Nginx
FROM nginx:alpine


# Copy built Angular files from dist/frontend/browser to Nginx's serving directory
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

# Expose port 80
EXPOSE 80

# Start Nginx to serve the Angular app
CMD ["nginx", "-g", "daemon off;"]
