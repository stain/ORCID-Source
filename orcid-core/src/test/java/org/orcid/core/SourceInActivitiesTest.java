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
package org.orcid.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;

import javax.annotation.Resource;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.orcid.core.exception.ActivityIdentifierValidationException;
import org.orcid.core.exception.ActivityTitleValidationException;
import org.orcid.core.exception.InvalidPutCodeException;
import org.orcid.core.manager.AffiliationsManager;
import org.orcid.core.manager.OrcidProfileManager;
import org.orcid.core.manager.OrgManager;
import org.orcid.core.manager.PeerReviewManager;
import org.orcid.core.manager.ProfileFundingManager;
import org.orcid.core.manager.SourceManager;
import org.orcid.core.manager.WorkManager;
import org.orcid.jaxb.model.common.Organization;
import org.orcid.jaxb.model.common.OrganizationAddress;
import org.orcid.jaxb.model.common.Title;
import org.orcid.jaxb.model.common.Url;
import org.orcid.jaxb.model.message.ActivitiesVisibilityDefault;
import org.orcid.jaxb.model.message.Claimed;
import org.orcid.jaxb.model.message.ContactDetails;
import org.orcid.jaxb.model.message.CreationMethod;
import org.orcid.jaxb.model.message.FamilyName;
import org.orcid.jaxb.model.message.GivenNames;
import org.orcid.jaxb.model.message.OrcidBio;
import org.orcid.jaxb.model.message.OrcidHistory;
import org.orcid.jaxb.model.message.OrcidInternal;
import org.orcid.jaxb.model.message.OrcidProfile;
import org.orcid.jaxb.model.message.PersonalDetails;
import org.orcid.jaxb.model.message.Preferences;
import org.orcid.jaxb.model.message.SendChangeNotifications;
import org.orcid.jaxb.model.message.SendOrcidNews;
import org.orcid.jaxb.model.message.SubmissionDate;
import org.orcid.jaxb.model.message.Visibility;
import org.orcid.jaxb.model.record_rc1.Education;
import org.orcid.jaxb.model.record_rc1.Funding;
import org.orcid.jaxb.model.record_rc1.FundingExternalIdentifier;
import org.orcid.jaxb.model.record_rc1.FundingExternalIdentifierType;
import org.orcid.jaxb.model.record_rc1.FundingExternalIdentifiers;
import org.orcid.jaxb.model.record_rc1.FundingTitle;
import org.orcid.jaxb.model.record_rc1.PeerReview;
import org.orcid.jaxb.model.record_rc1.PeerReviewType;
import org.orcid.jaxb.model.record_rc1.Relationship;
import org.orcid.jaxb.model.record_rc1.Role;
import org.orcid.jaxb.model.record_rc1.Work;
import org.orcid.jaxb.model.record_rc1.WorkExternalIdentifier;
import org.orcid.jaxb.model.record_rc1.WorkExternalIdentifierId;
import org.orcid.jaxb.model.record_rc1.WorkExternalIdentifierType;
import org.orcid.jaxb.model.record_rc1.WorkExternalIdentifiers;
import org.orcid.jaxb.model.record_rc1.WorkTitle;
import org.orcid.jaxb.model.record_rc1.WorkType;
import org.orcid.persistence.jpa.entities.ClientDetailsEntity;
import org.orcid.persistence.jpa.entities.ProfileEntity;
import org.orcid.persistence.jpa.entities.ProfileFundingEntity;
import org.orcid.persistence.jpa.entities.SourceEntity;
import org.orcid.pojo.ajaxForm.PojoUtil;
import org.orcid.utils.DateUtils;

/**
 * 
 * @author Angel Montenegro
 * 
 */
public class SourceInActivitiesTest extends BaseTest {

    private static final String CLIENT_1_ID = "APP-5555555555555555";
    private static final String CLIENT_2_ID = "APP-5555555555555556";

    @Resource
    private OrcidProfileManager orcidProfileManager;

    @Resource
    private WorkManager workManager;

    @Resource
    ProfileFundingManager profileFundingManager;

    @Resource
    AffiliationsManager affiliationsManager;
    
    @Resource
    private PeerReviewManager peerReviewManager;

