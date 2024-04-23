package com.litianyu.ohshortlink.admin.controller;

import com.litianyu.ohshortlink.admin.common.conversion.result.Result;
import com.litianyu.ohshortlink.admin.common.conversion.result.Results;
import com.litianyu.ohshortlink.admin.remote.ShortLinkRemoteService;
import com.litianyu.ohshortlink.project.dto.req.RecycleBinSaveReqDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回收站管理控制层
 */
@RestController(value = "recycleBinControllerByAdmin")
@RequiredArgsConstructor
public class RecycleBinController {

    private final ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};

    /**
     * 保存回收站
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) {
        shortLinkRemoteService.saveRecycleBin(requestParam);
        return Results.success();
    }
}
