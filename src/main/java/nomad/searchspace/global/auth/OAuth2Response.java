package nomad.searchspace.global.auth;

import java.util.Map;

public interface OAuth2Response {

    Map<String, Object> getAttributes();

    String getProvider();

    String getProviderId();

    String getName();

    String getProfileImage();

    String getEmail();
}
