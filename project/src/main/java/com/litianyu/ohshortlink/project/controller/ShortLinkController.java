package com.litianyu.ohshortlink.project.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.litianyu.ohshortlink.project.common.convention.result.Result;
import com.litianyu.ohshortlink.project.common.convention.result.Results;
import com.litianyu.ohshortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.litianyu.ohshortlink.project.dto.req.ShortLinkPageReqDTO;
import com.litianyu.ohshortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.litianyu.ohshortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.litianyu.ohshortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.litianyu.ohshortlink.project.service.ShortLinkService;
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
     * 创建短链接
     */
    @PostMapping("/api/short-link/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return Results.success(shortLinkService.createShortLink(requestParam));
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
