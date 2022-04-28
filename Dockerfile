ARG VERSION
ARG KEYCLOAK_VERSION=17.0.1

# Build extensions.
FROM maven:3-openjdk-17 as builder
ARG VERSION
ARG KEYCLOAK_VERSION
COPY extensions /src/extensions
RUN cd /src/extensions && mvn package

# Configure Keycloak.
FROM quay.io/keycloak/keycloak:${KEYCLOAK_VERSION} as keycloak
ARG VERSION
ARG KEYCLOAK_VERSION
COPY --from=builder \
     /src/extensions/target/keycloak-arenadata-${VERSION}.jar \
     /opt/keycloak/providers/
COPY themes/arenadata/ /opt/keycloak/themes/arenadata/
RUN /opt/keycloak/bin/kc.sh build \
    --db=postgres \
    --metrics-enabled=true \
    --features=scripts

# Build Keycloak.
FROM quay.io/keycloak/keycloak:${KEYCLOAK_VERSION}
ARG VERSION
ARG KEYCLOAK_VERSION
COPY --from=keycloak /opt/keycloak/lib/quarkus/ /opt/keycloak/lib/quarkus/
COPY --from=keycloak /opt/keycloak/providers/ /opt/keycloak/providers/
COPY --from=keycloak /opt/keycloak/themes/ /opt/keycloak/themes/
WORKDIR /opt/keycloak
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
