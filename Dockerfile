ARG PROVIDER_VERSION

# Build provider.
FROM maven:3-openjdk-17 as provider
ARG PROVIDER_VERSION
COPY providers/keycloak-yandex /src
RUN cd /src && mvn package

# Configure Keycloak.
FROM quay.io/keycloak/keycloak:17.0.1 as keycloak
ARG PROVIDER_VERSION
COPY --from=provider /src/target/keycloak-yandex-${PROVIDER_VERSION}.jar /opt/keycloak/providers
COPY themes/arenadata/ /opt/keycloak/themes/arenadata/
RUN /opt/keycloak/bin/kc.sh build --db=postgres --metrics-enabled=true --features=token-exchange

# Build Keycloak.
FROM quay.io/keycloak/keycloak:17.0.1
ARG PROVIDER_VERSION
COPY --from=keycloak /opt/keycloak/lib/quarkus/ /opt/keycloak/lib/quarkus/
COPY --from=keycloak /opt/keycloak/providers/ /opt/keycloak/providers/
COPY --from=keycloak /opt/keycloak/themes/ /opt/keycloak/themes/
WORKDIR /opt/keycloak
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
