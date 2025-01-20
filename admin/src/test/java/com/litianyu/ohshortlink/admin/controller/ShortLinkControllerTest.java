package com.litianyu.ohshortlink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.litianyu.ohshortlink.admin.common.conversion.result.Result;
import com.litianyu.ohshortlink.admin.common.conversion.result.Results;
import com.litianyu.ohshortlink.admin.remote.ShortLinkActualRemoteService;
import com.litianyu.ohshortlink.admin.remote.dto.req.ShortLinkBatchCreateReqDTO;
import com.litianyu.ohshortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.litianyu.ohshortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.litianyu.ohshortlink.admin.remote.dto.req.ShortLinkUpdateReqDTO;
import com.litianyu.ohshortlink.admin.remote.dto.resp.ShortLinkBaseInfoRespDTO;
import com.litianyu.ohshortlink.admin.remote.dto.resp.ShortLinkBatchCreateRespDTO;
import com.litianyu.ohshortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.litianyu.ohshortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.litianyu.ohshortlink.admin.toolkit.EasyExcelWebUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ShortLinkControllerTest {

    @InjectMocks
    private ShortLinkController shortLinkController;

    @Mock
    private ShortLinkActualRemoteService shortLinkActualRemoteService;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Test
    public void test_createShortLink() {
        ShortLinkCreateReqDTO req = new ShortLinkCreateReqDTO();
        Result<ShortLinkCreateRespDTO> res = new Result<>();
        when(shortLinkActualRemoteService.createShortLink(req)).thenReturn(res);
        Result<ShortLinkCreateRespDTO> result = shortLinkController.createShortLink(req);
        Assert.assertEquals(res, result);
    }

    @Test
    public void test_batchCreateShortLink() {
        ShortLinkBatchCreateReqDTO req = new ShortLinkBatchCreateReqDTO();
        Result<ShortLinkBatchCreateRespDTO> result = Results.success(new ShortLinkBatchCreateRespDTO());
        MockedStatic<EasyExcelWebUtil> easyExcelWebUtilMockedStatic = mockStatic(EasyExcelWebUtil.class);
        // fail
        result.setCode("fail");
        when(shortLinkActualRemoteService.batchCreateShortLink(req)).thenReturn(result);
        shortLinkController.batchCreateShortLink(req, httpServletResponse);
        easyExcelWebUtilMockedStatic.verify(
                ()->EasyExcelWebUtil.write(
                        httpServletResponse,
                        "批量创建短链接-SaaS短链接系统",
                        ShortLinkBaseInfoRespDTO.class,
                        null),
                times(0)
        );
        // success
        result.setCode(Result.SUCCESS_CODE);
        when(shortLinkActualRemoteService.batchCreateShortLink(req)).thenReturn(result);
        shortLinkController.batchCreateShortLink(req, httpServletResponse);
        easyExcelWebUtilMockedStatic.verify(
                ()->EasyExcelWebUtil.write(
                        httpServletResponse,
                        "批量创建短链接-SaaS短链接系统",
                        ShortLinkBaseInfoRespDTO.class,
                        null),
                times(1)
        ); // 注意，这个次数是整个单元测试方法内累计的，0+1=1次。如果前面是2次，这里就应该是2+1=3次
    }

    @Test
    public void test_updateShortLink() {
        ShortLinkUpdateReqDTO req = new ShortLinkUpdateReqDTO();
        Result<Void> result = shortLinkController.updateShortLink(req);
        Assert.assertEquals(Result.SUCCESS_CODE, result.getCode());
    }

    @Test
    public void test_pageShortLink() {
        ShortLinkPageReqDTO req = new ShortLinkPageReqDTO();
        req.setGid("gid");
        req.setOrderTag("orderTag");
        req.setCurrent(1);
        req.setSize(100);
        when(shortLinkActualRemoteService.pageShortLink(req.getGid(), req.getOrderTag(), req.getCurrent(), req.getSize()))
                .thenReturn(Results.success(new Page<>(1, 100)));
        Result<Page<ShortLinkPageRespDTO>> result = shortLinkController.pageShortLink(req);
        Assert.assertEquals(true, result.isSuccess());
    }
}