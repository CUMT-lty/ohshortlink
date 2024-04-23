package com.litianyu.ohshortlink.admin.controller;

import com.litianyu.ohshortlink.admin.common.conversion.result.Result;
import com.litianyu.ohshortlink.admin.remote.ShortLinkRemoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * URL 标题控制层
 */
@RestController(value = "urlTitleControllerByAdmin")
@RequiredArgsConstructor
public class UrlTitleController {

    private final ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};

    /**
     * 根据URL获取对应网站的标题
     */
    @GetMapping("/api/short-link/admin/v1/title")
    public Result<String> getTitleByUrl(@RequestParam("url") String url) {
        return shortLinkRemoteService.getTitleByUrl(url);
    }
}
