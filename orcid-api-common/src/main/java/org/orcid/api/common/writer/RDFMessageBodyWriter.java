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
package org.orcid.api.common.writer;

import static org.orcid.api.common.OrcidApiConstants.APPLICATION_RDFXML;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.orcid.jaxb.model.message.OrcidMessage;
import org.orcid.jaxb.model.message.OrcidProfile;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * 2011-2013 ORCID
 * 
 * @author Stian Soiland-Reyes
 * @author Declan Newman (declan) Date: 02/03/2012
 */
@Provider
@Produces( { APPLICATION_RDFXML })
public class RDFMessageBodyWriter implements MessageBodyWriter<OrcidMessage> {
    
    private static final String FOAF_0_1 = "http://xmlns.com/foaf/0.1/";

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
        return (message != null && message.toString() != null) ? message.toString().getBytes().length : -1;
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
            
//            AnnotationProperty foafName = m.getAnnotationProperty(FOAF_0_1 + "name");
            DatatypeProperty foafName = m.getDatatypeProperty(FOAF_0_1 + "name");
            person.addProperty(foafName, orcidProfile.getOrcidBio().getPersonalDetails().getCreditName().getContent());

            DatatypeProperty foafGivenName = m.getDatatypeProperty(FOAF_0_1 + "givenName");
            person.addProperty(foafGivenName, orcidProfile.getOrcidBio().getPersonalDetails().getGivenNames().getContent());

            DatatypeProperty familyName = m.getDatatypeProperty(FOAF_0_1 + "familyName");
            person.addProperty(familyName, orcidProfile.getOrcidBio().getPersonalDetails().getFamilyName().getContent());
            
            m.write(entityStream);
    }
}
