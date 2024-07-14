package com.litianyu.ohshortlink.project.controller;


import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.litianyu.ohshortlink.project.common.convention.result.Result;
import com.litianyu.ohshortlink.project.common.convention.result.Results;
import com.litianyu.ohshortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.litianyu.ohshortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.litianyu.ohshortlink.project.dto.req.ShortLinkPageReqDTO;
import com.litianyu.ohshortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.litianyu.ohshortlink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import com.litianyu.ohshortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.litianyu.ohshortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.litianyu.ohshortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.litianyu.ohshortlink.project.handler.CustomBlockHandler;
import com.litianyu.ohshortlink.project.service.ShortLinkService;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * 短链接跳转原始链接
     */
    @GetMapping("/{short-uri}")
    public void restoreUrl(@PathVariable("short-uri") String shortUri, ServletRequest request, ServletResponse response) {
        shortLinkService.restoreUrl(shortUri, request, response);
    }

    /**
     * 创建短链接
     */
    @PostMapping("/api/short-link/v1/create")
    @SentinelResource( // 对接口限流
            value = "create_short-link", // TODO：这部分后续抽到配置类中
            blockHandler = "createShortLinkBlockHandlerMethod",
            blockHandlerClass = CustomBlockHandler.class
    )
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return Results.success(shortLinkService.createShortLink(requestParam));
    }

    /**
     * 批量创建短链接
     */
    @PostMapping("/api/short-link/v1/create/batch")
    public Result<ShortLinkBatchCreateRespDTO> batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParam) {
        return Results.success(shortLinkService.batchCreateShortLink(requestParam));
    }

    /**
     * 修改短链接
     */
    @PostMapping("/api/short-link/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        shortLinkService.updateShortLink(requestParam);
        return Results.success();
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/short-link/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return Results.success(shortLinkService.pageShortLink(requestParam));
    }


    /**
     * 查询短链接分组内数量
     */
    @GetMapping("/api/short-link/v1/count")
    public Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam("requestParam") List<String> requestParam) {
        return Results.success(shortLinkService.listGroupShortLinkCount(requestParam));
    }
}
