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

import static org.orcid.api.common.OrcidApiConstants.APPLICATION_RDFXML;
import static org.orcid.api.common.OrcidApiConstants.BIO_PATH;
import static org.orcid.api.common.OrcidApiConstants.BIO_SEARCH_PATH;
import static org.orcid.api.common.OrcidApiConstants.EXTERNAL_IDENTIFIER_PATH;
import static org.orcid.api.common.OrcidApiConstants.ORCID_JSON;
import static org.orcid.api.common.OrcidApiConstants.ORCID_XML;
import static org.orcid.api.common.OrcidApiConstants.PROFILE_GET_PATH;
import static org.orcid.api.common.OrcidApiConstants.STATUS_PATH;
import static org.orcid.api.common.OrcidApiConstants.TEXT_N3;
import static org.orcid.api.common.OrcidApiConstants.TEXT_TURTLE;
import static org.orcid.api.common.OrcidApiConstants.VND_ORCID_JSON;
import static org.orcid.api.common.OrcidApiConstants.VND_ORCID_XML;
import static org.orcid.api.common.OrcidApiConstants.WORKS_PATH;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.orcid.api.common.OrcidApiService;
import org.orcid.api.common.delegator.OrcidApiServiceDelegator;
import org.orcid.jaxb.model.message.OrcidMessage;
import org.orcid.jaxb.model.message.OrcidProfile;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;

/**
 * Copyright 2011-2012 ORCID
 *
 * @author Declan Newman (declan) Date: 01/03/2012
 */
@Component
@Path("/")
public class T1OrcidApiServiceImpl implements OrcidApiService<Response> {

    private static final String FOAF_0_1 = "http://xmlns.com/foaf/0.1/";
    final static Counter T1_GET_REQUESTS = Metrics.newCounter(T1OrcidApiServiceImpl.class, "T1-GET-REQUESTS");
    final static Counter T1_SEARCH_REQUESTS = Metrics.newCounter(T1OrcidApiServiceImpl.class, "T1-SEARCH-REQUESTS");

    final static Counter T1_SEARCH_RESULTS_NONE_FOUND = Metrics.newCounter(T1OrcidApiServiceImpl.class, "T1-SEARCH-RESULTS-NONE-FOUND");
    final static Counter T1_SEARCH_RESULTS_FOUND = Metrics.newCounter(T1OrcidApiServiceImpl.class, "T1-SEARCH-RESULTS-FOUND");

    @Context
    private UriInfo uriInfo;

    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @Resource(name = "orcidServiceDelegator")
    private OrcidApiServiceDelegator serviceDelegator;

    public void setServiceDelegator(OrcidApiServiceDelegator serviceDelegator) {
        this.serviceDelegator = serviceDelegator;
    }

    /**
     * @return Plain text message indicating health of service
     */
    @GET
    @Produces(value = { MediaType.TEXT_PLAIN })
    @Path(STATUS_PATH)
    public Response viewStatusText() {
        return serviceDelegator.viewStatusText();
    }

    /**
     * GETs the HTML representation of the ORCID record
     *
     * @param orcid
     *         the ORCID that corresponds to the user's record
     * @return the HTML representation of the ORCID record
     */
    @Override
    @GET
    @Produces(value = { MediaType.TEXT_HTML })
    @Path(BIO_PATH)
    public Response viewBioDetailsHtml(@PathParam("orcid") String orcid) {
        T1_GET_REQUESTS.inc();
        Response response = serviceDelegator.findBioDetails(orcid);
        return Response.fromResponse(response).header("Content-Disposition", "attachment; filename=\"" + orcid + "-bio.xml\"").build();
    }

