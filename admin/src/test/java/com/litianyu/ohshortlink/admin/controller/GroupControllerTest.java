package com.litianyu.ohshortlink.admin.controller;

import com.litianyu.ohshortlink.admin.common.conversion.result.Result;
import com.litianyu.ohshortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import com.litianyu.ohshortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.litianyu.ohshortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.litianyu.ohshortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.litianyu.ohshortlink.admin.service.GroupService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// @RequiredArgsConstructor 这个注解不能在测试类中使用
@RunWith(MockitoJUnitRunner.class)
public class GroupControllerTest {

    @InjectMocks
    private GroupController groupController;

    @Mock
    private GroupService groupService;

    /**
     * 和 @RunWith(MockitoJUnitRunner.class) 二选一，都可以为 UT 提供框架使用的自动验证
     */
//    @Before
//    public void setup(){
//        MockitoAnnotations.initMocks(this);
//    }

    @Test // 这个注解不要导错了
    public void test_save() {
        ShortLinkGroupSaveReqDTO shortLinkGroupSaveReqDTO = new ShortLinkGroupSaveReqDTO();
        shortLinkGroupSaveReqDTO.setName("name");
        Result<Void> result = groupController.save(shortLinkGroupSaveReqDTO);
        assertEquals(true, result.isSuccess());
    }

    @Test
    public void test_listGroup() {
        when(groupService.listGroup()).thenReturn(new ArrayList<>());
        Result<List<ShortLinkGroupRespDTO>> result = groupController.listGroup();
        assertEquals(true, result.isSuccess());
    }

    @Test
    public void test_updateGroup() {
        ShortLinkGroupUpdateReqDTO mock = mock(ShortLinkGroupUpdateReqDTO.class);
        Result<Void> result = groupController.updateGroup(mock);
        assertEquals(true, result.isSuccess());
    }

    @Test
    public void test_deleteGroup() {
        Result<Void> result = groupController.deleteGroup("gid");
        assertEquals(true, result.isSuccess());
    }

    @Test
    public void test_sortGroup() {
        ShortLinkGroupSortReqDTO mock = mock(ShortLinkGroupSortReqDTO.class);
        Result<Void> result = groupController.sortGroup(List.of(mock));
        assertEquals(true, result.isSuccess());
    }
}