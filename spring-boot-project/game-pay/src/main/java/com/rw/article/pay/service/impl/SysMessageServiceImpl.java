package com.rw.article.pay.service.impl;

import com.rw.article.common.constant.Constants;
import com.rw.article.pay.entity.SysMessage;
import com.rw.article.pay.service.ISysMessageRepository;
import com.rw.article.pay.service.ISysMessageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: ${todo}
 * @date 2018/8/211:39
 */
@Service
public class SysMessageServiceImpl implements ISysMessageService {


    @Resource
    private ISysMessageRepository sysMessageRepository;


    @Override
    public int insertSysMessage(String title, String content, int type, int status, int creatUser, int formUser, String formName, Long toUserId) {
        SysMessage sysMessage = new SysMessage();
        sysMessage.setTitle(title);
        sysMessage.setContent(content);
        sysMessage.setType(type);
        sysMessage.setStatus(status);
        sysMessage.setCreateTime(new Date());
        sysMessage.setCreateUserId(creatUser);
        sysMessage.setFromUserId(Long.parseLong(formUser+""));
        sysMessage.setFromName(formName);

        if(toUserId > 0){
            sysMessage.setToUserId(toUserId);
            sysMessage.setReadTime(new Date());
            sysMessage.setStatus(1);
            sysMessage.setToUserStatus(1);
        }
        sysMessage.setDbConfig(Constants.NOT_DB_CONFIG);
        sysMessage = sysMessageRepository.insert(sysMessage);
        if(null != sysMessage && sysMessage.getId() > 0){
            return 1;
        }
        return 0;
    }
}
