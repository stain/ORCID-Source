/**
 * =============================================================================
 *
 * ORCID (R) Open Source
 * http://orcid.org
 *
 * Copyright (c) 2012-2014 ORCID, Inc.
 * Licensed under an MIT-Style License (MIT)
 * http://orcid.org/open-source-license
 *
 * This copyright and license information (including a link to the full license)
 * shall be included in its entirety in all copies or substantial portion of
 * the software.
 *
 * =============================================================================
 */
package org.orcid.core.oauth;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.orcid.core.constants.OrcidOauth2Constants;
import org.orcid.core.manager.LoadOptions;
import org.orcid.core.manager.OrcidProfileManager;
import org.orcid.jaxb.model.message.OrcidProfile;
import org.orcid.persistence.dao.OrcidOauth2AuthoriziationCodeDetailDao;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

public class OrcidTokenEnhancer implements TokenEnhancer {

    @Resource
    private OrcidProfileManager orcidProfileManager;

    @Resource
    private OrcidOauth2AuthoriziationCodeDetailDao orcidOauth2AuthoriziationCodeDetailDao;

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        if (!(accessToken instanceof DefaultOAuth2AccessToken))
            throw new UnsupportedOperationException("At this time we can handle only tokens of type DefaultOauth2AccessToken");
        DefaultOAuth2AccessToken result = (DefaultOAuth2AccessToken) accessToken;
        OrcidOauth2AuthInfo authInfo = new OrcidOauth2AuthInfo(authentication);
        String userOrcid = authInfo.getUserOrcid();

        Map<String, Object> additionalInfo = new HashMap<String, Object>();
        if (result.getAdditionalInformation() != null && !result.getAdditionalInformation().isEmpty()) {
            additionalInfo.putAll(result.getAdditionalInformation());
        }

        // If the additional info object already contains the orcid info, leave
        // it
        if (!additionalInfo.containsKey("orcid")) {
            additionalInfo.put("orcid", userOrcid);
        }

        // If the additional info object already contains the name info, leave
        // it
        if (!additionalInfo.containsKey("name")) {
            if (userOrcid != null) {
                OrcidProfile orcidProfile = orcidProfileManager.retrieveOrcidProfile(userOrcid, LoadOptions.BIO_ONLY);
                String name = orcidProfile.getOrcidBio().getPersonalDetails().retrievePublicDisplayName();
                additionalInfo.put("name", name);
            }
        }

        // Overwrite token version
        additionalInfo.put(OrcidOauth2Constants.TOKEN_VERSION, OrcidOauth2Constants.PERSISTENT_TOKEN);

        // Overwrite persistent flag
        if (isPersistentTokenEnabled(authentication.getOAuth2Request())) {
            additionalInfo.put(OrcidOauth2Constants.PERSISTENT, true);
        } else {
            additionalInfo.put(OrcidOauth2Constants.PERSISTENT, false);
        }

        // Put the updated additional info object in the result
        result.setAdditionalInformation(additionalInfo);

        return result;
    }

    /**
     * Checks the authorization code to verify if the user enable the persistent
     * token or not
     * */
    private boolean isPersistentTokenEnabled(OAuth2Request authorizationRequest) {
        if (authorizationRequest != null) {
            Map<String, String> params = authorizationRequest.getRequestParameters();
            if (params != null) {
                if (params.containsKey(OrcidOauth2Constants.IS_PERSISTENT)) {
                    String isPersistent = params.get(OrcidOauth2Constants.IS_PERSISTENT);
                    if (Boolean.valueOf(isPersistent)) {
                        return true;
                    }
                } else if (params.containsKey("code")) {
                    String code = params.get("code");
                    if (orcidOauth2AuthoriziationCodeDetailDao.find(code) != null) {
                        if (orcidOauth2AuthoriziationCodeDetailDao.isPersistentToken(code)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
