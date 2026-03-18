# Use an official Maven image as the base image
FROM node:20-alpine as build

WORKDIR /app
COPY . .
RUN npm ci \
    && npm run build

FROM nginx:latest
COPY --from=build /app/dist/browser /usr/share/nginx/html
COPY nginx/nginx.conf /etc/nginx/nginx.conf
# ADD (for custom error page)
COPY nginx/error.html /usr/share/nginx/html/error.html
ENV JSFOLDER=/usr/share/nginx/html
COPY nginx/25-inject-env-var.sh /docker-entrypoint.d/25-inject-env-var.sh

USER root

RUN chmod a+rwx -R /usr/share/nginx/html \
    && chmod a+x /docker-entrypoint.d/25-inject-env-var.sh \
    && chown -R nginx:nginx /var/cache/nginx

CMD ["nginx", "-g", "daemon off;"]

USER nginx
EXPOSE 4200
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 CMD curl -f -s localhost:4200 || exit 1
