package com.litianyu.ohshortlink.admin.remote;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.litianyu.ohshortlink.admin.common.conversion.result.Result;
import com.litianyu.ohshortlink.admin.common.conversion.result.Results;
import com.litianyu.ohshortlink.admin.dto.req.RecycleBinRecoverReqDTO;
import com.litianyu.ohshortlink.admin.dto.req.RecycleBinRemoveReqDTO;
import com.litianyu.ohshortlink.admin.dto.req.RecycleBinSaveReqDTO;
import com.litianyu.ohshortlink.admin.remote.dto.req.*;
import com.litianyu.ohshortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.litianyu.ohshortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.litianyu.ohshortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.litianyu.ohshortlink.admin.remote.dto.resp.ShortLinkStatsRespDTO;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO：后续这个文件要删除的（整个 rpc 调用要使用 spring cloud 来重构）

/**
 * 短链接中台远程调用服务
 */
public interface ShortLinkRemoteService {

    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("gid", requestParam.getGid());
        requestMap.put("current", requestParam.getCurrent());
        requestMap.put("size", requestParam.getSize());
        // 这里构造 Map 是因为，序列化之后得到的参数字符串和直接用 dto 序列化的参数字符串是一样的
        String resultPageJsonStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/page", requestMap);// hutool 提供的 HTTP 方法（这里后续会改成微服务，暂时先用 http 调用） TODO: project 模块的端口改为 8001
        return JSON.parseObject(resultPageJsonStr, new TypeReference<>() {}); // TODO: 因为 result 中有泛型，这里这个 TypeReference 相当于告诉泛型这里传的是什么东西
    }

    default Result<ShortLinkCreateRespDTO> createShortLink(ShortLinkCreateReqDTO requestParam){
        String resultBodyJsonStr = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/create", JSON.toJSONString(requestParam));
        return JSON.parseObject(resultBodyJsonStr, new TypeReference<>() {});
    }

   default Result<Void> updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/update", JSON.toJSONString(requestParam));
        return Results.success();
   }

    default Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(List<String> requestParam){
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestParam", requestParam);
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/count", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {});
    }

    default Result<String> getTitleByUrl(String url){
        String resultStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/title?url=" + url);
        return JSON.parseObject(resultStr, new TypeReference<Result<String>>() {});
    }

    default void saveRecycleBin(RecycleBinSaveReqDTO requestParam) {
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/save", JSON.toJSONString(requestParam));
    }

    default Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("gidList", requestParam.getGidList());
        requestMap.put("current", requestParam.getCurrent());
        requestMap.put("size", requestParam.getSize());
        // 这里构造 Map 是因为，序列化之后得到的参数字符串和直接用 dto 序列化的参数字符串是一样的
        String resultPageJsonStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/page", requestMap);// hutool 提供的 HTTP 方法（这里后续会改成微服务，暂时先用 http 调用） TODO: project 模块的端口改为 8001
        return JSON.parseObject(resultPageJsonStr, new TypeReference<>() {}); // TODO: 因为 result 中有泛型，这里这个 TypeReference 相当于告诉泛型这里传的是什么东西
    }

    default void recoverRecycleBin(@RequestBody RecycleBinRecoverReqDTO requestParam) {
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/recover", JSON.toJSONString(requestParam));
    }

    default void removeRecycleBin(@RequestBody RecycleBinRemoveReqDTO requestParam) {
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/remove", JSON.toJSONString(requestParam));
    }

    default Result<ShortLinkStatsRespDTO> oneShortLinkStats(ShortLinkStatsReqDTO shortLinkStatsReqDTO) {
        String resultBodyStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats", BeanUtil.beanToMap(shortLinkStatsReqDTO));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }
}
