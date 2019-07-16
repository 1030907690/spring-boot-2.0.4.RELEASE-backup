package com.rw.article.pay.entity;

import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/** 
 * ClassName:BlockIp <br/> 
 * Function: TODO (系统,全局的配置). <br/>
 * Reason:   TODO (). <br/> 
 * Date:     2017年6月26日 下午5:38:05 <br/> 
 * @author   lyh 
 * @version   
 * @see       
 */
@Document(collection="sys_config")
public class SysConfig extends BaseEntity implements Serializable{


	/**配置项代码**/
	private String itemCode;
	
	/**配置项的值**/
	private String itemVal;

	/**状态：0无效、1正常、2删除**/
	private Integer status;

	/**记录创建时间**/
	private Date createTime;

	/**更新时间**/
	private Date updateTime;




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



	public String getItemCode() {
		return itemCode;
	}

	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}

	public String getItemVal() {
		return itemVal;
	}

	public void setItemVal(String itemVal) {
		this.itemVal = itemVal;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
}
  