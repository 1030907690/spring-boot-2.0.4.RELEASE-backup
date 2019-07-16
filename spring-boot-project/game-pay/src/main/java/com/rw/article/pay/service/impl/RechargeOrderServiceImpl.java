package com.rw.article.pay.service.impl;

import com.rw.article.common.constant.AdminReturnMessage;
import com.rw.article.common.http.HttpClient;
import com.rw.article.common.http.SendAdminOfflineMessageLog;
import com.rw.article.common.type.AttentionLogType;
import com.rw.article.common.type.RechargeType;
import com.rw.article.common.utils.CommonFunction;
import com.rw.article.common.constant.Constants;
import com.rw.article.common.jackson.JsonObject;
import com.rw.article.common.utils.pay.berich.MD5Utils;
import com.rw.article.pay.dao.BaseDao;
import com.rw.article.pay.entity.*;
import com.rw.article.pay.service.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: 订单service impl
 * @date 2018/8/118:03
 */
@Service
public class RechargeOrderServiceImpl implements IRechargeOrderService {

    @Resource
    private IGamePlayerRepository gamePlayerRepository;


    @Resource
    private IPayPlatformRepository payPlatformRepository;


    @Resource
    private IRechargeOrderRepository rechargeOrderRepository;


    @Resource
    private ISysMessageService sysMessageService;

    @Resource
    private IAttentionLogRepository attentionLogRepository;


    @Resource
    private IMoneyRechargeLogRepository moneyRechargeLogRepository;
    private final static Logger log = LoggerFactory.getLogger(RechargeOrderServiceImpl.class);


    private String centerServerUrl;

    private String centerServerVerification;

    @Resource
    private ISysConfigRepository sysConfigRepository;

    @Resource
    private BaseDao baseDao;

    @Override
    public JsonObject createRechargeOrder(String userId, String price, String payType, String type, String device, String remoteAddress) {
        JsonObject info = null;
        // 判断类型
        if (!StringUtils.isNumeric(type)) {
            info = Constants.getErrorMessage(Constants.ErrorCode.OTHER_ERROR, Constants.RECHARGE_ORDER, "类型错误");
            return info;
        }
        if (null == payType || "".equals(payType) || !StringUtils.isNumeric(payType) || null == userId || "".equals(userId) || null == price || "".equals(price) || !StringUtils.isNumeric(userId)) {
            info = Constants.getErrorMessage(Constants.ErrorCode.OTHER_ERROR, Constants.RECHARGE_ORDER, "参数异常");
            return info;
        }

        GamePlayer userInfo = gamePlayerRepository.findById(Long.parseLong(userId)).orElse(null);
        if (null == userInfo || userInfo.getRobot().equals(1)) {
            info = Constants.getErrorMessage(Constants.ErrorCode.ACCOUNT_EXCEPTION, Constants.RECHARGE_ORDER, "用户帐号异常");
            return info;
        }


        //判断充值的钱 最大值 和最小值
        List<PayPlatform> payPlatformList = payPlatformRepository.findByPayTypeAndStatusAndPayModeType(Integer.parseInt(payType), 1, Integer.parseInt(type));
        PayPlatform payPlatform = null;
        if (null == payPlatformList || payPlatformList.size() <= 0 || payPlatformList.size() > 1) {
            info = Constants.getErrorMessage(Constants.ErrorCode.ACCOUNT_EXCEPTION, Constants.RECHARGE_ORDER, "支付方式异常!请选择其他支付方式");
            return info;
        }

        payPlatform = payPlatformList.get(0);
        long priceComparing = CommonFunction.multiply(Long.parseLong(price), Constants.TEN_THOUSAND);
        if (priceComparing < payPlatform.getPayMin() || priceComparing > payPlatform.getPayMax()) {
            info = Constants.getErrorMessage(Constants.ErrorCode.ACCOUNT_EXCEPTION, Constants.RECHARGE_ORDER, "充值金额只能大于 " + CommonFunction.getGoldIsMultipl(payPlatform.getPayMin()) + "并且小于" + CommonFunction.getGoldIsMultipl(payPlatform.getPayMax()));
            return info;
        }


        String orderId = System.currentTimeMillis() + "_shop_" + price;

        int count = 1000;
        while (null != getRechargeOrderByOrderNo(orderId) && count > 0) {
            orderId = System.currentTimeMillis() + "_shop_" + price;
            count--;
        }
        //最后再检测一次,如果不成功返回false
        if (null != getRechargeOrderByOrderNo(orderId)) {
            info = Constants.getErrorMessage(Constants.ErrorCode.ACCOUNT_EXCEPTION, Constants.RECHARGE_ORDER, "您的操作太频繁,请稍后再试!");
            return info;
        }

        RechargeOrder rechargeOrder = new RechargeOrder();
        rechargeOrder.setCreateTime(new Date());
        rechargeOrder.setRechargePrice(CommonFunction.exchangeGoldCoin("1", Long.parseLong(price)));
        rechargeOrder.setOrderId(orderId);
        rechargeOrder.setPayStatus(0);
        rechargeOrder.setStatus(0);
        rechargeOrder.setPayType(Integer.parseInt(payType));
        rechargeOrder.setUserId(Long.parseLong(userId));
        rechargeOrder.setMoney(Integer.parseInt(price) * 100);
        rechargeOrder.setDbConfig(Constants.NOT_DB_CONFIG);
        long line = rechargeOrderRepository.insert(rechargeOrder).getId();

        if (line > 0) {
            info = Constants.getSuccessMessage(Constants.RECHARGE_ORDER, rechargeOrder.getOrderId());
        } else {
            info = Constants.getErrorMessage(Constants.ErrorCode.ACCOUNT_EXCEPTION, Constants.RECHARGE_ORDER, "保存异常!");
        }
        return info;
    }


