.PHONY: build start start-dev
-include .env
VERSION:=0.2.0
KC_DB?=postgres
KC_DB_URL?=jdbc:postgresql://localhost/keycloak
KC_DB_USERNAME?=keycloak
KC_DB_PASSWORD?=keycloak
KC_ARG?=""
IMAGE="hub.adsw.io/library/keycloak-arenadata:17-${VERSION}"
build:
	docker build --build-arg VERSION=${VERSION} -t ${IMAGE} .
start start-dev: build
	docker run -p 80:8080 \
      -e KC_DB=${KC_DB}  \
	  -e KC_DB_URL=${KC_DB_URL}  \
	  -e KC_DB_USERNAME=${KC_DB_USERNAME} \
	  -e KC_DB_PASSWORD='${KC_DB_PASSWORD}' \
	  -e KEYCLOAK_ADMIN=admin \
	  -e KEYCLOAK_ADMIN_PASSWORD=admin \
	  ${IMAGE} \
	  $@ \
	    --http-enabled=true \
		--proxy=edge \
		--hostname-strict=false ${KC_ARGS}
