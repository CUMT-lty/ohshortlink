package com.litianyu.ohshortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.litianyu.ohshortlink.project.dto.req.RecycleBinRecoverReqDTO;
import com.litianyu.ohshortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.litianyu.ohshortlink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.litianyu.ohshortlink.project.dto.resp.ShortLinkPageRespDTO;

/**
 * 回收站管理接口层
 */
public interface RecycleBinService {

    /**
     * 保存回收站
     *
     * @param requestParam 请求参数
     */
    void saveRecycleBin(RecycleBinSaveReqDTO requestParam);

    /**
     * 分页查询短链接
     *
     * @param requestParam 分页查询短链接请求参数
     * @return 短链接分页返回结果
     */
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam);

    /**
     * 从回收站恢复短链接
     *
     * @param requestParam 恢复短链接请求参数
     */
    void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam);
}
