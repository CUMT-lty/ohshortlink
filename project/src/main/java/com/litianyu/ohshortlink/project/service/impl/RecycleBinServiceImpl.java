package com.litianyu.ohshortlink.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.litianyu.ohshortlink.project.dao.entity.ShortLinkDO;
import com.litianyu.ohshortlink.project.dao.mapper.ShortLinkMapper;
import com.litianyu.ohshortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.litianyu.ohshortlink.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.litianyu.ohshortlink.project.common.constant.RedisKeyConstant.GOTO_SHORT_LINK_KEY;


/**
 * 回收站管理接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements RecycleBinService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void saveRecycleBin(RecycleBinSaveReqDTO requestParam) {
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0);
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .enableStatus(1) // 启用状态设置为未启用
                .build();
        baseMapper.update(shortLinkDO, updateWrapper);
        stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl())); // 将 redis 中对应的 key 删除
    }
}
