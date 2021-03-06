<#--

    =============================================================================

    ORCID (R) Open Source
    http://orcid.org

    Copyright (c) 2012-2014 ORCID, Inc.
    Licensed under an MIT-Style License (MIT)
    http://orcid.org/open-source-license

    This copyright and license information (including a link to the full license)
    shall be included in its entirety in all copies or substantial portion of
    the software.

    =============================================================================

-->
<#-- @ftlvariable name="profile" type="org.orcid.jaxb.model.message.OrcidProfile" -->
<@protected nav="notifications">
<#escape x as x?html> 
<div class="row">
    <div class="col-md-3 col-sm-12 col-xs-12 padding-fix">
        <#include "admin_menu.ftl"/>
    </div>
    <div class="col-md-9 col-sm-12 col-xs-12" ng-controller="NotificationsCtrl" >
        <!-- <h2>${springMacroRequestContext.getMessage("notifications.title")}</h2> -->
        <div class="notification-top-bar">
            <label class="checkbox pull-right">
                <input type="checkbox" ng-model="notificationsSrvc.showArchived" ng-change="reloadNotifications()"></input>
                ${springMacroRequestContext.getMessage("notifications.show_archived")}
            </label>
        </div>
        <div ng-show="notificationsSrvc.loading == true" class="text-center" id="notificationsSpinner">
            <i class="glyphicon glyphicon-refresh spin x4 green" id="spinner"></i><!-- Hidden with a CSS hack on IE 7 only -->
            <!--[if lt IE 8]>    
                <img src="${staticCdn}/img/spin-big.gif" width="85" height ="85"/>
            <![endif]-->
        </div>
        <div ng-cloak ng-show="notificationsSrvc.loading == false && notifications.length == 0  &&!areMore()">${springMacroRequestContext.getMessage("notifications.none")}</div>
        <div ng-cloak ng-show="notificationsSrvc.loading == false && notifications.length &gt; 0">            
            <table class="table table-responsive table-condensed notifications">
           		<thead>					
	                <tr>
	                    <th>${springMacroRequestContext.getMessage("notifications.from")}</th>
	                    <th>${springMacroRequestContext.getMessage("notifications.subject")}</th>
	                    <th>${springMacroRequestContext.getMessage("notifications.date")}</th>
	                    <th></th>
	                </tr>
                </thead>
                <tbody>
	                <tr ng-repeat-start="notification in notifications" ng-class="{unread: !notification.readDate, archived: notification.archivedDate}">
	                    <td ng-click="toggleDisplayBody(notification.putCode)">
	                        <i class="glyphicon-chevron-down glyphicon x0" ng-class="{'glyphicon-chevron-right':!displayBody[notification.putCode]}"></i>
	                        <span ng-show="notification.source" ng-cloak>{{notification.source.sourceName.content}}</span><span ng-hide="notification.source" ng-cloak>ORCID</span>
	                    </td>
	                    <td ng-click="toggleDisplayBody(notification.putCode)"><span ng-cloak>{{notification.subject}}</span></td>
	                    <td ng-click="toggleDisplayBody(notification.putCode)"><span ng-cloak>{{notification.createdDate|humanDate}}</span></td>
	                    <td>
	                        <span ng-hide="notification.archivedDate"><a href="" ng-click="archive(notification.putCode)" class="glyphicon glyphicon-download-alt dark-grey" title="${springMacroRequestContext.getMessage("notifications.archive")}"></a></span>
	                    </td>
	                </tr>
	                <tr ng-repeat-end ng-show="displayBody[notification.putCode]" onclick="return false;">
	                    <td colspan="4">
	                        <iframe id="{{notification.putCode}}" ng-src="{{ '<@orcid.rootPath '/inbox'/>/' + notification.notificationType + '/' + notification.putCode + '/notification.html'}}" class="notification-iframe" frameborder="0" width="100%" scrolling="no"></iframe>
	                    </td>
	                </tr>
                </tbody>

            </table>
        </div>
        <div ng-cloak ng-hide="notificationsSrvc.loading == false && notifications.length &gt; 0">
            <br/><br/>
        </div>   
        <div ng-cloak>
            <button ng-show="areMore() && notificationsSrvc.loadingMore == false" ng-click="showMore()" class="btn" type="submit" id="show-more-button">${springMacroRequestContext.getMessage("notifications.show_more")}</button>
        </div>
        <div ng-cloak ng-show="notificationsSrvc.loadingMore == true" id="moreNotificationsSpinner">
            <i class="glyphicon glyphicon-refresh spin x4 green" id="spinner"></i><!-- Hidden with a CSS hack on IE 7 only -->
            <!--[if lt IE 8]>    
                <img src="${staticCdn}/img/spin-big.gif" width="85" height ="85"/>
            <![endif]-->
        </div>
    </div>
</div>

</#escape>
</@protected>
