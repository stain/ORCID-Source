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
import static org.orcid.api.common.OrcidApiConstants.TEXT_N3;
import static org.orcid.api.common.OrcidApiConstants.TEXT_TURTLE;
import static org.orcid.api.common.OrcidApiConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

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
    
    private static final String FOAF_0_1 = "http://xmlns.com/foaf/0.1/";
    @SuppressWarnings("unused")
    private Ontology foaf;
    private DatatypeProperty foafName;
    private DatatypeProperty foafGivenName;
    private DatatypeProperty foafFamilyName;
    private OntClass foafPerson;
    private OntClass foafOnlineAccount;
    private ObjectProperty foafAccount;
    private ObjectProperty foafAccountServiceHomepage;

    @Value("${org.orcid.core.baseUri:http://orcid.org}")
    private String baseUri = "http://orcid.org";
    private OntClass foafDocument;
    private DatatypeProperty foafAccountName;
    private ObjectProperty foafPrimaryTopic;
    private OntClass foafPersonalProfileDocument;

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
        
        OrcidProfile orcidProfile = xml.getOrcidProfile();
        
        
        Individual person = describePerson(orcidProfile, m);
        
        Individual account = describeAccount(orcidProfile, m, person);
        
        
        MediaType rdfXml = new MediaType("application", "rdf+xml");
        if (mediaType.isCompatible(rdfXml)) {
            m.write(entityStream); 
        } else {
            // Must be Turtle or N3 then?
            m.write(entityStream, "N3");
        } 
    }

    private Individual describeAccount(OrcidProfile orcidProfile, OntModel m, Individual person) {
        // Add /orcid-profile to identify the profile itself
        String orcidProfileUri = orcidProfile.getOrcidId() + PROFILE_POST_PATH;

        Individual account = m.createIndividual(orcidProfileUri, foafOnlineAccount);
        person.addProperty(foafAccount, account);
        if (baseUri != null) {
            account.addProperty(foafAccountServiceHomepage, m.createIndividual(baseUri, null));
        }
        account.addProperty(foafAccountName, orcidProfile.getOrcid().getValue());
        if (orcidProfile.getOrcidHistory() != null) {
            if (orcidProfile.getOrcidHistory().isClaimed().booleanValue()) {
                // Set account as PersonalProfileDocument ?
                account.addRDFType(foafPersonalProfileDocument);
            }
        }
        account.addProperty(foafPrimaryTopic, person);
        return account;
    }

    private Individual describePerson(OrcidProfile orcidProfile, OntModel m) {
        String orcidUri = orcidProfile.getOrcidId();
        Individual person = m.createIndividual(orcidUri, foafPerson);
        PersonalDetails personalDetails = orcidProfile.getOrcidBio().getPersonalDetails();
        
        if (personalDetails.getCreditName() != null) {
            person.addProperty(foafName, personalDetails.getCreditName().getContent());
        } else {
            // TODO: Should we generate a foafName assuming first/last order?
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

        OntModel ontModel = ModelFactory.createOntologyModel();
        ontModel.setNsPrefix("foaf", FOAF_0_1);
        //ontModel.getDocumentManager().loadImports(foaf.getOntModel());
        return ontModel;
    }

    protected synchronized void loadFoaf() {
        if (foaf != null) {
            return;
        }
        OntModel ontModel = ModelFactory.createOntologyModel();

        // Load from classpath
        InputStream foafOnt = getClass().getResourceAsStream("foaf.rdf");
        foaf = ontModel.createOntology(FOAF_0_1);
        foaf.getModel().read(foafOnt, FOAF_0_1);

        // // The loaded ontology
        // foaf = ontModel.getOntology(FOAF_0_1);

        // classes from foaf
        foafDocument =  ontModel.getOntClass(FOAF_0_1 + "Document");
        foafPerson =  ontModel.getOntClass(FOAF_0_1 + "Person");
        foafOnlineAccount =  ontModel.getOntClass(FOAF_0_1 + "OnlineAccount");
        foafPersonalProfileDocument =  ontModel.getOntClass(FOAF_0_1 + "PersonalProfileDocument");
        
        // properties from foaf
        foafName = ontModel.getDatatypeProperty(FOAF_0_1 + "name");
        foafGivenName = ontModel.getDatatypeProperty(FOAF_0_1 + "givenName");
        foafFamilyName = ontModel.getDatatypeProperty(FOAF_0_1 + "familyName");
        foafAccountName = ontModel.getDatatypeProperty(FOAF_0_1 + "accountName");
        
        
        foafPrimaryTopic = ontModel.getObjectProperty(FOAF_0_1 + "primaryTopic");
        
        foafAccount = ontModel.getObjectProperty(FOAF_0_1 + "account");
        foafAccountServiceHomepage = ontModel.getObjectProperty(FOAF_0_1 + "accountServiceHomepage");

    }
}
