package com.litianyu.ohshortlink.project.service;

import com.litianyu.ohshortlink.project.dto.req.RecycleBinSaveReqDTO;

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
}
