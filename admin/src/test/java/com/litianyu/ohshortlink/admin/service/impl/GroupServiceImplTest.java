package com.litianyu.ohshortlink.admin.service.impl;

import com.litianyu.ohshortlink.admin.dao.mapper.GroupUniqueMapper;
import com.litianyu.ohshortlink.admin.remote.ShortLinkActualRemoteService;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import static com.litianyu.ohshortlink.admin.common.constant.RedisCacheConstant.LOCK_GROUP_CREATE_KEY;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GroupServiceImplTest extends TestCase {

    @InjectMocks
    private GroupServiceImpl groupService;

    @Mock
    private  RBloomFilter<String> gidRegisterCachePenetrationBloomFilter;
    @Mock
    private  GroupUniqueMapper groupUniqueMapper;
    @Mock
    private  ShortLinkActualRemoteService shortLinkActualRemoteService;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RLock lock;

    @Test
    public void test_saveGroup() {
        String username = "username";
        String groupName = "groupName";
        when(redissonClient.getLock(String.format(LOCK_GROUP_CREATE_KEY, username))).thenReturn(lock);
        groupService.saveGroup(username, groupName);
    }
}