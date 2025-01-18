package com.litianyu.ohshortlink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.litianyu.ohshortlink.admin.common.conversion.result.Result;
import com.litianyu.ohshortlink.admin.dto.req.RecycleBinRecoverReqDTO;
import com.litianyu.ohshortlink.admin.dto.req.RecycleBinRemoveReqDTO;
import com.litianyu.ohshortlink.admin.dto.req.RecycleBinSaveReqDTO;
import com.litianyu.ohshortlink.admin.remote.ShortLinkActualRemoteService;
import com.litianyu.ohshortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.litianyu.ohshortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.litianyu.ohshortlink.admin.service.RecycleBinService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class RecycleBinControllerTest {

    @InjectMocks // 待测试类
    private RecycleBinController recycleBinController;

    @Mock // 待测试类依赖的 bean
    private RecycleBinService recycleBinService;

    @Mock
    private ShortLinkActualRemoteService shortLinkActualRemoteService;

    @Test
    public void test_saveRecycleBin() {
        RecycleBinSaveReqDTO mock = mock(RecycleBinSaveReqDTO.class);
        Result<Void> result = recycleBinController.saveRecycleBin(mock);
        Assert.assertEquals(true, result.isSuccess());
    }

    @Test
    public void test_pageShortLink() {
        ShortLinkRecycleBinPageReqDTO mock = mock(ShortLinkRecycleBinPageReqDTO.class);
        Result<Page<ShortLinkPageRespDTO>> pageResult = recycleBinController.pageShortLink(mock);
        Assert.assertEquals(null, pageResult);
    }

    @Test
    public void test_recoverRecycleBin() {
        RecycleBinRecoverReqDTO mock = mock(RecycleBinRecoverReqDTO.class);
        Result<Void> result = recycleBinController.recoverRecycleBin(mock);
        Assert.assertEquals(true, result.isSuccess());
    }

    @Test
    public void test_removeRecycleBin() {
        RecycleBinRemoveReqDTO mock = mock(RecycleBinRemoveReqDTO.class);
        Result<Void> result = recycleBinController.removeRecycleBin(mock);
        Assert.assertEquals(true, result.isSuccess());
    }
}