    @Resource
    private OrgManager orgManager;

    @Mock
    private SourceManager sourceManager;

    static String userOrcid = null;
    static Organization organization = null;
    
    @BeforeClass
    public static void initDBUnitData() throws Exception {
        initDBUnitData(Arrays.asList("/data/SecurityQuestionEntityData.xml", "/data/SourceClientDetailsEntityData.xml"));
    }

    @Before
    public void before() {        
        workManager.setSourceManager(sourceManager);
        profileFundingManager.setSourceManager(sourceManager);
        affiliationsManager.setSourceManager(sourceManager);
        peerReviewManager.setSourceManager(sourceManager);
        if (PojoUtil.isEmpty(userOrcid)) {
            OrcidProfile newUser = getMinimalOrcidProfile();
            userOrcid = newUser.getOrcidIdentifier().getPath();
        }
    }

    @AfterClass
    public static void after() throws Exception {
        removeDBUnitData(Arrays.asList("/data/SourceClientDetailsEntityData.xml", "/data/SecurityQuestionEntityData.xml"));
    }

    @Test
    public void sourceDoesntChange_Work_Test() {
        when(sourceManager.retrieveSourceEntity()).thenReturn(new SourceEntity(new ProfileEntity(userOrcid)));
        Work work1 = getWork(userOrcid);
        assertNotNull(work1);
        assertNotNull(work1.getWorkTitle());
        assertNotNull(work1.getWorkTitle().getTitle());
        assertFalse(PojoUtil.isEmpty(work1.getWorkTitle().getTitle().getContent()));
        assertNotNull(work1.getSource());
        assertNotNull(work1.getSource().retrieveSourcePath());
        assertEquals(userOrcid, work1.getSource().retrieveSourcePath());
        
        when(sourceManager.retrieveSourceEntity()).thenReturn(new SourceEntity(new ClientDetailsEntity(CLIENT_1_ID)));
        Work work2 = getWork(userOrcid);
        assertNotNull(work2);
        assertNotNull(work2.getWorkTitle());
        assertNotNull(work2.getWorkTitle().getTitle());
        assertFalse(PojoUtil.isEmpty(work2.getWorkTitle().getTitle().getContent()));
        assertNotNull(work2.getSource());
        assertNotNull(work2.getSource().retrieveSourcePath());
        assertEquals(CLIENT_1_ID, work2.getSource().retrieveSourcePath());

        when(sourceManager.retrieveSourceEntity()).thenReturn(new SourceEntity(new ClientDetailsEntity(CLIENT_2_ID)));
        Work work3 = getWork(userOrcid);
        assertNotNull(work3);
        assertNotNull(work3.getWorkTitle());
        assertNotNull(work3.getWorkTitle().getTitle());
        assertFalse(PojoUtil.isEmpty(work3.getWorkTitle().getTitle().getContent()));
        assertNotNull(work3.getSource());
        assertNotNull(work3.getSource().retrieveSourcePath());
        assertEquals(CLIENT_2_ID, work3.getSource().retrieveSourcePath());

        when(sourceManager.retrieveSourceEntity()).thenReturn(new SourceEntity(new ProfileEntity(userOrcid)));
        Work work4 = getWork(userOrcid);
        assertNotNull(work4);
        assertNotNull(work4.getWorkTitle());
        assertNotNull(work4.getWorkTitle().getTitle());
        assertFalse(PojoUtil.isEmpty(work4.getWorkTitle().getTitle().getContent()));        
        assertNotNull(work4.getSource());
        assertNotNull(work4.getSource().retrieveSourcePath());
        assertEquals(userOrcid, work4.getSource().retrieveSourcePath());

        Work fromDb1 = workManager.getWork(userOrcid, work1.getPutCode());
        assertNotNull(fromDb1);
        assertEquals(userOrcid, fromDb1.getSource().retrieveSourcePath());

        Work fromDb2 = workManager.getWork(userOrcid, work2.getPutCode());
        assertNotNull(fromDb2);
        assertEquals(CLIENT_1_ID, fromDb2.getSource().retrieveSourcePath());

        Work fromDb3 = workManager.getWork(userOrcid, work3.getPutCode());
        assertNotNull(fromDb3);
        assertEquals(CLIENT_2_ID, fromDb3.getSource().retrieveSourcePath());

        Work fromDb4 = workManager.getWork(userOrcid, work4.getPutCode());
        assertNotNull(fromDb4);
        assertEquals(userOrcid, fromDb4.getSource().retrieveSourcePath());
    }

