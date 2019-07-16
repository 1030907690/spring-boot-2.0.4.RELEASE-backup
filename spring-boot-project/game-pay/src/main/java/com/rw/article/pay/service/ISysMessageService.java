package com.rw.article.pay.service;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 系统消息
 * @date 2018/8/211:39
 */
public interface ISysMessageService  {
    public int insertSysMessage(String title, String content, int type, int status, int creatUser, int formUser, String formName, Long toUserId);
}
