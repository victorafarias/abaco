FROM maven:3.8-jdk-8 AS build

WORKDIR /app

COPY . .

RUN mvn package -DskipTests

FROM adoptopenjdk:8u292-b10-jre-hotspot-focal

ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y --no-install-recommends fontconfig fonts-liberation wget && \
    echo "ttf-mscorefonts-installer msttcorefonts/accepted-mscorefonts-eula select true" | debconf-set-selections && \
    apt-get install -y --no-install-recommends ttf-mscorefonts-installer && \
    fc-cache -f && \
    rm -rf /var/lib/apt/lists/* /var/cache/msttcorefonts/*

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JHIPSTER_SLEEP=0 \
    JAVA_OPTS="-server -Xmx2G -XX:MinHeapFreeRatio=20 -XX:MaxHeapFreeRatio=40 -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 -Duser.timezone=America/Sao_Paulo"

COPY --from=build /app/target/*.war /app.war

COPY src/main/docker/docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

VOLUME /tmp
EXPOSE 8080
CMD ["docker-entrypoint.sh"]
