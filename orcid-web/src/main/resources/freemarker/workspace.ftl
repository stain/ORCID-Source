<#--

    =============================================================================

    ORCID (R) Open Source
    http://orcid.org

    Copyright (c) 2012-2013 ORCID, Inc.
    Licensed under an MIT-Style License (MIT)
    http://orcid.org/open-source-license

    This copyright and license information (including a link to the full license)
    shall be included in its entirety in all copies or substantial portion of
    the software.

    =============================================================================

-->
<#-- @ftlvariable name="profile" type="org.orcid.jaxb.model.message.OrcidProfile" -->
<@protected nav="record">
<#escape x as x?html>
<#if emailVerified?? && emailVerified>
    <div class="alert alert-success">
        <strong><@spring.message "orcid.frontend.web.email_verified"/></strong>
    </div>
</#if>

<#if invalidVerifyUrl?? && invalidVerifyUrl>
    <div class="alert alert-success">
        <strong><@spring.message "orcid.frontend.web.invalid_verify_link"/></strong>
    </div>
</#if>

<div class="row workspace-top public-profile">

	<#-- hidden divs that trigger angular -->
	<#if RequestParameters['recordClaimed']??>
	    <div ng-controller="ClaimThanks" style="display: hidden;"></div>	    
	<#elseif !Session.CHECK_EMAIL_VALIDATED?exists>
    	<div ng-controller="VerifyEmailCtrl" style="display: hidden;"></div>
	</#if>

    <div class="span3 lhs">
    	<div class="workspace-left workspace-profile">
            <h2 class="full-name">
                <#if (profile.orcidBio.personalDetails.creditName.content)??>
                    ${(profile.orcidBio.personalDetails.creditName.content)!}
                <#else>
                    ${(profile.orcidBio.personalDetails.givenNames.content)!} ${(profile.orcidBio.personalDetails.familyName.content)!}
                </#if>
            </h2>
            <p><small id="orcid-id" class="orcid-id">${baseUriHttp}/${(profile.orcid.value)!}</small></p>
	        <p class="hoover-white-fonts"><a href="${baseUriHttp}/${(profile.orcid.value)!}" class="label btn-primary"><@orcid.msg 'workspace.ViewPublicORCIDRecord'/></a></p>
	        <#if ((profile.orcidBio.personalDetails.otherNames.otherName)?size != 0)>
	        	<p><strong><@orcid.msg 'workspace.Alsoknownas'/></strong><br />
		       		<#list profile.orcidBio.personalDetails.otherNames.otherName as otherName>
		       			${otherName.content}<#if otherName_has_next><br /></#if>
		       		</#list></p>
	       	</#if>
            <#if (profile.orcidBio.contactDetails.address.country.content)??>
                <p><strong><@orcid.msg 'public_profile.labelCountry'/></strong>
                ${(profile.orcidBio.contactDetails.address.country.content)!}
                </p>
            </#if>
	       	<#if (profile.orcidBio.keywords)?? && (profile.orcidBio.keywords.keyword?size != 0)>
	        	<p><strong><@orcid.msg 'public_profile.labelKeywords'/></strong> 
		       		<#list profile.orcidBio.keywords.keyword as keyword>
		       			${keyword.content}<#if keyword_has_next>,</#if>
		       		</#list></p>
	       	</#if>
	       	<#if (profile.orcidBio.researcherUrls)?? && (profile.orcidBio.researcherUrls.researcherUrl?size != 0)>
	        	<p><strong><@orcid.msg 'public_profile.labelWebsites'/></strong> <br/>
		       		<#list profile.orcidBio.researcherUrls.researcherUrl as url>
		       		   <a href="<@orcid.absUrl url.url/>" target="_blank"><#if (url.urlName.content)! != "">${url.urlName.content}<#else>${url.url.value}</#if></a><#if url_has_next><br/></#if>
		       		</#list></p>
	       	</#if>
       		<div ng-controller="ExternalIdentifierCtrl" ng-hide="!externalIdentifiersPojo.externalIdentifiers.length" ng-cloak>	       			
       			<p><strong><@orcid.msg 'public_profile.labelOtherIDs'/></strong> </p>
		        <table id="externalIdentifierTable">
		        	<tr style="vertical-align:bottom;" ng-repeat='externalIdentifier in externalIdentifiersPojo.externalIdentifiers'>
		        		<td class="padRgt">
		        			<p ng-hide="externalIdentifier.externalIdUrl">{{externalIdentifier.externalIdCommonName.content}} {{externalIdentifier.externalIdReference.content}}</p>
		        			<p ng-show="externalIdentifier.externalIdUrl"><a href="{{externalIdentifier.externalIdUrl.value}}" target="_blank">{{externalIdentifier.externalIdCommonName.content}} {{externalIdentifier.externalIdReference.content}}</a></p>
		     			</td>
			   			<td class="padRgt">
			   				<p><a href ng-click="deleteExternalIdentifier($index)" class="icon-trash orcid-icon-trash grey"></a></p>
			   			</td>		        		
		        	</tr>
		        </table>
			</div>							    
	        <@security.authorize ifAnyGranted="ROLE_ADMIN, ROLE_GROUP, ROLE_BASIC, ROLE_BASIC_INSTITUTION, ROLE_PREMIUM, ROLE_PREMIUM_INSTITUTION">
	        	 <p><a href="<@spring.url "/manage-clients" />">${springMacroRequestContext.getMessage("workspace.ManageClientCredentials")}</a></p>	        	 
	        </@security.authorize>
			<@security.authorize ifAnyGranted="ROLE_ADMIN">
				<p><a href="<@spring.url "/deprecate-profile" />"><@orcid.msg 'admin.profile_deprecation.workspace_link' /></a></p>
			</@security.authorize>
        </div>
    </div>
    <div class="span9">
        <div class="workspace-right">
        	<div class="workspace-inner workspace-header">
                <div class="alert alert-info"><strong><@orcid.msg 'workspace.addinformationaboutyou'/></strong></div>
        		<div class="workspace-overview" id="works-overview" ng-controller="WorkOverviewCtrl">
        			<a href="#workspace-publications" class="overview-count"><span ng-bind="works.length"></span></a>
        			<a href="#workspace-publications" class="overview-title"><@orcid.msg 'workspace.Works'/></a>
                    <br />
                    <a href="#workspace-publications" class="btn-update no-icon"><@orcid.msg 'workspace.view'/></a>
        		</div>
                <div class="workspace-overview">
                    <a href="#workspace-affiliations" class="overview-count">${(profile.orcidBio.affiliations?size)!0}</a>
                    <a href="#workspace-affiliations" class="overview-title"><@orcid.msg 'workspace_bio.Affiliations'/></a>
                    <div><a target="_blank" href="http://support.orcid.org/forums/179657-coming-soon" class="btn-update no-icon"><@orcid.msg 'workspace.ComingSoon'/></a></div>
                </div>
        		<div class="workspace-overview">
        			<a href="#workspace-grants" class="overview-count">${(profile.orcidActivities.orcidGrants.orcidGrant?size)!0}</a>
        			<a href="#workspace-grants" class="overview-title"><@orcid.msg 'workspace.Grants'/></a>
        			<br />
        			<a target="_blank" href="http://support.orcid.org/forums/179657-coming-soon" class="btn-update no-icon"><@orcid.msg 'workspace.ComingSoon'/></a>
        		</div>
        		<div class="workspace-overview">
        			<a href="#workspace-patents" class="overview-count">${(profile.orcidActivities.orcidPatents.orcidPatent?size)!0}</a>
        			<a href="#workspace-patents" class="overview-title"><@orcid.msg 'workspace.Patents'/></a>
        			<br />
        			<a target="_blank" href="http://support.orcid.org/forums/179657-coming-soon" class="btn-update no-icon"><@orcid.msg 'workspace.ComingSoon'/></a>
        		</div>
        	</div>
        	<div class="workspace-accordion" id="workspace-accordion">
        	
        	   <div id="workspace-personal" class="workspace-accordion-item workspace-accordion-active" ng-controller="PersonalInfoCtrl">
        			<div class="workspace-accordion-header" style="position: relative;">
        			   <a href="" ng-click="toggleDisplayInfo()" style="color: #338caf;">
        			       <i class="icon-caret-down" ng-class="{'icon-caret-right':displayInfo==false}"></i></a>
        			   </a> 
        			   <a href="" ng-click="toggleDisplayInfo()"><@orcid.msg 'workspace.personal_information'/></a> 
        			   <a href="<@spring.url '/account/manage-bio-settings'/>" id="upate-personal-modal-link" class="label btn-primary"><@orcid.msg 'workspace.Update'/></a>
        			</div>
            		<div class="workspace-accordion-content" ng-show="displayInfo">
            			<#include "workspace_personal.ftl"/>
        			</div>
            	</div>
            	
            	<#--
        		<div id="workspace-affiliations" class="workspace-accordion-item${(!(profile.orcidBio.affiliations)?? || (profile.orcidBio.affiliations?size = 0))?string(" workspace-accordion-active", "")}">
                    <div class="workspace-accordion-header">
                    	<a href="#"><@orcid.msg 'workspace_bio.Affiliations'/></a>
                    </div>
                </div>
                -->
                
                <div id="workspace-publications" style="position: relative;" class="workspace-accordion-item workspace-accordion-active" ng-controller="WorkCtrl">
                	<div class="workspace-accordion-header">
        				<a href="" ng-click="toggleDisplayWorks()" style="color: #338caf;">
        			       <i class="icon-caret-down icon" ng-class="{'icon-caret-right':displayWorks==false}"></i></a>
        			    </a> 
        				<a href="" ng-click="toggleDisplayWorks()"><@orcid.msg 'workspace.Works'/></a>
						<a href="#third-parties" class="label btn-primary" ng-click="showWorkImportWizard()"><@orcid.msg 'workspace.import_works'/></a>
						<a href="" class="label btn-primary" ng-click="addWorkModal()"><@orcid.msg 'manual_work_form_contents.add_work_manually'/></a>
					</div>
      	            <div ng-show="displayWorks" class="workspace-accordion-content">
	            		<#include "workspace_works_body_list.ftl"/>
	            	</div>
            	</div>
            	
            	<#--
        		<div id="workspace-grants" class="workspace-accordion-item">
        			<div class="workspace-accordion-header"><a href="#"><@orcid.msg 'workspace.Grants'/></a></div>
            	</div>
            	
        		<div id="workspace-patents" class="workspace-accordion-item">
        			<div class="workspace-accordion-header"><a href="#"><@orcid.msg 'workspace.Patents'/></a></div>
            	</div>
            	-->
            	
            </div>
        </div>
    </div>    
