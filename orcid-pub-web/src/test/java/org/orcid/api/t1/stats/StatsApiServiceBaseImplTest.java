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
package org.orcid.api.t1.stats;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.orcid.api.t1.stats.delegator.StatsApiServiceDelegator;
import org.orcid.core.manager.StatisticsManager;
import org.orcid.jaxb.model.statistics.StatisticsSummary;
import org.orcid.jaxb.model.statistics.StatisticsTimeline;
import org.orcid.persistence.dao.StatisticsDao;
import org.orcid.persistence.jpa.entities.StatisticKeyEntity;
import org.orcid.persistence.jpa.entities.StatisticValuesEntity;
import org.orcid.test.OrcidJUnit4ClassRunner;
import org.orcid.test.TargetProxyHelper;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(OrcidJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:orcid-t1-web-context.xml", "classpath:orcid-core-context.xml", "classpath:orcid-t1-security-context.xml" })
@SuppressWarnings("deprecation")
public class StatsApiServiceBaseImplTest {

    @Resource(name = "statsApiServiceDelegator")
    StatsApiServiceDelegator serviceDelegator;

    // the class that contains the thing we're mocking
    @Resource(name = "statisticsManager")
    StatisticsManager statsManager;

    StatisticsDao statisticsDao = mock(StatisticsDao.class);

    @Before
    public void init() {
        // create our mock data
        List<StatisticValuesEntity> statsTimelineValues = new ArrayList<StatisticValuesEntity>();
        List<StatisticValuesEntity> statsSummaryValues = new ArrayList<StatisticValuesEntity>();

        StatisticValuesEntity a = new StatisticValuesEntity();
        a.setId(1l);
        a.setStatisticName("name1");
        a.setStatisticValue(100l);
        StatisticKeyEntity akey = new StatisticKeyEntity();
        akey.setGenerationDate(new Date(2000, 1, 1));
        akey.setId(200l);
        a.setKey(akey);

        StatisticValuesEntity b = new StatisticValuesEntity();
        b.setId(1l);
        b.setStatisticName("name1");
        b.setStatisticValue(101l);
        StatisticKeyEntity bkey = new StatisticKeyEntity();
        bkey.setGenerationDate(new Date(1999, 1, 1));
        bkey.setId(201l);
        b.setKey(bkey);

        StatisticValuesEntity c = new StatisticValuesEntity();
        c.setId(1l);
        c.setStatisticName("name2");
        c.setStatisticValue(102l);
        c.setKey(akey);

        statsTimelineValues.add(a);
        statsTimelineValues.add(b);
        statsSummaryValues.add(a);
        statsSummaryValues.add(c);

        // mock the methods used
        when(statisticsDao.getLatestKey()).thenReturn(akey);
        when(statisticsDao.getStatistic("name1")).thenReturn(statsTimelineValues);
        when(statisticsDao.getStatistic(200l)).thenReturn(statsSummaryValues);

        //statsManager.setStatisticsDao(statisticsDao);
        TargetProxyHelper.injectIntoProxy(statsManager, "statisticsDao", statisticsDao);
        
        // setup security context
        ArrayList<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
        roles.add(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));
        Authentication auth = new AnonymousAuthenticationToken("anonymous", "anonymous", roles);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void testViewStatsSummary() {
        Assert.assertEquals(serviceDelegator.getStatsSummary().getStatus(), 200);
        StatisticsSummary s = (StatisticsSummary) serviceDelegator.getStatsSummary().getEntity();
        Assert.assertEquals(s.getDate(), new Date(2000, 1, 1));
        Assert.assertEquals(s.getStatistics().size(), 2);
        Assert.assertEquals((long) s.getStatistics().get("name1"), 100l);
        Assert.assertEquals((long) s.getStatistics().get("name2"), 102l);

    }

    @Test
    public void testViewStatsTimeline() {
        Assert.assertEquals(serviceDelegator.getStatsSummary().getStatus(), 200);
        StatisticsTimeline s = (StatisticsTimeline) serviceDelegator.getStatsTimeline("name1").getEntity();
        Assert.assertEquals(s.getStatisticName(), "name1");
        Assert.assertEquals(s.getTimeline().size(), 2);
        Assert.assertEquals((long) s.getTimeline().get(new Date(1999, 1, 1)), 101l);
        Assert.assertEquals((long) s.getTimeline().get(new Date(2000, 1, 1)), 100l);
    }
    
}