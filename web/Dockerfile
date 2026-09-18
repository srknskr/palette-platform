# Build Stage
FROM node:22-alpine AS build

WORKDIR /app

# Copy dependency specifications
COPY package*.json ./
RUN npm ci

# Copy source and build
COPY . .
RUN npm run build

# Production Stage: Unprivileged Nginx
FROM nginxinc/nginx-unprivileged:alpine-slim

USER nginx

# Copy built SPA assets
COPY --from=build /app/dist /usr/share/nginx/html

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 8080

CMD ["nginx", "-g", "daemon off;"]
