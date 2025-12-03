FROM registry.gitlab.com/pages/hugo/hugo_extended:latest as build

# Install necessary tools and upgrade packages in one layer
RUN apk add --no-cache curl git nodejs npm wget \
    && apk upgrade --no-cache --ignore alpine-baselayout \
    && curl https://htmltest.wjdp.uk | sh -s -- -b /usr/local/bin

# Install Go 1.23.2
RUN wget -q https://go.dev/dl/go1.23.2.linux-amd64.tar.gz \
    && rm -rf /usr/local/go \
    && tar -C /usr/local -xzf go1.23.2.linux-amd64.tar.gz \
    && rm go1.23.2.linux-amd64.tar.gz \
    && export PATH="$PATH":/usr/local/go/bin \
    && go env -w GOBIN=/usr/local/go/bin

# Ensure Go is in the path for subsequent commands
ENV PATH="/usr/local/go/bin:${PATH}"

WORKDIR /app
COPY . .
RUN mkdir -p themes/hugo-theme-relearn
RUN git clone -b main --single-branch https://github.com/McShelby/hugo-theme-relearn.git themes/hugo-theme-relearn

RUN mkdir -p themes/hugo-video
RUN git clone -b main --single-branch https://github.com/martignoni/hugo-video.git themes/hugo-video

RUN hugo -d public/


# Build docker image
FROM nginxinc/nginx-unprivileged:1.27-alpine3.20

COPY --from=build /app/public/ /usr/share/nginx/html

CMD ["nginx", "-g", "daemon off;"]

USER 1001
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 CMD curl -f -s localhost:8080 || exit 1

