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
package org.orcid.core.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.junit.Before;
import org.junit.Test;
import org.orcid.jaxb.model.common.ContributorEmail;
import org.orcid.jaxb.model.common.ContributorOrcid;
import org.orcid.jaxb.model.common.Country;
import org.orcid.jaxb.model.common.CreatedDate;
import org.orcid.jaxb.model.common.CreditName;
import org.orcid.jaxb.model.common.Day;
import org.orcid.jaxb.model.common.Iso3166Country;
import org.orcid.jaxb.model.common.LastModifiedDate;
import org.orcid.jaxb.model.common.Month;
import org.orcid.jaxb.model.common.PublicationDate;
import org.orcid.jaxb.model.common.Subtitle;
import org.orcid.jaxb.model.common.Title;
import org.orcid.jaxb.model.common.Url;
import org.orcid.jaxb.model.common.Year;
import org.orcid.jaxb.model.message.FuzzyDate;
import org.orcid.jaxb.model.message.WorkCategory;
import org.orcid.jaxb.model.record_rc1.CitationType;
import org.orcid.jaxb.model.record_rc1.Relationship;
import org.orcid.jaxb.model.record_rc1.Work;
import org.orcid.jaxb.model.record_rc1.WorkExternalIdentifierId;
import org.orcid.jaxb.model.record_rc1.WorkExternalIdentifierType;
import org.orcid.jaxb.model.record_rc1.WorkExternalIdentifiers;
import org.orcid.jaxb.model.record_rc1.WorkTitle;
import org.orcid.jaxb.model.record_rc1.WorkType;
import org.orcid.pojo.ajaxForm.Citation;
import org.orcid.pojo.ajaxForm.Contributor;
import org.orcid.pojo.ajaxForm.Date;
import org.orcid.pojo.ajaxForm.PojoUtil;
import org.orcid.pojo.ajaxForm.Text;
import org.orcid.pojo.ajaxForm.TranslatedTitle;
import org.orcid.pojo.ajaxForm.Visibility;
import org.orcid.pojo.ajaxForm.WorkExternalIdentifier;
import org.orcid.pojo.ajaxForm.WorkForm;
import org.springframework.test.context.ContextConfiguration;

/**
 * 
 * @author Angel Montenegro
 * 
 */
@ContextConfiguration(locations = { "classpath:orcid-core-context.xml" })
public class WorkFormTest {

    private DatatypeFactory datatypeFactory = null;