    @Test(expected=ActivityTitleValidationException.class)
    public void addWorkWithoutTitle() {
    	getWorkWithoutTitle(userOrcid, true);
    }
    
    @Test(expected=ActivityIdentifierValidationException.class)
    public void addWorkWithoutExternalIdentifiers() {
    	getWorkWithoutExternalIdentifier(userOrcid, true);
    }
    
    @Test(expected=InvalidPutCodeException.class)
    public void addWorkWithPutCode() {
    	getWorkWithPutCode(userOrcid, true);
    }
    
    @Test
    public void sourceDoesntChange_Funding_Test() {
        when(sourceManager.retrieveSourceEntity()).thenReturn(new SourceEntity(new ProfileEntity(userOrcid)));
        ProfileFundingEntity funding1 = getProfileFundingEntity(userOrcid);
        assertNotNull(funding1);
        assertFalse(PojoUtil.isEmpty(funding1.getTitle()));
        assertEquals(userOrcid, funding1.getSource().getSourceId());

        when(sourceManager.retrieveSourceEntity()).thenReturn(new SourceEntity(new ClientDetailsEntity(CLIENT_1_ID)));
        ProfileFundingEntity funding2 = getProfileFundingEntity(userOrcid);
        assertNotNull(funding2);
        assertFalse(PojoUtil.isEmpty(funding2.getTitle()));
        assertEquals(CLIENT_1_ID, funding2.getSource().getSourceId());

        when(sourceManager.retrieveSourceEntity()).thenReturn(new SourceEntity(new ClientDetailsEntity(CLIENT_2_ID)));
        ProfileFundingEntity funding3 = getProfileFundingEntity(userOrcid);
        assertNotNull(funding3);
        assertFalse(PojoUtil.isEmpty(funding3.getTitle()));
        assertEquals(CLIENT_2_ID, funding3.getSource().getSourceId());

        when(sourceManager.retrieveSourceEntity()).thenReturn(new SourceEntity(new ProfileEntity(userOrcid)));
        ProfileFundingEntity funding4 = getProfileFundingEntity(userOrcid);
        assertNotNull(funding4);
        assertFalse(PojoUtil.isEmpty(funding4.getTitle()));
        assertEquals(userOrcid, funding4.getSource().getSourceId());

        ProfileFundingEntity fromDb1 = profileFundingManager.getProfileFundingEntity(funding1.getId());
        assertNotNull(fromDb1);
        assertEquals(userOrcid, fromDb1.getSource().getSourceId());

        ProfileFundingEntity fromDb2 = profileFundingManager.getProfileFundingEntity(funding2.getId());
        assertNotNull(fromDb2);
        assertEquals(CLIENT_1_ID, fromDb2.getSource().getSourceId());

        ProfileFundingEntity fromDb3 = profileFundingManager.getProfileFundingEntity(funding3.getId());
        assertNotNull(fromDb3);
        assertEquals(CLIENT_2_ID, fromDb3.getSource().getSourceId());

        ProfileFundingEntity fromDb4 = profileFundingManager.getProfileFundingEntity(funding4.getId());
        assertNotNull(fromDb4);
        assertEquals(userOrcid, fromDb4.getSource().getSourceId());
    }
	
    @Test(expected=ActivityTitleValidationException.class)
    public void addFundingWithoutTitle() {
    	getFundingWithoutTitle(userOrcid);
    }
    
    @Test(expected=ActivityIdentifierValidationException.class)
    public void addFundingWithoutExternalIdentifiers() {
    	getFundingWithoutExtIdentifiers(userOrcid);
    }
    
    @Test(expected=InvalidPutCodeException.class)
    public void addFundingWithPutCode() {
    	getFundingWithPutCode(userOrcid);
    }
    
