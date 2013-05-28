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
package org.orcid.api.common.writer.rdf;

import static org.orcid.api.common.OrcidApiConstants.APPLICATION_RDFXML;
import static org.orcid.api.common.OrcidApiConstants.PROFILE_POST_PATH;
import static org.orcid.api.common.OrcidApiConstants.TEXT_N3;
import static org.orcid.api.common.OrcidApiConstants.TEXT_TURTLE;
import static org.orcid.api.common.OrcidApiConstants.WORKS_PATH;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.Charset;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.orcid.api.common.OrcidApiService;
import org.orcid.jaxb.model.message.ErrorDesc;
import org.orcid.jaxb.model.message.OrcidMessage;
import org.orcid.jaxb.model.message.OrcidProfile;
import org.orcid.jaxb.model.message.PersonalDetails;
import org.springframework.beans.factory.annotation.Value;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * 2013 ORCID
 * 
 * @author Stian Soiland-Reyes
 */
@Provider
@Produces( { APPLICATION_RDFXML, TEXT_TURTLE, TEXT_N3 })
public class RDFMessageBodyWriter implements MessageBodyWriter<OrcidMessage> {
    
    private static final String EN = "en";
    private static final String FOAF_RDF = "foaf.rdf";
    private static final String PAV = "http://purl.org/pav/";
    private static final String PAV_RDF = "pav.rdf";
    private static final String PROV_O_RDF = "prov-o.rdf";
    private static final String PROV = "http://www.w3.org/prov#";
    private static final String PROV_O = "http://www.w3.org/prov-o#";
    private static final String FOAF_0_1 = "http://xmlns.com/foaf/0.1/";
    protected static final String TMP_BASE = "app://614879b4-48c3-45ab-a828-2a72e43f80d9/";
    private static final Charset UTF8 = Charset.forName("UTF-8");
    
    private DatatypeProperty foafName;
    private DatatypeProperty foafGivenName;
    private DatatypeProperty foafFamilyName;
    private OntClass foafPerson;
    private OntClass foafOnlineAccount;
    private ObjectProperty foafAccount;
    private ObjectProperty foafAccountServiceHomepage;

    @Value("${org.orcid.core.baseUri:http://orcid.org}")
    private String baseUri = "http://orcid.org";
    private DatatypeProperty foafAccountName;
    private ObjectProperty foafPrimaryTopic;
    private ObjectProperty foafPublications;
    private OntClass foafPersonalProfileDocument;
    private OntModel prov;
    private OntModel foaf;
    private OntModel pav;