    @Override
    public RechargeOrder getRechargeOrderByOrderNo(String orderId) {
        List<RechargeOrder> list = rechargeOrderRepository.findByOrderId(orderId);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }


    @Override
    public int payAfterHandle(int line, RechargeOrder rechargeOrder, RechargeOrder beforeRechargeOrder) {
        try {


            if (beforeRechargeOrder != null && beforeRechargeOrder.getPayStatus().equals(0)) {
                String format = DateFormatUtils.format(rechargeOrder.getCreateTime(), "yyyy-MM-dd HH:mm:ss");
                //  发送邮件
                sysMessageService.insertSysMessage(AdminReturnMessage.MSG_46, String.format(AdminReturnMessage.MSG_86, format, CommonFunction.moneyRetainDecimal(CommonFunction.round(rechargeOrder.getRechargePrice(), Constants.TEN_THOUSAND))), 2, 1, Constants.SYS_BANKER_USER_ID
                        , Constants.SYS_BANKER_USER_ID, AdminReturnMessage.MSG_46, rechargeOrder.getUserId());


                // TODO 增加用户金币  2018年7月4日10:59:30 改为增加到背包   这里要到游戏服去调接口
                //userService.updateMoneyByUserId(rechargeOrder.getUserId(), rechargeOrder.getRechargePrice());


                //  累计充值总额
                GamePlayer account = gamePlayerRepository.findById(rechargeOrder.getUserId()).orElse(null);
               /* Account account = userService.getAccount(rechargeOrder.getUserId());
                Account recordAccount = new Account();
                recordAccount.setUserId(rechargeOrder.getUserId());
                recordAccount.setRechargeNum(account.getRechargeNum() + 1);
                Long totalBalance = CommonFunction.add(account.getTotalBalance(), rechargeOrder.getRechargePrice());
                recordAccount.setTotalBalance(totalBalance);
                userService.updateByPrimaryKeySelective(recordAccount);
*/
                //记录金币变化日志
                // 充值日志
                //Map<String, String> config = sysConfigService.getConfigs("convertmoney");
                MoneyRechargeLog moneyRechargeLog = new MoneyRechargeLog();
                moneyRechargeLog.setBalance(account.getMoney());
                moneyRechargeLog.setUserId(account.getId());
                moneyRechargeLog.setGetMoney(rechargeOrder.getRechargePrice());
                moneyRechargeLog.setUserType(1);
                moneyRechargeLog.setRate(1);
                moneyRechargeLog.setChannelId(1);
                //判断支付方式
                int payType = RechargeType.WECHAT_RECHARGE.getCode();
                //try {
                String payTypeStr = rechargeOrder.getPayType().toString().substring(rechargeOrder.getPayType().toString().length() - 1, rechargeOrder.getPayType().toString().length());
                switch (payTypeStr) {
                    case "1":
                        payType = RechargeType.ALIPAY_RECHARGE.getCode();
                        break;
                    case "2":
                        payType = RechargeType.WECHAT_RECHARGE.getCode();
                        break;
                    case "3":
                        payType = RechargeType.QQ_RECHARGE.getCode();
                        break;
                    case "4":
                        payType = RechargeType.UNIONPAY_RECHARGE.getCode();
                        break;
                    case "5":
                        //京东;
                        break;
                    default:
                        break;
                }
                //} catch (Exception e) {
                //    e.printStackTrace();
                //}

                moneyRechargeLog.setType(payType);
                moneyRechargeLog.setStatus(1);
                moneyRechargeLog.setCreateTime(new Date());
                moneyRechargeLog.setOpenId(1);
                moneyRechargeLog.setRemark("第三方支付充值");
                moneyRechargeLog.setRechbefore(account.getMoney());
                moneyRechargeLog.setRechlater(account.getMoney() + moneyRechargeLog.getGetMoney());
                moneyRechargeLogRepository.insert(moneyRechargeLog);


                //给用户增加金币
                SysConfig verificationSys = sysConfigRepository.findFirstByItemCodeAndStatusOrderByCreateTimeAsc("center_server_verification", 1);
                SysConfig urlSys = sysConfigRepository.findFirstByItemCodeAndStatusOrderByCreateTimeAsc("center_server_url", 1);
                if (null != verificationSys && verificationSys.getItemVal() != null) {
                    centerServerVerification = verificationSys.getItemVal();
                }
                if (null != urlSys && urlSys.getItemVal() != null) {
                    centerServerUrl = urlSys.getItemVal();
                }
                Map<String, String> httpParams = new HashMap<>();
                httpParams.put("userId", rechargeOrder.getUserId().toString());
                httpParams.put("money", rechargeOrder.getRechargePrice().toString());
                httpParams.put("sign", MD5Utils.MD5Encoding(rechargeOrder.getUserId().toString() + centerServerVerification));
                HttpClient.sendHttpRequestPost(centerServerUrl, httpParams, HttpClient.ENCODING);

                // 第三方充值修改为直接充值到背包，同时消息列表需要增加消息。在屏幕中间需要添加充值成功的消息。2018年7月4日10:58:25


            }


            if (null != rechargeOrder && null != beforeRechargeOrder && beforeRechargeOrder.getPayStatus().equals(0)) {
                // TODO 发送websocket消息

          /*  Map<String, Integer> socketPar = new HashMap<>();
            socketPar.put("userId", rechargeOrder.getUserId());
            socketPar.put("code", line > 0 ? rechargeOrder.getPayStatus() : 0);
            JsonObject data = new JsonObject();
            data.put("message", socketPar);
            JsonObject info = Constants.getSuccessMessage(SocketCmdInner.recharge_message, "", data);
            SyncMessage message = new SyncMessage(info, GameType.HALL.getCode(), -4, null);
            message.setSync(true);
            MessageUtils.convertAndSend(Constants.CAROUSEL_TOPIC, message.toString());*/
            }
            //发送关注
            sendAttentionMessage(rechargeOrder);
            line = 1;
        } catch (Exception e) {
            log.info("支付回调异常  [{}],[ {} ] ,[ {} ]", e, e.getMessage(), e.getStackTrace());
            line = 0;
        }
        return line;
    }