    private ProfileFundingEntity getFundingWithoutTitle(String userOrcid) {
        Funding funding = new Funding();        
        funding.setOrganization(getOrganization());
        funding.setType(org.orcid.jaxb.model.record_rc1.FundingType.AWARD);
        FundingExternalIdentifier extId = new FundingExternalIdentifier();
        extId.setValue("111");
        extId.setType(FundingExternalIdentifierType.GRANT_NUMBER);
        extId.setUrl(new Url("http://test.com"));
        FundingExternalIdentifiers extIdentifiers = new FundingExternalIdentifiers();
        extIdentifiers.getExternalIdentifier().add(extId);
        funding.setExternalIdentifiers(extIdentifiers);
        funding = profileFundingManager.createFunding(userOrcid, funding);
        return profileFundingManager.getProfileFundingEntity(funding.getPutCode());
    }
    
    private ProfileFundingEntity getFundingWithoutExtIdentifiers(String userOrcid) {
        Funding funding = new Funding();        
        funding.setOrganization(getOrganization());
        FundingTitle title = new FundingTitle();
        title.setTitle(new Title("Title " + System.currentTimeMillis()));
        funding.setTitle(title);
        funding.setType(org.orcid.jaxb.model.record_rc1.FundingType.AWARD);
        funding = profileFundingManager.createFunding(userOrcid, funding);
        return profileFundingManager.getProfileFundingEntity(funding.getPutCode());
    }
    
    private ProfileFundingEntity getFundingWithPutCode(String userOrcid) {
        Funding funding = new Funding();        
        funding.setOrganization(getOrganization());
        FundingTitle title = new FundingTitle();
        title.setTitle(new Title("Title " + System.currentTimeMillis()));
        funding.setTitle(title);
        funding.setType(org.orcid.jaxb.model.record_rc1.FundingType.AWARD);
        FundingExternalIdentifier extId = new FundingExternalIdentifier();
        extId.setValue("111");
        extId.setType(FundingExternalIdentifierType.GRANT_NUMBER);
        extId.setUrl(new Url("http://test.com"));
        FundingExternalIdentifiers extIdentifiers = new FundingExternalIdentifiers();
        extIdentifiers.getExternalIdentifier().add(extId);
        funding.setExternalIdentifiers(extIdentifiers);
        funding.setPutCode(Long.valueOf(111));
        funding = profileFundingManager.createFunding(userOrcid, funding);
        return profileFundingManager.getProfileFundingEntity(funding.getPutCode());
    }

