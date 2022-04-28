package io.arenadata.keycloak.providers.yandex;

import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;

public class YandexUserAttributeMapper extends AbstractJsonUserAttributeMapper {

    private static final String[] COMPATIBLE_PROVIDERS = new String[]{YandexIdentityProviderFactory.PROVIDER_ID};

    @Override
    public String[] getCompatibleProviders() {
        return COMPATIBLE_PROVIDERS;
    }

    @Override
    public String getId() {
        return "yandex-user-attribute-mapper";
    }
}