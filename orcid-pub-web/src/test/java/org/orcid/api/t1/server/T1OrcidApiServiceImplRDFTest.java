/**
 * =============================================================================
 *
 * ORCID (R) Open Source
 * http://orcid.org
 *
 * Copyright (c) 2012-2013 ORCID, Inc.
 * Licensed under an MIT-Style License (MIT)
 * http://orcid.org/open-source-license
 *
 * This copyright and license information (including a link to the full license)
 * shall be included in its entirety in all copies or substantial portion of
 * the software.
 *
 * =============================================================================
 */
package org.orcid.api.t1.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.orcid.api.common.OrcidApiService;
import org.orcid.api.common.delegator.OrcidApiServiceDelegator;
import org.orcid.jaxb.model.message.ContactDetails;
import org.orcid.jaxb.model.message.CreditName;
import org.orcid.jaxb.model.message.Email;
import org.orcid.jaxb.model.message.FamilyName;
import org.orcid.jaxb.model.message.GivenNames;
import org.orcid.jaxb.model.message.OrcidBio;
import org.orcid.jaxb.model.message.OrcidMessage;
import org.orcid.jaxb.model.message.OrcidProfile;
import org.orcid.jaxb.model.message.OrcidSearchResult;
import org.orcid.jaxb.model.message.OrcidSearchResults;
import org.orcid.jaxb.model.message.OtherNames;
import org.orcid.jaxb.model.message.PersonalDetails;

import com.sun.jersey.core.util.MultivaluedMapImpl;

public class T1OrcidApiServiceImplRDFTest {

    @Resource
    private OrcidApiService<Response> t1OrcidApiService = new T1OrcidApiServiceImpl();


    private final Response successResponse = Response.ok().build();

    private OrcidApiServiceDelegator mockServiceDelegator;

    @Before
    public void subvertDelegator() {
        mockServiceDelegator = mock(OrcidApiServiceDelegator.class);
        // view status is always fine
        when(mockServiceDelegator.viewStatusText()).thenReturn(successResponse);
        ((T1OrcidApiServiceImpl)t1OrcidApiService).setServiceDelegator(mockServiceDelegator);
    }

    private Response fakeBio() {
        OrcidMessage orcidMessage = new OrcidMessage();
        OrcidProfile orcidProfile1 = new OrcidProfile();
        orcidProfile1.setOrcid("000-1337");
        orcidProfile1.setOrcidId("000-1337");
        OrcidBio bio = new OrcidBio();
        orcidProfile1.setOrcidBio(bio);
        PersonalDetails personal = new PersonalDetails();
        bio.setPersonalDetails(personal);
        personal.setFamilyName(new FamilyName("Doe"));
        personal.setCreditName(new CreditName("John F Doe"));
        personal.setGivenNames(new GivenNames("John F"));
        personal.setOtherNames(new OtherNames());
        personal.getOtherNames().addOtherName("Johnny");
        personal.getOtherNames().addOtherName("Mr Doe");

        bio.setContactDetails(new ContactDetails());
        bio.getContactDetails().setEmail(Arrays.asList(new Email("john@example.org"), new Email("doe@example.com")));

        
        orcidMessage.setOrcidProfile(orcidProfile1);
        return Response.ok(orcidMessage).build();

    }

    @After
    public void resetVals() {
        T1OrcidApiServiceImpl.T1_SEARCH_REQUESTS.clear();
        T1OrcidApiServiceImpl.T1_GET_REQUESTS.clear();
        T1OrcidApiServiceImpl.T1_SEARCH_RESULTS_NONE_FOUND.clear();
        T1OrcidApiServiceImpl.T1_SEARCH_RESULTS_FOUND.clear();
    }

    @Test
    public void testCounterUnaffectedByViewStatus() {
        assertTrue(T1OrcidApiServiceImpl.T1_SEARCH_REQUESTS.count() == 0);
        assertTrue(T1OrcidApiServiceImpl.T1_GET_REQUESTS.count() == 0);
        Response response = t1OrcidApiService.viewStatusText();
        assertEquals(200, response.getStatus());
        assertTrue(T1OrcidApiServiceImpl.T1_SEARCH_REQUESTS.count() == 0);
        assertTrue(T1OrcidApiServiceImpl.T1_GET_REQUESTS.count() == 0);

    }