    @Test
    public void sourceDoesntChange_PeerReview_Test() {
        when(sourceManager.retrieveSourceEntity()).thenReturn(new SourceEntity(new ProfileEntity(userOrcid)));
        PeerReview peerReview1 = getPeerReview(userOrcid);
        assertNotNull(peerReview1);
        assertNotNull(peerReview1.getSubjectName());
        assertNotNull(peerReview1.getSubjectName().getTitle());
        assertFalse(PojoUtil.isEmpty(peerReview1.getSubjectName().getTitle().getContent()));
        assertEquals(userOrcid, peerReview1.retrieveSourcePath());
        
        when(sourceManager.retrieveSourceEntity()).thenReturn(new SourceEntity(new ClientDetailsEntity(CLIENT_1_ID)));
        PeerReview peerReview2 = getPeerReview(userOrcid);
        assertNotNull(peerReview2);
        assertNotNull(peerReview2.getSubjectName());
        assertNotNull(peerReview2.getSubjectName().getTitle());
        assertFalse(PojoUtil.isEmpty(peerReview2.getSubjectName().getTitle().getContent()));
        assertEquals(CLIENT_1_ID, peerReview2.retrieveSourcePath());
        
        when(sourceManager.retrieveSourceEntity()).thenReturn(new SourceEntity(new ClientDetailsEntity(CLIENT_2_ID)));
        PeerReview peerReview3 = getPeerReview(userOrcid);
        assertNotNull(peerReview3);
        assertNotNull(peerReview3.getSubjectName());
        assertNotNull(peerReview3.getSubjectName().getTitle());
        assertFalse(PojoUtil.isEmpty(peerReview3.getSubjectName().getTitle().getContent()));
        assertEquals(CLIENT_2_ID, peerReview3.retrieveSourcePath());
        
        when(sourceManager.retrieveSourceEntity()).thenReturn(new SourceEntity(new ProfileEntity(userOrcid)));
        PeerReview peerReview4 = getPeerReview(userOrcid);
        assertNotNull(peerReview4);
        assertNotNull(peerReview4.getSubjectName());
        assertNotNull(peerReview4.getSubjectName().getTitle());
        assertFalse(PojoUtil.isEmpty(peerReview4.getSubjectName().getTitle().getContent()));
        assertEquals(userOrcid, peerReview4.retrieveSourcePath());
        
        PeerReview fromDb1 = peerReviewManager.getPeerReview(userOrcid, peerReview1.getPutCode());
        assertNotNull(fromDb1);
        assertEquals(userOrcid, fromDb1.retrieveSourcePath());
        
        PeerReview fromDb2 = peerReviewManager.getPeerReview(userOrcid, peerReview2.getPutCode());
        assertNotNull(fromDb2);
        assertEquals(CLIENT_1_ID, fromDb2.retrieveSourcePath());
        
        PeerReview fromDb3 = peerReviewManager.getPeerReview(userOrcid, peerReview3.getPutCode());
        assertNotNull(fromDb3);
        assertEquals(CLIENT_2_ID, fromDb3.retrieveSourcePath());
        
        PeerReview fromDb4 = peerReviewManager.getPeerReview(userOrcid, peerReview4.getPutCode());
        assertNotNull(fromDb4);
        assertEquals(userOrcid, fromDb4.retrieveSourcePath());
    }

    @Test
    public void sourceDoesntChange_Affiliation_Test() {
        when(sourceManager.retrieveSourceEntity()).thenReturn(new SourceEntity(new ProfileEntity(userOrcid)));
        Education education1 = getEducation(userOrcid);
        assertNotNull(education1);        
        assertEquals(userOrcid, education1.retrieveSourcePath());
        
        when(sourceManager.retrieveSourceEntity()).thenReturn(new SourceEntity(new ClientDetailsEntity(CLIENT_1_ID)));
        Education education2 = getEducation(userOrcid);
        assertNotNull(education2);        
        assertEquals(CLIENT_1_ID, education2.retrieveSourcePath());
        
        when(sourceManager.retrieveSourceEntity()).thenReturn(new SourceEntity(new ClientDetailsEntity(CLIENT_2_ID)));
        Education education3 = getEducation(userOrcid);
        assertNotNull(education3);        
        assertEquals(CLIENT_2_ID, education3.retrieveSourcePath());
        
        when(sourceManager.retrieveSourceEntity()).thenReturn(new SourceEntity(new ProfileEntity(userOrcid)));
        Education education4 = getEducation(userOrcid);
        assertNotNull(education4);        
        assertEquals(userOrcid, education4.retrieveSourcePath());
        
        Education fromDb1 = affiliationsManager.getEducationAffiliation(userOrcid, education1.getPutCode());
        assertNotNull(fromDb1);
        assertEquals(userOrcid, fromDb1.retrieveSourcePath());
        
        Education fromDb2 = affiliationsManager.getEducationAffiliation(userOrcid, education2.getPutCode());
        assertNotNull(fromDb2);
        assertEquals(CLIENT_1_ID, fromDb2.retrieveSourcePath());
        
        Education fromDb3 = affiliationsManager.getEducationAffiliation(userOrcid, education3.getPutCode());
        assertNotNull(fromDb3);
        assertEquals(CLIENT_2_ID, fromDb3.retrieveSourcePath());
        
        Education fromDb4 = affiliationsManager.getEducationAffiliation(userOrcid, education4.getPutCode());
        assertNotNull(fromDb4);
        assertEquals(userOrcid, fromDb4.retrieveSourcePath());
    }
    
    @Test(expected=InvalidPutCodeException.class)
    public void addAffiliationWithPutCode() {
    	getAffiliationWithPutCode(userOrcid);
    }
    
