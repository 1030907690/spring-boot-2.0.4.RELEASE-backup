
package com.rw.article.pay.entity;


import com.rw.article.common.annotation.GeneratedValue;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * ClassName:Role <br/>
 * Function: TODO (角色数据库数据). <br/>
 * Reason: TODO (). <br/>
 * Date: 2015-7-3 下午4:34:45 <br/>
 * 
 * @author lyh
 * @version
 * @see
 */
@Document(collection="game_player")
public class GamePlayer implements Serializable {



	@Id
	@GeneratedValue
	private Long id = 1000000L;

	/**记录创建时间**/
	private Date createTime;

	/**更新时间**/
	private Date updateTime;


	/**配置文件为=1**/
	private  Integer dbConfig;



	/** 用户账号 **/
	@Indexed
	private String openId;

	/**昵称**/
	@Indexed
	private String name;

	/**图片url**/
	private String url;

	/**性别**/
	private Integer sex;

	/**游戏币()**/
	private Long money;

	/**头像索引**/
	private Integer headImgIndex;

	/**登录时间**/
	private Date loginInTime;

	/**退出游戏时间**/
	private Date loginOutTime;

	/**玩家状态,正常为1,2=封号,机器人的状态未在游戏时默认为2,在玩游戏时为1**/
	private Integer playerStatus;

	/**密码**/
	private String password;

	/**是不是机器人 机器人=1**/
	private Integer robot;

	/**注册IP**/
	private String registerIp;

	/**登录Ip**/
	private String loginIp;

	/**登录次数**/
	private Long loginCount;

	/**累计在线时间(分)**/
	private Long onlineTime;


	/**有效押注金币**/
	private Long validMoney;

	/**累计充值总额人民币**/
	private Long totalBalance;

	/**玩家累计提现**/
	private Long totalFetchMoney;


	/**游戏支出金币数**/
	private Long paidMoney;

	/**游戏进账金币数**/
	private Long incomeMoney;

	/**游戏盈亏金币数**/
	private Long breakEvenMoney;

	/**玩的总局数**/
	private Integer playSum;

	/**赢的局数**/
	private Integer winRound;

	/**平的局数**/
	private Integer avgRound;

	/**输的局数**/
	private Integer lostRound;



	/**赠送的用户金币**/
	private Long giveUserMoney;

	/**绑定手机号的金币**/
	private Long bindPhoneMoney;


	/**银行卡号**/
	private String bankCardNum;

	/**银行名称**/
	private String bankName;

	/**支付宝账号**/
	private String aliPayCardNum;

	/**支付宝名称**/
	private String  aliPayName;

	/**真实名字**/
	private String realName;



	/**上级返佣奖券**/
	private Long upReceive;

	/**下级返佣奖券**/
	private Long receive;

	/*下注返佣奖券*/
	private Long meReceive;


	/**保险箱金额**/
	private Long safeMoney;
	/**保险箱密码**/
	private String safePasswd;
	/**********手机设备与第三方信息***********/
	/**代理id,**/
	private String agentId;

	/**代理合作商Id**/
	private String agentChannelId;

	/**推广id**/
	private String parentId;

	/**渠道号**/
	private String channelId;

	/**esm**/
	private String esm;

	/**设备ID**/
	@Indexed
	private String deviceId;

	/**设备类型,android=1,IOS=2,web=3,**/
	private String deviceType;


	/**设备名称**/
	private String deviceName;

	/**电话号码**/
	@Indexed
	private String mobile;

	////////////////游戏数据////////////

	/**记牌器器功能,1=标记,0=无标记**/
	private Integer noteCardFlag;

	/**机器人的房间id**/
	private Long roomId;

	/**Long=游戏Id,Long=游戏次数**/
	Map<Long,Long> betCountsMap;

	/**临时充值的钱**/
	private Long tmpRechargeMoney;

	/**登录间隔时间**/
	private long loginInteralTime;

	/**每日在线时间(分钟)**/
	private Integer everyOnlineTime;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Integer getDbConfig() {
		return dbConfig;
	}

