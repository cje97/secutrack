# Agent TeamCity officiel + les outils nécessaires pour SecuTrack.
FROM jetbrains/teamcity-agent:latest

USER root
RUN apt-get update \
 && apt-get install -y --no-install-recommends openjdk-21-jdk-headless curl ca-certificates unzip gnupg \
 && ln -s /usr/lib/jvm/java-21-openjdk-$(dpkg --print-architecture) /usr/lib/jvm/jdk21 \
 && curl -fsSL https://deb.nodesource.com/setup_24.x | bash - \
 && apt-get install -y --no-install-recommends nodejs \
 && rm -rf /var/lib/apt/lists/*
USER buildagent

# JDK utilisé par les builds (l'agent lui-même garde son propre Java)
ENV JDK21=/usr/lib/jvm/jdk21