    private Education getAffiliationWithPutCode(String userOrcid) {
        Education education = new Education();
        education.setOrganization(getOrganization());
        education.setPutCode(Long.valueOf(111));
        education = affiliationsManager.createEducationAffiliation(userOrcid, education);
        return affiliationsManager.getEducationAffiliation(userOrcid, education.getPutCode());
    }

    private OrcidProfile getMinimalOrcidProfile() {
        OrcidProfile profile = new OrcidProfile();
        OrcidBio bio = new OrcidBio();
        ContactDetails contactDetails = new ContactDetails();
        contactDetails.addOrReplacePrimaryEmail(new org.orcid.jaxb.model.message.Email(System.currentTimeMillis() + "@user.com"));
        Preferences preferences = new Preferences();
        preferences.setSendChangeNotifications(new SendChangeNotifications(true));
        preferences.setSendOrcidNews(new SendOrcidNews(true));
        preferences.setSendMemberUpdateRequests(true);
        preferences.setSendEmailFrequencyDays("1");
        preferences.setActivitiesVisibilityDefault(new ActivitiesVisibilityDefault(Visibility.fromValue("public")));
        PersonalDetails personalDetails = new PersonalDetails();
        personalDetails.setFamilyName(new FamilyName("First"));
        personalDetails.setGivenNames(new GivenNames("Last"));
        bio.setContactDetails(contactDetails);
        bio.setPersonalDetails(personalDetails);
        OrcidInternal internal = new OrcidInternal();
        internal.setPreferences(preferences);
        profile.setOrcidBio(bio);
        profile.setOrcidInternal(internal);
        OrcidHistory orcidHistory = new OrcidHistory();
        orcidHistory.setClaimed(new Claimed(true));
        orcidHistory.setCreationMethod(CreationMethod.fromValue("integration-test"));
        profile.setOrcidHistory(orcidHistory);
        orcidHistory.setSubmissionDate(new SubmissionDate(DateUtils.convertToXMLGregorianCalendar(new Date())));
        profile.setPassword("password1");
        return orcidProfileManager.createOrcidProfile(profile, false, false);
    }

    private Work getWork(String userOrcid) {
        Work work = new Work();
        WorkTitle title = new WorkTitle();
        title.setTitle(new Title("Work " + System.currentTimeMillis()));
        work.setWorkTitle(title);
        work.setWorkType(org.orcid.jaxb.model.record_rc1.WorkType.BOOK);
        WorkExternalIdentifier extId = new WorkExternalIdentifier();
        extId.setWorkExternalIdentifierId(new WorkExternalIdentifierId("111"));
        extId.setWorkExternalIdentifierType(WorkExternalIdentifierType.DOI);
        WorkExternalIdentifiers extIdentifiers = new WorkExternalIdentifiers();
        extIdentifiers.getExternalIdentifier().add(extId);
        work.setWorkExternalIdentifiers(extIdentifiers);
        work = workManager.createWork(userOrcid, work, false);
        return workManager.getWork(userOrcid, work.getPutCode());
    }
    
    private Work getWorkWithoutTitle(String userOrcid2, boolean validate) {
    	Work work = new Work();
        work.setWorkType(org.orcid.jaxb.model.record_rc1.WorkType.BOOK);
        WorkExternalIdentifier extId = new WorkExternalIdentifier();
        extId.setWorkExternalIdentifierId(new WorkExternalIdentifierId("111"));
        extId.setWorkExternalIdentifierType(WorkExternalIdentifierType.DOI);
        WorkExternalIdentifiers extIdentifiers = new WorkExternalIdentifiers();
        extIdentifiers.getExternalIdentifier().add(extId);
        work.setWorkExternalIdentifiers(extIdentifiers);
        work = workManager.createWork(userOrcid, work, validate);
        return workManager.getWork(userOrcid, work.getPutCode());
	}
    