    @Before
    public void before() {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            // We're in serious trouble and can't carry on
            throw new IllegalStateException("Cannot create new DatatypeFactory");
        }
    }

    @Test
    public void equalsTest() {
        WorkForm form1 = getWorkForm();
        WorkForm form2 = getWorkForm();
        assertEquals(form1, form2);
        form1.setPutCode(Text.valueOf(String.valueOf(System.currentTimeMillis())));
        assertFalse(form1.equals(form2));
    }

    @Test
    public void toWorkTest() {
        WorkForm form = getWorkForm();
        Work work = form.toWork();
        assertEquals(getWork(), work);
    }

    @Test
    public void toWorkFormTest() {
        Work work = getWork();
        WorkForm form = WorkForm.valueOf(work);
        assertEquals(getWorkForm(), form);
    }

    private Work getWork() {
        Work work = new Work();
        work.setCountry(new Country(Iso3166Country.US));
        work.setJournalTitle(new Title("Journal title"));
        work.setLanguageCode("en");
        work.setPutCode(Long.valueOf("1"));
        work.setShortDescription("Short description");
        work.setSource(new org.orcid.jaxb.model.common.Source("0000-0000-0000-0000"));
        work.setUrl(new Url("http://myurl.com"));
        work.setVisibility(org.orcid.jaxb.model.common.Visibility.PUBLIC);
        org.orcid.jaxb.model.record_rc1.Citation citation = new org.orcid.jaxb.model.record_rc1.Citation();
        citation.setCitation("Citation");
        citation.setWorkCitationType(CitationType.FORMATTED_UNSPECIFIED);
        work.setWorkCitation(citation);
        WorkTitle title = new WorkTitle();
        title.setTitle(new Title("Title"));
        title.setTranslatedTitle(new org.orcid.jaxb.model.common.TranslatedTitle("Translated Title", "es"));
        title.setSubtitle(new Subtitle("Subtitle"));
        work.setWorkTitle(title);
        work.setWorkType(WorkType.ARTISTIC_PERFORMANCE);
        Date date = new Date();
        date.setDay("1");
        date.setMonth("1");
        date.setYear("2015");
        GregorianCalendar calendar = date.toCalendar();
        work.setCreatedDate(new CreatedDate(datatypeFactory.newXMLGregorianCalendar(calendar)));
        date = new Date();
        date.setDay("2");
        date.setMonth("2");
        date.setYear("2015");
        calendar = date.toCalendar();
        work.setLastModifiedDate(new LastModifiedDate(datatypeFactory.newXMLGregorianCalendar(calendar)));
        work.setPublicationDate(new PublicationDate(new Year(2015), new Month(3), new Day(3)));
        org.orcid.jaxb.model.record_rc1.WorkContributors contributors = new org.orcid.jaxb.model.record_rc1.WorkContributors();
        org.orcid.jaxb.model.common.Contributor contributor = new org.orcid.jaxb.model.common.Contributor();
        org.orcid.jaxb.model.common.ContributorAttributes attributes = new org.orcid.jaxb.model.common.ContributorAttributes();
        attributes.setContributorRole(org.orcid.jaxb.model.common.ContributorRole.CO_INVENTOR);
        attributes.setContributorSequence(org.orcid.jaxb.model.record_rc1.SequenceType.FIRST);
        contributor.setContributorAttributes(attributes);
        contributor.setContributorEmail(new ContributorEmail("Contributor email"));      
        ContributorOrcid contributorOrcid = new ContributorOrcid("Contributor orcid");
        contributorOrcid.setUri("Contributor uri");
        contributor.setContributorOrcid(contributorOrcid);
        CreditName creditName = new CreditName("Contributor credit name");
        creditName.setVisibility(org.orcid.jaxb.model.common.Visibility.PUBLIC);
        contributor.setCreditName(creditName);
        contributors.getContributor().add(contributor);
        work.setWorkContributors(contributors);
        WorkExternalIdentifiers externalIdentifiers = new WorkExternalIdentifiers();
        org.orcid.jaxb.model.record_rc1.WorkExternalIdentifier extId = new org.orcid.jaxb.model.record_rc1.WorkExternalIdentifier();
        extId.setWorkExternalIdentifierId(new WorkExternalIdentifierId("External Identifier ID"));
        extId.setWorkExternalIdentifierType(WorkExternalIdentifierType.ASIN);
        extId.setRelationship(Relationship.SELF);
        externalIdentifiers.getExternalIdentifier().add(extId);
        work.setWorkExternalIdentifiers(externalIdentifiers);
        return work;
    }

    private WorkForm getWorkForm() {
        WorkForm form = new WorkForm();
        form.setCitation(new Citation("Citation", "formatted-unspecified"));        
        List<Contributor> çontributors = new ArrayList<Contributor>();
        Contributor contributor = new Contributor();
        contributor.setContributorRole(Text.valueOf("co_inventor"));
        contributor.setContributorSequence(Text.valueOf("first"));
        contributor.setCreditName(Text.valueOf("Contributor credit name"));
        contributor.setCreditNameVisibility(new Visibility());
        contributor.setEmail(Text.valueOf("Contributor email"));
        contributor.setOrcid(Text.valueOf("Contributor orcid"));
        contributor.setUri(Text.valueOf("Contributor uri"));
        çontributors.add(contributor);
        form.setContributors(çontributors);
        form.setCountryCode(Text.valueOf("US"));        
        Date createdDate = new Date();
        createdDate.setDay("1");
        createdDate.setMonth("1");
        createdDate.setYear("2015");
        form.setCreatedDate(createdDate);        
        form.setJournalTitle(Text.valueOf("Journal title"));
        form.setLanguageCode(Text.valueOf("en"));
        Date lastModifiedDate = new Date();
        lastModifiedDate.setDay("2");
        lastModifiedDate.setMonth("2");
        lastModifiedDate.setYear("2015");
        form.setLastModified(lastModifiedDate);
        Date publicationDate = new Date();
        publicationDate.setDay("03");
        publicationDate.setMonth("03");
        publicationDate.setYear("2015");
        form.setPublicationDate(publicationDate);
        form.setDateSortString(PojoUtil.createDateSortString(null, new FuzzyDate(2015, 3, 3)));
        form.setPutCode(Text.valueOf("1"));
        form.setShortDescription(Text.valueOf("Short description"));
        form.setSource("0000-0000-0000-0000");        
        form.setSubtitle(Text.valueOf("Subtitle"));
        form.setTitle(Text.valueOf("Title"));
        form.setTranslatedTitle(new TranslatedTitle("Translated Title", "es"));
        form.setUrl(Text.valueOf("http://myurl.com"));
        form.setVisibility(org.orcid.jaxb.model.message.Visibility.PUBLIC);        
        List<WorkExternalIdentifier> extIds = new ArrayList<WorkExternalIdentifier>();
        WorkExternalIdentifier extId = new WorkExternalIdentifier();
        extId.setWorkExternalIdentifierId(Text.valueOf("External Identifier ID"));
        extId.setWorkExternalIdentifierType(Text.valueOf("asin"));
        extId.setRelationship(Text.valueOf(Relationship.SELF.value()));
        extIds.add(extId);
        form.setWorkExternalIdentifiers(extIds);
        form.setWorkType(Text.valueOf("artistic-performance"));
        WorkCategory category = WorkCategory.fromWorkType(WorkType.fromValue(form.getWorkType().getValue()));
        form.setWorkCategory(Text.valueOf(category.value()));
        return form;
    }
}