    /**
     * GETs the XML representation of the ORCID record containing only the
     * Biography details
     *
     * @param orcid
     *         the ORCID that corresponds to the user's record
     * @return the XML representation of the ORCID record
     */
    @Override
    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML })
    @Path(BIO_PATH)
    public Response viewBioDetailsXml(@PathParam("orcid") String orcid) {
        T1_GET_REQUESTS.inc();
        return serviceDelegator.findBioDetails(orcid);
    }
    
    /**
     * GETs the RDF/XML representation of the ORCID record containing only the
     * Biography details
     * 
     * @param orcid
     *            the ORCID that corresponds to the user's record
     * @return the RDF/XML representation of the ORCID record
     */
    @Override
    @GET
    @Produces(value = { APPLICATION_RDFXML })
    @Path(BIO_PATH)
    public Response viewBioDetailsRdf(@PathParam("orcid") String orcid) {
        T1_GET_REQUESTS.inc();
        Response xmlResp = serviceDelegator.findBioDetails(orcid);
        return xmlToRdf(xmlResp, APPLICATION_RDFXML);
    }

    protected Response xmlToRdf(Response xmlResp, String mediaType) {
        OrcidMessage xml = (OrcidMessage) xmlResp.getEntity();
        
        // Create RDF model
        OntModel m = ModelFactory.createOntologyModel();
        // TODO: Load FOAF locally, and cached
        m.setDynamicImports(true);
        OrcidProfile orcidProfile = xml.getOrcidProfile();
        String profileUri = orcidProfile.getOrcid().getValue();
        Ontology ont = m.createOntology(profileUri + "#");
        ont.addImport(m.createResource(FOAF_0_1));
        m.setNsPrefix("foaf", FOAF_0_1);
        
        Individual person = m.createIndividual(profileUri, m.getOntClass(FOAF_0_1 + "Person"));
        
//        AnnotationProperty foafName = m.getAnnotationProperty(FOAF_0_1 + "name");
        DatatypeProperty foafName = m.getDatatypeProperty(FOAF_0_1 + "name");
        person.addProperty(foafName, orcidProfile.getOrcidBio().getPersonalDetails().getCreditName().getContent());
        
        // TOOD: Do a stream to Response
        StringWriter writer = new StringWriter();
        m.write(writer);
        
        String rdf = writer.toString();
        return Response.ok(rdf, mediaType).build();
    }

    /**
     * GETs the RDF Turtle representation of the ORCID record containing only the
     * Biography details
     * 
     * @param orcid
     *            the ORCID that corresponds to the user's record
     * @return the RDF Turtle representation of the ORCID record
     */
    @GET
    @Produces(value = { TEXT_N3, TEXT_TURTLE })
    @Path(BIO_PATH)
    public Response viewBioDetailsTurtle(@PathParam("orcid") String orcid) {
        T1_GET_REQUESTS.inc();
        return xmlToRdf(serviceDelegator.findBioDetails(orcid), TEXT_TURTLE);
    }

    /**
     * GETs the JSON representation of the ORCID record containing only the
     * Biography details
     *
     * @param orcid
     *         the ORCID that corresponds to the user's record
     * @return the JSON representation of the ORCID record
     */
    @Override
    @GET
    @Produces(value = { VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(BIO_PATH)
    public Response viewBioDetailsJson(@PathParam("orcid") String orcid) {
        T1_GET_REQUESTS.inc();
        return serviceDelegator.findBioDetails(orcid);
    }

    /**
     * GETs the HTML representation of the ORCID external identifiers
     *
     * @param orcid
     *         the ORCID that corresponds to the user's record
     * @return the HTML representation of the ORCID record
     */
    @Override
    @GET
    @Produces(value = { MediaType.TEXT_HTML })
    @Path(EXTERNAL_IDENTIFIER_PATH)
    public Response viewExternalIdentifiersHtml(@PathParam("orcid") String orcid) {
        T1_GET_REQUESTS.inc();
        Response response = serviceDelegator.findExternalIdentifiers(orcid);
        return Response.fromResponse(response).header("Content-Disposition", "attachment; filename=\"" + orcid + "-external-ids.xml\"").build();
    }

    /**
     * GETs the XML representation of the ORCID record containing only the
     * external identifiers
     *
     * @param orcid
     *         the ORCID that corresponds to the user's record
     * @return the XML representation of the ORCID record
     */
    @Override
    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML })
    @Path(EXTERNAL_IDENTIFIER_PATH)
    public Response viewExternalIdentifiersXml(@PathParam("orcid") String orcid) {
        T1_GET_REQUESTS.inc();
        return serviceDelegator.findExternalIdentifiers(orcid);
    }

    /**
     * GETs the JSON representation of the ORCID record containing only the
     * external identifiers
     *
     * @param orcid
     *         the ORCID that corresponds to the user's record
     * @return the JSON representation of the ORCID record
     */
    @Override
    @GET
    @Produces(value = { VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(EXTERNAL_IDENTIFIER_PATH)
    public Response viewExternalIdentifiersJson(@PathParam("orcid") String orcid) {
        T1_GET_REQUESTS.inc();
        return serviceDelegator.findExternalIdentifiers(orcid);
    }

    /**
     * GETs the HTML representation of the ORCID record containing all details
     *
     * @param orcid
     *         the ORCID that corresponds to the user's record
     * @return the HTML representation of the ORCID record
     */
    @Override
    @GET
    @Produces(value = { MediaType.TEXT_HTML })
    @Path(PROFILE_GET_PATH)
    public Response viewFullDetailsHtml(@PathParam("orcid") String orcid) {
        T1_GET_REQUESTS.inc();
        Response response = serviceDelegator.findFullDetails(orcid);
        return Response.fromResponse(response).header("Content-Disposition", "attachment; filename=\"" + orcid + "-profile.xml\"").build();
    }

    /**
     * GETs the XML representation of the ORCID record containing all details
     *
     * @param orcid
     *         the ORCID that corresponds to the user's record
     * @return the XML representation of the ORCID record
     */
    @Override
    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML })
    @Path(PROFILE_GET_PATH)
    public Response viewFullDetailsXml(@PathParam("orcid") String orcid) {
        T1_GET_REQUESTS.inc();
        return serviceDelegator.findFullDetails(orcid);
    }

    /**
     * GETs the JSON representation of the ORCID record containing all details
     *
     * @param orcid
     *         the ORCID that corresponds to the user's record
     * @return the JSON representation of the ORCID record
     */
    @Override
    @GET
    @Produces(value = { VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(PROFILE_GET_PATH)
    public Response viewFullDetailsJson(@PathParam("orcid") String orcid) {
        T1_GET_REQUESTS.inc();
        return serviceDelegator.findFullDetails(orcid);
    }

    /**
     * GETs the HTML representation of the ORCID record containing only work
     * details
     *
     * @param orcid
     *         the ORCID that corresponds to the user's record
     * @return the HTML representation of the ORCID record
     */
    @Override
    @GET
    @Produces(value = { MediaType.TEXT_HTML })
    @Path(WORKS_PATH)
    public Response viewWorksDetailsHtml(@PathParam("orcid") String orcid) {
        T1_GET_REQUESTS.inc();
        Response response = serviceDelegator.findWorksDetails(orcid);
        return Response.fromResponse(response).header("Content-Disposition", "attachment; filename=\"" + orcid + "-works.xml\"").build();
    }

    /**
     * GETs the XML representation of the ORCID record containing only work
     * details
     *
     * @param orcid
     *         the ORCID that corresponds to the user's record
     * @return the XML representation of the ORCID record
     */
    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML })
    @Path(WORKS_PATH)
    public Response viewWorksDetailsXml(@PathParam("orcid") String orcid) {
        T1_GET_REQUESTS.inc();
        return serviceDelegator.findWorksDetails(orcid);
    }

    /**
     * GETs the JSON representation of the ORCID record containing only work
     * details
     *
     * @param orcid
     *         the ORCID that corresponds to the user's record
     * @return the JSON representation of the ORCID record
     */
    @Override
    @GET
    @Produces(value = { VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(WORKS_PATH)
    public Response viewWorksDetailsJson(@PathParam("orcid") String orcid) {
        T1_GET_REQUESTS.inc();
        return serviceDelegator.findWorksDetails(orcid);
    }

    /**
     * Gets the JSON representation any Orcid Profiles (BIO) only relevant to
     * the given query
     *
     * @param query
     * @return
     */
    @Override
    @GET
    @Produces(value = { VND_ORCID_JSON, ORCID_JSON, MediaType.APPLICATION_JSON })
    @Path(BIO_SEARCH_PATH)
    public Response searchByQueryJSON(String query) {
        T1_SEARCH_REQUESTS.inc();
        Map<String, List<String>> queryParams = uriInfo.getQueryParameters();
        Response jsonQueryResults = serviceDelegator.searchByQuery(queryParams);
        registerSearchMetrics(jsonQueryResults);
        return jsonQueryResults;
    }

    /**
     * Gets the XML representation any Orcid Profiles (BIO) only relevant to the
     * given query
     *
     * @param query
     * @return
     */
    @Override
    @GET
    @Produces(value = { VND_ORCID_XML, ORCID_XML, MediaType.APPLICATION_XML })
    @Path(BIO_SEARCH_PATH)
    public Response searchByQueryXML(String query) {
        T1_SEARCH_REQUESTS.inc();
        Map<String, List<String>> queryParams = uriInfo.getQueryParameters();
        Response xmlQueryResults = serviceDelegator.searchByQuery(queryParams);
        registerSearchMetrics(xmlQueryResults);
        return xmlQueryResults;
    }

    private void registerSearchMetrics(Response results) {
        OrcidMessage orcidMessage = (OrcidMessage) results.getEntity();
        if (orcidMessage != null && orcidMessage.getOrcidSearchResults() != null && !orcidMessage.getOrcidSearchResults().getOrcidSearchResult().isEmpty()) {
            T1_SEARCH_RESULTS_FOUND.inc(orcidMessage.getOrcidSearchResults().getOrcidSearchResult().size());
            return;
        }

        T1_SEARCH_RESULTS_NONE_FOUND.inc();
    }

}