    @Context
    private UriInfo uriInfo;

    
    /**
     * Ascertain if the MessageBodyWriter supports a particular type.
     * 
     * @param type
     *            the class of object that is to be written.
     * @param genericType
     *            the type of object to be written, obtained either by
     *            reflection of a resource method return type or via inspection
     *            of the returned instance.
     *            {@link javax.ws.rs.core.GenericEntity} provides a way to
     *            specify this information at runtime.
     * @param annotations
     *            an array of the annotations on the resource method that
     *            returns the object.
     * @param mediaType
     *            the media type of the HTTP entity.
     * @return true if the type is supported, otherwise false.
     */
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return OrcidMessage.class.isAssignableFrom(type);
    }

    /**
     * Called before <code>writeTo</code> to ascertain the length in bytes of
     * the serialized form of <code>t</code>. A non-negative return value is
     * used in a HTTP <code>Content-Length</code> header.
     * 
     * @param message
     *            the instance to write
     * @param type
     *            the class of object that is to be written.
     * @param genericType
     *            the type of object to be written, obtained either by
     *            reflection of a resource method return type or by inspection
     *            of the returned instance.
     *            {@link javax.ws.rs.core.GenericEntity} provides a way to
     *            specify this information at runtime.
     * @param annotations
     *            an array of the annotations on the resource method that
     *            returns the object.
     * @param mediaType
     *            the media type of the HTTP entity.
     * @return length in bytes or -1 if the length cannot be determined in
     *         advance
     */
    @Override
    public long getSize(OrcidMessage message, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        // TODO: Can we calculate the size in advance? 
        // It would mean buffering up the actual RDF
        return -1;
    }

    /**
     * Write a type to an HTTP response. The response header map is mutable but
     * any changes must be made before writing to the output stream since the
     * headers will be flushed prior to writing the response body.
     * 
     * @param message
     *            the instance to write.
     * @param type
     *            the class of object that is to be written.
     * @param genericType
     *            the type of object to be written, obtained either by
     *            reflection of a resource method return type or by inspection
     *            of the returned instance.
     *            {@link javax.ws.rs.core.GenericEntity} provides a way to
     *            specify this information at runtime.
     * @param annotations
     *            an array of the annotations on the resource method that
     *            returns the object.
     * @param mediaType
     *            the media type of the HTTP entity.
     * @param httpHeaders
     *            a mutable map of the HTTP response headers.
     * @param entityStream
     *            the {@link java.io.OutputStream} for the HTTP entity. The
     *            implementation should not close the output stream.
     * @throws java.io.IOException
     *             if an IO error arises
     * @throws javax.ws.rs.WebApplicationException
     *             if a specific HTTP error response needs to be produced. Only
     *             effective if thrown prior to the response being committed.
     */
    @Override
    public void writeTo(OrcidMessage xml, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
        OutputStream entityStream) throws IOException, WebApplicationException {

        OntModel m = getOntModel();

        if (xml.getErrorDesc() != null) {
            describeError(xml.getErrorDesc(), m);
        }
        
        OrcidProfile orcidProfile = xml.getOrcidProfile();
        System.out.println(httpHeaders);
        if (orcidProfile != null) {        
            Individual person = describePerson(orcidProfile, m);        
            if (person != null) {
                Individual account = describeAccount(orcidProfile, m, person);
            }
        }
        MediaType rdfXml = new MediaType("application", "rdf+xml");
        if (mediaType.isCompatible(rdfXml)) {
            m.write(entityStream, "RDF/XML", TMP_BASE); 
        } else {
            // Silly workaround to generate relative URIs 


            // The below would not correctly relativize according to TMP_BASE
            // https://issues.apache.org/jira/browse/JENA-132 
            // m.write(entityStream, "N3", TMP_BASE);

            StringWriter writer = new StringWriter();
            m.write(writer, "N3", TMP_BASE);
            String relativizedTurtle = writer.toString().replace(TMP_BASE, "");            
            entityStream.write(relativizedTurtle.getBytes(UTF8));           
        } 
    }

    protected void describeError(ErrorDesc errorDesc, OntModel m) {
        String error = errorDesc.getContent();
        Individual root = m.createIndividual(TMP_BASE, null);
        root.setLabel("Error", EN);
        root.setComment(error, EN);
    }

    private Individual describeAccount(OrcidProfile orcidProfile, OntModel m, Individual person) {
        // Add /orcid-profile to identify the profile itself
        String orcidProfileUri = orcidProfile.getOrcidId() + PROFILE_POST_PATH;

        Individual account = m.createIndividual(orcidProfileUri, foafOnlineAccount);
        person.addProperty(foafAccount, account);
        if (baseUri != null) {
            account.addProperty(foafAccountServiceHomepage, m.createIndividual(baseUri, null));
        }
        String orcId = orcidProfile.getOrcid().getValue();
        account.addProperty(foafAccountName, orcId);
        account.addLabel(orcId, null);
        if (orcidProfile.getOrcidHistory() != null) {
            if (orcidProfile.getOrcidHistory().isClaimed().booleanValue()) {
                // Set account as PersonalProfileDocument ?
                account.addRDFType(foafPersonalProfileDocument);
            }
        }
        account.addProperty(foafPrimaryTopic, person);

        // Links to publications resource
        UriBuilder builder = uriInfo.getBaseUriBuilder();
        // CHECK - does this get orcid-pub-web vs. orcid-web etc. wrong?
        URI worksDetails = builder.path(OrcidApiService.class, "viewWorksDetailsXml").build(orcId);
        person.addProperty(foafPublications, m.createIndividual(worksDetails.toASCIIString(), null));
        
        return account;
    }

    private Individual describePerson(OrcidProfile orcidProfile, OntModel m) {
        String orcidUri = orcidProfile.getOrcidId();
        Individual person = m.createIndividual(orcidUri, foafPerson);
        if (orcidProfile.getOrcidBio() == null) {
            return person;
        }
        PersonalDetails personalDetails = orcidProfile.getOrcidBio().getPersonalDetails();
        if (personalDetails == null) {
            return person;
        }
        
        if (personalDetails.getCreditName() != null) {
            String creditName = personalDetails.getCreditName().getContent();
            person.addProperty(foafName, creditName);
            person.addLabel(creditName, null);
        } else if (personalDetails.getGivenNames() != null && personalDetails.getFamilyName() != null ){
            // Naive combination assuming givenNames ~= first name and familyName ~= lastName
            // See http://www.w3.org/International/questions/qa-personal-names for further
            // considerations -- we don't report this as foaf:name as we can't be sure
            // NOTE: ORCID gui is westernized asking for "First name" and
            // "Last name" and assuming the above mapping
            String label = personalDetails.getGivenNames() + " "  + personalDetails.getFamilyName();
            person.addLabel(label, null);
        }
        
        if (personalDetails.getGivenNames() != null) {
            person.addProperty(foafGivenName, personalDetails.getGivenNames().getContent());
        }
        if (personalDetails.getFamilyName() != null) {
            person.addProperty(foafFamilyName, personalDetails.getFamilyName().getContent());
        }
        
        return person;
    }

    protected OntModel getOntModel() {
        if (foaf == null) {
            loadFoaf();
        }
        if (prov == null) {
            loadProv();
        }
        if (pav == null) {
            loadPav();
        }
        
        
        OntModel ontModel = ModelFactory.createOntologyModel();
        ontModel.setNsPrefix("foaf", FOAF_0_1);
        //ontModel.getDocumentManager().loadImports(foaf.getOntModel());
        return ontModel;
    }

    protected synchronized void loadPav() {
        if (pav != null) {
            return;
        }
        OntModel ontModel = loadOntologyFromClasspath(PAV_RDF, PAV);            
        pav = ontModel;            
    }
    
    protected synchronized void loadProv() {
        if (prov != null) {
            return;
        }
        OntModel ontModel = loadOntologyFromClasspath(PROV_O_RDF, PROV_O);
        
        prov = ontModel;
    }

    protected OntModel loadOntologyFromClasspath(String classPathUri, String uri) {
        OntModel ontModel = ModelFactory.createOntologyModel();

        // Load from classpath
        InputStream inStream = getClass().getResourceAsStream(classPathUri);
        if (inStream == null) {
            throw new IllegalArgumentException("Can't load " + classPathUri);
        }
        Ontology ontology = ontModel.createOntology(uri);
        ontModel.read(inStream, uri);
        return ontModel;
    }

    protected synchronized void loadFoaf() {
        if (foaf != null) {
            return;
        }

        OntModel ontModel = loadOntologyFromClasspath(FOAF_RDF, FOAF_0_1);            
        
        // foaf = ontModel.getOntology(FOAF_0_1);

        // classes from foaf
        foafPerson =  ontModel.getOntClass(FOAF_0_1 + "Person");
        foafOnlineAccount =  ontModel.getOntClass(FOAF_0_1 + "OnlineAccount");
        foafPersonalProfileDocument =  ontModel.getOntClass(FOAF_0_1 + "PersonalProfileDocument");
        
        // properties from foaf
        foafName = ontModel.getDatatypeProperty(FOAF_0_1 + "name");
        foafGivenName = ontModel.getDatatypeProperty(FOAF_0_1 + "givenName");
        foafFamilyName = ontModel.getDatatypeProperty(FOAF_0_1 + "familyName");
        foafAccountName = ontModel.getDatatypeProperty(FOAF_0_1 + "accountName");
        
        
        foafPrimaryTopic = ontModel.getObjectProperty(FOAF_0_1 + "primaryTopic");
        foafPublications = ontModel.getObjectProperty(FOAF_0_1 + "publications");
        
        foafAccount = ontModel.getObjectProperty(FOAF_0_1 + "account");
        foafAccountServiceHomepage = ontModel.getObjectProperty(FOAF_0_1 + "accountServiceHomepage");

        foaf = ontModel;            
    }

    public UriInfo getUriInfo() {
        return uriInfo;
    }

    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }
}
