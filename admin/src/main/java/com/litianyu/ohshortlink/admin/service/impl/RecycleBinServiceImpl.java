package com.litianyu.ohshortlink.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.litianyu.ohshortlink.admin.common.biz.user.UserContext;
import com.litianyu.ohshortlink.admin.common.conversion.exception.ServiceException;
import com.litianyu.ohshortlink.admin.common.conversion.result.Result;
import com.litianyu.ohshortlink.admin.dao.entity.GroupDO;
import com.litianyu.ohshortlink.admin.dao.mapper.GroupMapper;
import com.litianyu.ohshortlink.admin.remote.ShortLinkRemoteService;
import com.litianyu.ohshortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.litianyu.ohshortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.litianyu.ohshortlink.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * URL 回收站接口实现层
 */
@Service(value = "recycleBinServiceImplByAdmin")
@RequiredArgsConstructor
public class RecycleBinServiceImpl implements RecycleBinService {
    private final ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};
    private final GroupMapper groupMapper;

    @Override
    public Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        // 查询当前用户下的所有分组
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        List<GroupDO> groupDOList = groupMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(groupDOList)) {
            throw new ServiceException("用户无分组信息");
        }
        requestParam.setGidList(groupDOList.stream().map(GroupDO::getGid).toList());
        return shortLinkRemoteService.pageRecycleBinShortLink(requestParam);
    }
}
