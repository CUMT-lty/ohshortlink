package com.litianyu.ohshortlink.admin.controller;

// TODO：后期用 spring cloud 整合

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.litianyu.ohshortlink.admin.common.conversion.result.Result;
import com.litianyu.ohshortlink.admin.remote.ShortLinkRemoteService;
import com.litianyu.ohshortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.litianyu.ohshortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.litianyu.ohshortlink.admin.remote.dto.req.ShortLinkUpdateReqDTO;
import com.litianyu.ohshortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.litianyu.ohshortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接后管控制层
 */
@RestController
public class ShortLinkController {

    /**
     * TODO: 后续重构为 spring cloud feign 调用
     */
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};

    /**
     * 创建短链接
     */
    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return shortLinkRemoteService.createShortLink(requestParam);
    }

    /**
     * 修改短链接
     */
    @PostMapping("/api/short-link/admin/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        return shortLinkRemoteService.updateShortLink(requestParam);
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/short-link/admin/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return shortLinkRemoteService.pageShortLink(requestParam);
    }
}
