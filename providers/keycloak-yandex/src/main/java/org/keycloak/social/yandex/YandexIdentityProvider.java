package org.keycloak.social.yandex;

import com.fasterxml.jackson.databind.JsonNode;
import org.jboss.logging.Logger;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;

public class YandexIdentityProvider extends AbstractOAuth2IdentityProvider<YandexIdentityProviderConfig>
        implements SocialIdentityProvider<YandexIdentityProviderConfig> {

    private static final Logger log = Logger.getLogger(YandexIdentityProvider.class);

    public static final String AUTH_URL = "https://oauth.yandex.ru/authorize";
    public static final String TOKEN_URL = "https://oauth.yandex.ru/token";
    public static final String PROFILE_URL = "https://login.yandex.ru/info";
    public static final String DEFAULT_SCOPE = "";

    public YandexIdentityProvider(KeycloakSession session, YandexIdentityProviderConfig config) {
        super(session, config);
        config.setAuthorizationUrl(AUTH_URL);
        config.setTokenUrl(TOKEN_URL);
        config.setUserInfoUrl(PROFILE_URL);
    }

    @Override
    protected boolean supportsExternalExchange() {
        return true;
    }

    @Override
    protected String getProfileEndpointForValidation(EventBuilder event) {
        return PROFILE_URL;
    }

    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(EventBuilder event, JsonNode profile) {
        BrokeredIdentityContext user = new BrokeredIdentityContext(getJsonProperty(profile, "id"));
        String domain = ((YandexIdentityProviderConfig) getConfig()).getHostedDomain();

        String email = getJsonProperty(profile, "default_email");
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Can not get user's email.");
        }
        if (domain != null && !domain.isEmpty() && !email.endsWith("@" + domain)) {
            throw new IllegalArgumentException("Hosted domain does not match.");
        }
        String login = getJsonProperty(profile, "login");
        if (login == null || login.trim().isEmpty()) {
            user.setUsername(email);
        } else {
            user.setUsername(login);
        }
        user.setEmail(email);
        user.setFirstName(getJsonProperty(profile, "first_name"));
        user.setLastName(getJsonProperty(profile, "last_name"));

        user.setIdpConfig(getConfig());
        user.setIdp(this);

        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, profile, getConfig().getAlias());

        return user;
    }

    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken) {
        log.debug("doGetFederatedIdentity()");
        JsonNode profile = null;
        try {
            profile = SimpleHttp.doGet(PROFILE_URL + "?oauth_token=" + accessToken, session).asJson();
            return extractIdentityFromProfile(null, profile);
        } catch (Exception e) {
            throw new IdentityBrokerException("Could not obtain user profile from Yandex: " + e.getMessage(), e);
        }
    }

    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPE;
    }
}