    private Work getWorkWithoutExternalIdentifier(String userOrcid, boolean validate) {
        Work work = new Work();
        WorkTitle title = new WorkTitle();
        title.setTitle(new Title("Work " + System.currentTimeMillis()));
        work.setWorkTitle(title);
        work.setWorkType(org.orcid.jaxb.model.record_rc1.WorkType.BOOK);
        work = workManager.createWork(userOrcid, work, validate);
        return workManager.getWork(userOrcid, work.getPutCode());
    }
    private Work getWorkWithPutCode(String userOrcid, boolean validate) {
        Work work = new Work();
        WorkTitle title = new WorkTitle();
        title.setTitle(new Title("Work " + System.currentTimeMillis()));
        work.setWorkTitle(title);
        WorkExternalIdentifier extId = new WorkExternalIdentifier();
        extId.setWorkExternalIdentifierId(new WorkExternalIdentifierId("111"));
        extId.setWorkExternalIdentifierType(WorkExternalIdentifierType.DOI);
        WorkExternalIdentifiers extIdentifiers = new WorkExternalIdentifiers();
        extIdentifiers.getExternalIdentifier().add(extId);
        work.setWorkExternalIdentifiers(extIdentifiers);
        work.setWorkType(org.orcid.jaxb.model.record_rc1.WorkType.BOOK);
        work.setPutCode(Long.valueOf(111));
        work = workManager.createWork(userOrcid, work, validate);
        return workManager.getWork(userOrcid, work.getPutCode());
    }

    private ProfileFundingEntity getProfileFundingEntity(String userOrcid) {
        Funding funding = new Funding();        
        funding.setOrganization(getOrganization());
        FundingTitle title = new FundingTitle();
        title.setTitle(new Title("Title " + System.currentTimeMillis()));
        funding.setTitle(title);
        funding.setType(org.orcid.jaxb.model.record_rc1.FundingType.AWARD);
        FundingExternalIdentifier extId = new FundingExternalIdentifier();
        extId.setValue("111");
        extId.setType(FundingExternalIdentifierType.GRANT_NUMBER);
        extId.setUrl(new Url("http://test.com"));
        extId.setRelationship(Relationship.PART_OF);
        FundingExternalIdentifiers extIdentifiers = new FundingExternalIdentifiers();
        extIdentifiers.getExternalIdentifier().add(extId);
        funding.setExternalIdentifiers(extIdentifiers);
        funding = profileFundingManager.createFunding(userOrcid, funding);
        return profileFundingManager.getProfileFundingEntity(funding.getPutCode());
    }

    private Education getEducation(String userOrcid) {
        Education education = new Education();
        education.setOrganization(getOrganization());
        education = affiliationsManager.createEducationAffiliation(userOrcid, education);
        return affiliationsManager.getEducationAffiliation(userOrcid, education.getPutCode());
    }
    
    private PeerReview getPeerReview(String userOrcid) {
        PeerReview peerReview = new PeerReview();
        peerReview.setOrganization(getOrganization());
        peerReview.setType(PeerReviewType.EVALUATION);
        WorkTitle workTitle = new WorkTitle();
        workTitle.setTitle(new Title("Title " + System.currentTimeMillis()));
        WorkExternalIdentifiers workExtIds = new WorkExternalIdentifiers();
        WorkExternalIdentifier workExtId = new WorkExternalIdentifier();
        workExtId.setWorkExternalIdentifierId(new WorkExternalIdentifierId("ID"));
        workExtId.setWorkExternalIdentifierType(WorkExternalIdentifierType.AGR);
        workExtIds.getExternalIdentifier().add(workExtId);        
        peerReview.setSubjectName(workTitle);
        peerReview.setSubjectExternalIdentifier(workExtId);
        peerReview.setSubjectType(WorkType.ARTISTIC_PERFORMANCE);
        peerReview.setExternalIdentifiers(workExtIds);
        peerReview.setRole(Role.CHAIR);
        peerReview = peerReviewManager.createPeerReview(userOrcid, peerReview, false);
        return peerReviewManager.getPeerReview(userOrcid, peerReview.getPutCode());
    }

    private Organization getOrganization() {
        if (organization == null) {
            OrganizationAddress address = new OrganizationAddress();
            address.setCity("City");
            address.setRegion("Region");
            address.setCountry(org.orcid.jaxb.model.common.Iso3166Country.US);                        
            organization = new Organization();                                    
            organization.setName("Org name");
            organization.setAddress(address);
        }
        return organization;
    }        
}