	public void setDbConfig(Integer dbConfig) {
		this.dbConfig = dbConfig;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getSex() {
		return sex;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
	}

	public Long getMoney() {
		return money;
	}

	public void setMoney(Long money) {
		this.money = money;
	}

	public Integer getHeadImgIndex() {
		return headImgIndex;
	}

	public void setHeadImgIndex(Integer headImgIndex) {
		this.headImgIndex = headImgIndex;
	}

	public Date getLoginInTime() {
		return loginInTime;
	}

	public void setLoginInTime(Date loginInTime) {
		this.loginInTime = loginInTime;
	}

	public Date getLoginOutTime() {
		return loginOutTime;
	}

	public void setLoginOutTime(Date loginOutTime) {
		this.loginOutTime = loginOutTime;
	}

	public Integer getPlayerStatus() {
		return playerStatus;
	}

	public void setPlayerStatus(Integer playerStatus) {
		this.playerStatus = playerStatus;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getRobot() {
		return robot;
	}

	public void setRobot(Integer robot) {
		this.robot = robot;
	}

	public String getRegisterIp() {
		return registerIp;
	}

	public void setRegisterIp(String registerIp) {
		this.registerIp = registerIp;
	}

	public String getLoginIp() {
		return loginIp;
	}

	public void setLoginIp(String loginIp) {
		this.loginIp = loginIp;
	}

	public Long getLoginCount() {
		return loginCount;
	}

	public void setLoginCount(Long loginCount) {
		this.loginCount = loginCount;
	}

	public Long getOnlineTime() {
		return onlineTime;
	}

	public void setOnlineTime(Long onlineTime) {
		this.onlineTime = onlineTime;
	}

	public Long getValidMoney() {
		return validMoney;
	}

	public void setValidMoney(Long validMoney) {
		this.validMoney = validMoney;
	}

	public Long getTotalBalance() {
		return totalBalance;
	}

	public void setTotalBalance(Long totalBalance) {
		this.totalBalance = totalBalance;
	}

	public Long getTotalFetchMoney() {
		return totalFetchMoney;
	}

	public void setTotalFetchMoney(Long totalFetchMoney) {
		this.totalFetchMoney = totalFetchMoney;
	}

	public Long getPaidMoney() {
		return paidMoney;
	}

	public void setPaidMoney(Long paidMoney) {
		this.paidMoney = paidMoney;
	}

	public Long getIncomeMoney() {
		return incomeMoney;
	}

	public void setIncomeMoney(Long incomeMoney) {
		this.incomeMoney = incomeMoney;
	}

	public Long getBreakEvenMoney() {
		return breakEvenMoney;
	}

	public void setBreakEvenMoney(Long breakEvenMoney) {
		this.breakEvenMoney = breakEvenMoney;
	}

	public Integer getPlaySum() {
		return playSum;
	}

	public void setPlaySum(Integer playSum) {
		this.playSum = playSum;
	}

	public Integer getWinRound() {
		return winRound;
	}

	public void setWinRound(Integer winRound) {
		this.winRound = winRound;
	}

	public Integer getAvgRound() {
		return avgRound;
	}

	public void setAvgRound(Integer avgRound) {
		this.avgRound = avgRound;
	}

	public Integer getLostRound() {
		return lostRound;
	}

	public void setLostRound(Integer lostRound) {
		this.lostRound = lostRound;
	}

	public Long getGiveUserMoney() {
		return giveUserMoney;
	}

	public void setGiveUserMoney(Long giveUserMoney) {
		this.giveUserMoney = giveUserMoney;
	}

	public Long getBindPhoneMoney() {
		return bindPhoneMoney;
	}

	public void setBindPhoneMoney(Long bindPhoneMoney) {
		this.bindPhoneMoney = bindPhoneMoney;
	}

	public String getBankCardNum() {
		return bankCardNum;
	}

	public void setBankCardNum(String bankCardNum) {
		this.bankCardNum = bankCardNum;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getAliPayCardNum() {
		return aliPayCardNum;
	}

	public void setAliPayCardNum(String aliPayCardNum) {
		this.aliPayCardNum = aliPayCardNum;
	}

	public String getAliPayName() {
		return aliPayName;
	}

	public void setAliPayName(String aliPayName) {
		this.aliPayName = aliPayName;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public Long getUpReceive() {
		return upReceive;
	}

	public void setUpReceive(Long upReceive) {
		this.upReceive = upReceive;
	}

	public Long getReceive() {
		return receive;
	}

	public void setReceive(Long receive) {
		this.receive = receive;
	}

	public Long getMeReceive() {
		return meReceive;
	}

	public void setMeReceive(Long meReceive) {
		this.meReceive = meReceive;
	}

	public Long getSafeMoney() {
		return safeMoney;
	}

	public void setSafeMoney(Long safeMoney) {
		this.safeMoney = safeMoney;
	}

	public String getSafePasswd() {
		return safePasswd;
	}

	public void setSafePasswd(String safePasswd) {
		this.safePasswd = safePasswd;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public String getAgentChannelId() {
		return agentChannelId;
	}

	public void setAgentChannelId(String agentChannelId) {
		this.agentChannelId = agentChannelId;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getEsm() {
		return esm;
	}

	public void setEsm(String esm) {
		this.esm = esm;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public Integer getNoteCardFlag() {
		return noteCardFlag;
	}

	public void setNoteCardFlag(Integer noteCardFlag) {
		this.noteCardFlag = noteCardFlag;
	}

	public Long getRoomId() {
		return roomId;
	}

	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}

	public Map<Long, Long> getBetCountsMap() {
		return betCountsMap;
	}

	public void setBetCountsMap(Map<Long, Long> betCountsMap) {
		this.betCountsMap = betCountsMap;
	}

	public Long getTmpRechargeMoney() {
		return tmpRechargeMoney;
	}

	public void setTmpRechargeMoney(Long tmpRechargeMoney) {
		this.tmpRechargeMoney = tmpRechargeMoney;
	}

	public long getLoginInteralTime() {
		return loginInteralTime;
	}

	public void setLoginInteralTime(long loginInteralTime) {
		this.loginInteralTime = loginInteralTime;
	}

	public Integer getEveryOnlineTime() {
		return everyOnlineTime;
	}

	public void setEveryOnlineTime(Integer everyOnlineTime) {
		this.everyOnlineTime = everyOnlineTime;
	}
}
