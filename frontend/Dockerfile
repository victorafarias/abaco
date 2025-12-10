FROM node:10 AS build

WORKDIR /app

COPY . .

RUN npm install --registry=https://element.basis.com.br/repository/npm-registry

RUN npm run build

FROM nginx:stable

COPY docker/nginx/conf/ /conf/
COPY docker/nginx/certificates/ /certificates/

RUN \
    # copy configurations
    cp /conf/default.conf /conf/gzip.conf /etc/nginx/conf.d/ &&\
    cp /conf/proxy.conf /conf/env.sh /etc/nginx/ &&\
    rm -Rf /conf &&\
    # copy letsencrypt chain
    mkdir -p /etc/nginx/ssl &&\
    mkdir -p /etc/letsencrypt/production/certs/ssl &&\
    cp certificates/ca-bundle.crt /etc/nginx/ssl/ &&\
    rm -Rf /certificates

COPY --from=build /app/dist /usr/share/nginx/html

CMD ["sh", "-c", "/etc/nginx/env.sh ; nginx -g 'daemon off;'"]
