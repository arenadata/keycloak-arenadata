package io.arenadata.keycloak.authenticators;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class MattermostIntegrationAuthenticator implements Authenticator {

    private static final Logger LOG = Logger.getLogger(MattermostIntegrationAuthenticator.class);

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        UserModel user = context.getUser();
        String email = user.getEmail();
        Map<String, String> config = context.getAuthenticatorConfig().getConfig();
        int mattermostId = 0;

        // Local user without email, e.g. admin, do nothing.
        if (email == null || email.trim().isEmpty()) {
            context.success();
            return;
        }

        String gitlabToken = config.get(MattermostIntegrationAuthenticatorFactory.GITLAB_TOKEN);
        String gitlabURI = config.get(MattermostIntegrationAuthenticatorFactory.GITLAB_URI);

        try {
            JsonNode searchUserResponse = SimpleHttp.doGet(
                    gitlabURI + "/api/v4/users?search=" + email, context.getSession())
                    .header("PRIVATE-TOKEN", gitlabToken)
                    .asJson();
            if (searchUserResponse.size() > 0) {
                mattermostId = searchUserResponse.get(0)
                        .get("id").asInt(0);
            } else {
                GitlabCreateUserRequest createUserRequest = new GitlabCreateUserRequest();
                createUserRequest.setEmail(email);
                createUserRequest.setName(user.getFirstName() + " " + user.getLastName());
                createUserRequest.setUsername(user.getUsername());
                JsonNode createUserResponse = SimpleHttp.doPost(gitlabURI + "/api/v4/users", context.getSession())
                        .header("PRIVATE-TOKEN", gitlabToken)
                        .json(createUserRequest)
                        .asJson();
                if (createUserResponse != null) {
                    mattermostId = createUserResponse.get("id").asInt(0);
                }
            }
            if (mattermostId > 0) {
                user.setSingleAttribute(
                        "mattermostid", Integer.toString(mattermostId));
            }
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        context.success();
    }

    @Override
    public void action(AuthenticationFlowContext context) {
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void close() {
    }

    static class GitlabCreateUserRequest {
        private String email;
        private String name;
        private String username;
        private boolean forceRandomPassword = true;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        @JsonProperty("force_random_password")
        public boolean getForceRandomPassword() {
            return forceRandomPassword;
        }

        @JsonProperty("force_random_password")
        public void setForceRandomPassword(boolean forceRandomPassword) {
            this.forceRandomPassword = forceRandomPassword;
        }
    }
}