    @Test
    public void testViewBioDetailsJson() {
        assertTrue(T1OrcidApiServiceImpl.T1_SEARCH_REQUESTS.count() == 0);
        assertTrue(T1OrcidApiServiceImpl.T1_GET_REQUESTS.count() == 0);
        when(mockServiceDelegator.findBioDetails(any(String.class))).thenReturn(fakeBio());
        assertTrue(T1OrcidApiServiceImpl.T1_SEARCH_REQUESTS.count() == 0);
        Response response = t1OrcidApiService.viewBioDetailsJson("orcid");
        assertEquals(200, response.getStatus());
        System.out.println(response.getEntity());
        assertTrue(T1OrcidApiServiceImpl.T1_GET_REQUESTS.count() == 1);
        assertTrue(T1OrcidApiServiceImpl.T1_SEARCH_REQUESTS.count() == 0);
    }

    @Test
    public void testViewExternalIdentifiersJson() {
        assertTrue(T1OrcidApiServiceImpl.T1_SEARCH_REQUESTS.count() == 0);
        assertTrue(T1OrcidApiServiceImpl.T1_GET_REQUESTS.count() == 0);
        when(mockServiceDelegator.findExternalIdentifiers(any(String.class))).thenReturn(successResponse);
        assertTrue(T1OrcidApiServiceImpl.T1_SEARCH_REQUESTS.count() == 0);
        Response response = t1OrcidApiService.viewExternalIdentifiersJson("orcid");
        assertEquals(200, response.getStatus());
        assertTrue(T1OrcidApiServiceImpl.T1_GET_REQUESTS.count() == 1);
        assertTrue(T1OrcidApiServiceImpl.T1_SEARCH_REQUESTS.count() == 0);
    }

    @Test
    public void testViewFullDetailsJson() {
        assertTrue(T1OrcidApiServiceImpl.T1_SEARCH_REQUESTS.count() == 0);
        assertTrue(T1OrcidApiServiceImpl.T1_GET_REQUESTS.count() == 0);
        when(mockServiceDelegator.findFullDetails(any(String.class))).thenReturn(successResponse);
        assertTrue(T1OrcidApiServiceImpl.T1_SEARCH_REQUESTS.count() == 0);
        Response response = t1OrcidApiService.viewFullDetailsJson("orcid");
        assertEquals(200, response.getStatus());
        assertTrue(T1OrcidApiServiceImpl.T1_GET_REQUESTS.count() == 1);
        assertTrue(T1OrcidApiServiceImpl.T1_SEARCH_REQUESTS.count() == 0);
    }

    @Test
    public void testViewWorksDetailsJson() {
        assertTrue(T1OrcidApiServiceImpl.T1_SEARCH_REQUESTS.count() == 0);
        assertTrue(T1OrcidApiServiceImpl.T1_GET_REQUESTS.count() == 0);
        when(mockServiceDelegator.findWorksDetails(any(String.class))).thenReturn(successResponse);
        assertTrue(T1OrcidApiServiceImpl.T1_SEARCH_REQUESTS.count() == 0);
        Response response = t1OrcidApiService.viewWorksDetailsJson("orcid");
        assertEquals(200, response.getStatus());
        assertTrue(T1OrcidApiServiceImpl.T1_GET_REQUESTS.count() == 1);
        assertTrue(T1OrcidApiServiceImpl.T1_SEARCH_REQUESTS.count() == 0);
    }

    private MultivaluedMap<String, String> queryParams() {
        return new MultivaluedMapImpl();
    }

    private Response orcidWithMultipleResults() {

        OrcidMessage orcidMessage = new OrcidMessage();
        OrcidProfile orcidProfile1 = new OrcidProfile();
        OrcidProfile orcidProfile2 = new OrcidProfile();
        OrcidProfile orcidProfile3 = new OrcidProfile();

        OrcidSearchResult orcidSearchResult1 = new OrcidSearchResult();
        OrcidSearchResult orcidSearchResult2 = new OrcidSearchResult();
        OrcidSearchResult orcidSearchResult3 = new OrcidSearchResult();

        orcidSearchResult1.setOrcidProfile(orcidProfile1);
        orcidSearchResult2.setOrcidProfile(orcidProfile2);
        orcidSearchResult3.setOrcidProfile(orcidProfile3);

        List<OrcidSearchResult> searchResults = new ArrayList<OrcidSearchResult>();
        searchResults.add(orcidSearchResult1);
        searchResults.add(orcidSearchResult2);
        searchResults.add(orcidSearchResult3);

        OrcidSearchResults orcidSearchResults = new OrcidSearchResults();
        orcidSearchResults.getOrcidSearchResult().addAll(searchResults);
        orcidMessage.setOrcidSearchResults(orcidSearchResults);
        return Response.ok(orcidMessage).build();

    }

}