</div>
</#escape>

<script type="text/ng-template" id="verify-email-modal">
	<div style="padding: 20px">
			<h4><@orcid.msg 'workspace.your_primary_email'/></h4>
			<@orcid.msg 'workspace.ensure_future_access'/><br />
			<br />
			<span class="btn btn-primary" id="modal-close" ng-click="verifyEmail()"><@orcid.msg 'workspace.send_verification'/></span>
			<span class="btn" id="modal-close" ng-click="closeColorBox()"><@orcid.msg 'freemarker.btncancel'/></span>
		</div>
</script>

<script type="text/ng-template" id="verify-email-modal-sent">
	<div style="padding: 20px; width: 400px;">
		<h4><@orcid.msg 'workspace.sent'/></h4>
		<@orcid.msg 'workspace.check_your_email'/><br />
		<br />
		<span class="btn" ng-click="closeColorBox()"><@orcid.msg 'freemarker.btnclose'/></span>
	</div>
</script>

<script type="text/ng-template" id="claimed-record-thanks">
	<div style="padding: 20px;">
		<strong><@spring.message "orcid.frontend.web.record_claimed"/></strong><br />
		<br />
		<button class="btn" ng-click="close()"><@spring.message "freemarker.btnclose"/></button>
	</div>
</script>
	
