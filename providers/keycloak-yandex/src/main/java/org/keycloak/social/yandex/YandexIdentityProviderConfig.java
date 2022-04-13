package org.keycloak.social.yandex;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

public class YandexIdentityProviderConfig extends OAuth2IdentityProviderConfig {

    public YandexIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    public YandexIdentityProviderConfig() {
    }

    public String getHostedDomain() {
        String hostedDomain = getConfig().get("hostedDomain")       ;
        return hostedDomain == null || hostedDomain.isEmpty() ? null : hostedDomain;
    }

    public void setHostedDomain(final String hostedDomain) {
        getConfig().put("hostedDomain", hostedDomain);
    }

}