    /***
     *zhouzhongqing
     * 2018年9月12日18:25:36
     * 发送关注消息
     * */
    private void sendAttentionMessage(RechargeOrder rechargeOrder){
        if(null != rechargeOrder){
            //先查询是否有关注
            AttentionLog attentionLog = attentionLogRepository.findFirstByUserIdAndTypeOrderByCreateTimeAsc(rechargeOrder.getUserId(), AttentionLogType.RECHARGE_ORDER.getCode());
            if(null == attentionLog){
                //如果为空 插入
                attentionLog = new AttentionLog();
                attentionLog.setUserId(rechargeOrder.getUserId());
                attentionLog.setType(AttentionLogType.RECHARGE_ORDER.getCode());
                attentionLog.setCreateTime(new Date());
                attentionLog.setUpdateTime(attentionLog.getCreateTime());
                attentionLog.setDbConfig(Constants.NOT_DB_CONFIG);
                List<String> idList = new ArrayList<>();
                idList.add(rechargeOrder.getId().toString());
                attentionLog.setIdList(idList);
                attentionLogRepository.insert(attentionLog);
            }else{
                //不为空
                AttentionLog tempLong = new AttentionLog();
                tempLong.setId(attentionLog.getId());
                List<String> idList = (null == attentionLog.getIdList() ? new ArrayList<>() :  attentionLog.getIdList());
                idList.add(rechargeOrder.getId().toString());
                tempLong.setIdList(idList);
                baseDao.updateByEntity(tempLong);
            }

            //发送消息
            SysConfig urlSysConfig = sysConfigRepository.findFirstByItemCodeAndStatusOrderByCreateTimeAsc("admin_attention_url",1);
            if(null != urlSysConfig ){
                SendAdminOfflineMessageLog.getInstance().sendAdminOfflineMessageLog(urlSysConfig.getItemVal(),AttentionLogType.RECHARGE_ORDER.getCode(),String.format(Constants.ATTENTION_RECHARGE,rechargeOrder.getUserId()));
            }else{
                log.error("未设置 admin_attention_url [ {} ]",urlSysConfig);
            }
        }else{
            log.error("订单为空 [{}]",rechargeOrder);
        }
    }
}
