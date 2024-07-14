package com.litianyu.ohshortlink.project.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.litianyu.ohshortlink.project.common.convention.result.Result;
import com.litianyu.ohshortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.litianyu.ohshortlink.project.dto.resp.ShortLinkCreateRespDTO;

/**
 * 自定义流控策略
 */
public class CustomBlockHandler {

    public static Result<ShortLinkCreateRespDTO> createShortLinkBlockHandlerMethod(ShortLinkCreateReqDTO requestParam, BlockException exception) {
        return new Result<ShortLinkCreateRespDTO>().setCode("B100000").setMessage("当前访问网站人数过多，请稍后再试..."); // TODO：这个(错误码 + message)应该抽出去
    }
}
