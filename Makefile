.PHONY: build start start-dev
-include .env
PROVIDER_VERSION:=0.1.0
KC_DB?=postgres
KC_DB_URL?=jdbc:postgresql://localhost/keycloak
KC_DB_USERNAME?=keycloak
KC_DB_PASSWORD?=keycloak
KC_ARG?=""
IMAGE="hub.adsw.io/library/keycloak-arenadata:17-${PROVIDER_VERSION}"
build:
	docker build --build-arg PROVIDER_VERSION=${PROVIDER_VERSION} -t ${IMAGE} .
start start-dev: build
	docker run -p 80:8080 \
      -e KC_DB=${KC_DB}  \
	  -e KC_DB_URL=${KC_DB_URL}  \
	  -e KC_DB_USERNAME=${KC_DB_USERNAME} \
	  -e KC_DB_PASSWORD='${KC_DB_PASSWORD}' \
	  ${IMAGE} \
	  $@ \
	    --http-enabled=true \
		--proxy=edge \
		--hostname-strict=false ${KC_ARGS}
