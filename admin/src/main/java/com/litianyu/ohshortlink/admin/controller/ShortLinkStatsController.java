package com.litianyu.ohshortlink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.litianyu.ohshortlink.admin.common.conversion.result.Result;
import com.litianyu.ohshortlink.admin.remote.ShortLinkActualRemoteService;
import com.litianyu.ohshortlink.admin.remote.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import com.litianyu.ohshortlink.admin.remote.dto.req.ShortLinkGroupStatsReqDTO;
import com.litianyu.ohshortlink.admin.remote.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.litianyu.ohshortlink.admin.remote.dto.req.ShortLinkStatsReqDTO;
import com.litianyu.ohshortlink.admin.remote.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.litianyu.ohshortlink.admin.remote.dto.resp.ShortLinkStatsRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接监控控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    @GetMapping("/api/short-link/admin/v1/stats")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(ShortLinkStatsReqDTO requestParam) {
        return shortLinkActualRemoteService.oneShortLinkStats(requestParam.getFullShortUrl(), requestParam.getGid(), requestParam.getStartDate(), requestParam.getEndDate());
    }

    @GetMapping("/api/short-link/admin/v1/stats/access-record")
    public Result<Page<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        return shortLinkActualRemoteService.shortLinkStatsAccessRecord(requestParam.getFullShortUrl(), requestParam.getGid(), requestParam.getStartDate(), requestParam.getEndDate());
    }

    @GetMapping("/api/short-link/admin/v1/stats/group")
    public Result<ShortLinkStatsRespDTO> groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam) {
        return shortLinkActualRemoteService.groupShortLinkStats(requestParam.getGid(), requestParam.getStartDate(), requestParam.getEndDate());
    }

    @GetMapping("/api/short-link/admin/v1/stats/access-record/group")
    public Result<Page<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam) {
        return shortLinkActualRemoteService.groupShortLinkStatsAccessRecord(requestParam.getGid(), requestParam.getStartDate(), requestParam.getEndDate());
    }
}