<script type="text/ng-template" id="claimed-record-thanks-source-grand-read">
	<div style="padding: 20px;">
		<strong><@spring.message "orcid.frontend.web.record_claimed"/></strong><br />
		<br />
		<strong ng-bind="sourceGrantReadWizard.displayName"></strong> <@spring.message "orcid.frontend.web.record_claimed.would_like"/><br />
		<br />
		<button class="btn btn-primary" ng-click="yes()"><@spring.message "orcid.frontend.web.record_claimed.yes_go_to" /></button>
		<button class="btn" ng-click="close()"><@spring.message "orcid.frontend.web.record_claimed.no_thanks" /></button>
	</div>
</script>

<script type="text/ng-template" id="delete-external-id-modal">
	<div style="padding: 20px;">
		<h3><@orcid.msg 'manage.deleteExternalIdentifier.pleaseConfirm'/> {{removeExternalModalText}} </h3>
		<button class="btn btn-danger" ng-click="removeExternalIdentifier()"><@orcid.msg 'manage.deleteExternalIdentifier.delete'/></button> 
		<a href="" ng-click="closeModal()"><@orcid.msg 'manage.deleteExternalIdentifier.cancel'/></a>
	<div>
</script>

<script type="text/ng-template" id="import-wizard-modal">
    <#if ((thirdPartiesForImport)??)>
    	<div id="third-parties">	
			<div class="span9">
				<a class="btn pull-right close-button" ng-click="closeModal()">X</a>
	           	<h1 class="lightbox-title" style="text-transform: uppercase;"><@orcid.msg 'workspace.import_works'/></h1>
	           		
	           	</div>
	           	<br />          		
    	       	<div class="justify"><@orcid.msg 'workspace.ImportResearchActivities.description'/></div>
            	<br />    	           	
    	       	<#list thirdPartiesForImport?sort_by("displayName") as thirdPartyDetails>
                     <#assign redirect = (thirdPartyDetails.redirectUris.redirectUri[0].value) >
                     <#assign predefScopes = (thirdPartyDetails.redirectUris.redirectUri[0].scopeAsSingleString) >
                     <strong><a href="<@spring.url '/oauth/authorize?client_id=${thirdPartyDetails.clientId}&response_type=code&scope=${predefScopes}&redirect_uri=${redirect}'/>" ng-click="closeModal()" target="_blank">${thirdPartyDetails.displayName}</a></strong><br />
                     <div class="justify">${(thirdPartyDetails.shortDescription)!}</div>
                     <#if (thirdPartyDetails_has_next)><hr /></#if>
                 </#list>
                 <br />
                 <div class="footer">
	               	<strong><@orcid.msg 'workspace.ImportResearchActivities.footer.title'/></strong>
	                <br />
	                <@orcid.msg 'workspace.ImportResearchActivities.footer.description1'/> <a href="<@orcid.msg 'workspace.ImportResearchActivities.footer.description.url'/>"><@orcid.msg 'workspace.ImportResearchActivities.footer.description.link'/></a> <@orcid.msg 'workspace.ImportResearchActivities.footer.description2'/>
                 </div>
		</div>
	</#if>
</script>


	
</@protected>
