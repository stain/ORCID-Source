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
package org.orcid.integration.api.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.orcid.integration.api.t2.BaseT2OrcidOAuthApiClientIntegrationTest;
import org.orcid.jaxb.model.message.OrcidMessage;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.sun.jersey.api.client.ClientResponse;

@RunWith(SpringJUnit4ClassRunner.class)
public class T2OrcidOAuthApiClientReadOnlyIntegrationTest extends BaseT2OrcidOAuthApiClientIntegrationTest {

    @Test
    public void testViewBioDetailsXml() throws Exception {

        createNewOrcidUsingAccessToken();
        // doesn't matter which format we use to create - it's only to get the
        // orcid back from the header location
        assertClientResponse401Details(oauthT2Client1_2_rc6.viewBioDetailsXml(this.orcid, null));
        // doesn't matter which format we use to create - it's only to get the
        // orcid back from the header location

        ClientResponse clientResponse = oauthT2Client1_2_rc6.viewBioDetailsXml(this.orcid, accessToken);
        assertNotNull(clientResponse);
        assertEquals(200, clientResponse.getStatus());
        assertEquals("application/vnd.orcid+xml; charset=UTF-8; qs=5", clientResponse.getType().toString());
        OrcidMessage orcidMessage = clientResponse.getEntity(OrcidMessage.class);
        assertNotNull(orcidMessage);
        assertEquals(this.orcid, orcidMessage.getOrcidProfile().getOrcidIdentifier().getPath());
        assertNotNull(orcidMessage.getOrcidProfile().getOrcidBio());
        assertNull(orcidMessage.getOrcidProfile().retrieveOrcidWorks());       
        orcidClientDataHelper.deleteOrcidProfile(this.orcid);
    }

    @Test
    public void testViewBioDetailsJson() throws Exception {

        // doesn't matter which format we use to create - it's only to get the
        // orcid back from the header location

        createNewOrcidUsingAccessToken();
        assertClientResponse401Details(oauthT2Client1_2_rc6.viewBioDetailsJson(this.orcid, null));
        ClientResponse clientResponse = oauthT2Client1_2_rc6.viewBioDetailsJson(this.orcid, accessToken);
        assertNotNull(clientResponse);
        assertEquals(200, clientResponse.getStatus());
        assertEquals("application/vnd.orcid+json; charset=UTF-8; qs=4", clientResponse.getType().toString());
        OrcidMessage orcidMessage = clientResponse.getEntity(OrcidMessage.class);
        assertEquals(this.orcid, orcidMessage.getOrcidProfile().getOrcidIdentifier().getPath());
        assertNotNull(orcidMessage.getOrcidProfile().getOrcidBio());
        assertNull(orcidMessage.getOrcidProfile().retrieveOrcidWorks());        
        orcidClientDataHelper.deleteOrcidProfile(this.orcid);
    }

    @Test
    public void testViewFullDetailsXml() throws Exception {
        createNewOrcidUsingAccessToken();
        assertClientResponse401Details(oauthT2Client1_2_rc6.viewFullDetailsXml(this.orcid, null));
        ClientResponse clientResponse = oauthT2Client1_2_rc6.viewFullDetailsXml(this.orcid, accessToken);
        assertNotNull(clientResponse);
        assertEquals(200, clientResponse.getStatus());
        assertEquals("application/vnd.orcid+xml; charset=UTF-8; qs=5", clientResponse.getType().toString());
        OrcidMessage orcidMessage = clientResponse.getEntity(OrcidMessage.class);
        assertNotNull(orcidMessage);
        assertEquals(this.orcid, orcidMessage.getOrcidProfile().getOrcidIdentifier().getPath());
        assertNotNull(orcidMessage.getOrcidProfile().getOrcidBio());
        assertNotNull(orcidMessage.getOrcidProfile().retrieveOrcidWorks());
        assertNotNull(orcidMessage.getOrcidProfile().getOrcidHistory());
    }

    @Test
    public void testViewWorksDetailsXml() throws Exception {
        createNewOrcidUsingAccessToken();
        assertClientResponse401Details(oauthT2Client1_2_rc6.viewWorksDetailsXml(this.orcid, null));
        ClientResponse clientResponse = oauthT2Client1_2_rc6.viewWorksDetailsXml(this.orcid, accessToken);
        assertNotNull(clientResponse);
        assertEquals(200, clientResponse.getStatus());
        assertEquals("application/vnd.orcid+xml; charset=UTF-8; qs=5", clientResponse.getType().toString());
        OrcidMessage orcidMessage = clientResponse.getEntity(OrcidMessage.class);
        assertNotNull(orcidMessage);
        assertEquals(this.orcid, orcidMessage.getOrcidProfile().getOrcidIdentifier().getPath());
        assertNotNull(orcidMessage.getOrcidProfile().retrieveOrcidWorks());
        assertNull(orcidMessage.getOrcidProfile().getOrcidBio());
        assertNotNull(orcidMessage.getOrcidProfile().getOrcidHistory());
    }

    @Test
    public void testViewWorksDetailsJson() throws Exception {
        createNewOrcidUsingAccessToken();
        assertClientResponse401Details(oauthT2Client1_2_rc6.viewWorksDetailsJson(this.orcid, null));
        ClientResponse clientResponse = oauthT2Client1_2_rc6.viewWorksDetailsJson(this.orcid, accessToken);
        assertNotNull(clientResponse);
        assertEquals(200, clientResponse.getStatus());
        assertEquals("application/vnd.orcid+json; charset=UTF-8; qs=4", clientResponse.getType().toString());
        OrcidMessage orcidMessage = clientResponse.getEntity(OrcidMessage.class);
        assertNotNull(orcidMessage);
        assertEquals(this.orcid, orcidMessage.getOrcidProfile().getOrcidIdentifier().getPath());
        assertNotNull(orcidMessage.getOrcidProfile().retrieveOrcidWorks());
        assertNull(orcidMessage.getOrcidProfile().getOrcidBio());
        assertNotNull(orcidMessage.getOrcidProfile().getOrcidHistory());
    }

    @Test
    public void testViewFullDetailsJson() throws Exception {
        createNewOrcidUsingAccessToken();
        assertClientResponse401Details(oauthT2Client1_2_rc6.viewWorksDetailsJson(this.orcid, null));
        ClientResponse clientResponse = oauthT2Client1_2_rc6.viewFullDetailsJson(this.orcid, accessToken);
        assertNotNull(clientResponse);
        assertEquals(200, clientResponse.getStatus());
        assertEquals("application/vnd.orcid+json; charset=UTF-8; qs=4", clientResponse.getType().toString());
        OrcidMessage orcidMessage = clientResponse.getEntity(OrcidMessage.class);
        assertNotNull(orcidMessage);

        assertEquals(this.orcid, orcidMessage.getOrcidProfile().getOrcidIdentifier().getPath());
        assertNotNull(orcidMessage.getOrcidProfile().getOrcidBio());
        assertNotNull(orcidMessage.getOrcidProfile().retrieveOrcidWorks());
        assertNotNull(orcidMessage.getOrcidProfile().getOrcidHistory());
    }

}
