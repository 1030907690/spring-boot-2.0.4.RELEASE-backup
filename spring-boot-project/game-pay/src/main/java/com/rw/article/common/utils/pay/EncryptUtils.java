package com.rw.article.common.utils.pay;/*
package com.rw.common.utils.pay;

import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Md5Hash;

public class EncryptUtils {

    */
/**
     * 对密码进行md5加密,并返回密文和salt，包含在User对象中
     *
     * @param username       用户名
     * @param password       密码
     * @param hashIterations 迭代次数
     * @return array[0]=密文  array=[1]=salt
     *//*

    public static String[] md5Password(String username, String password, int hashIterations) {
        SecureRandomNumberGenerator secureRandomNumberGenerator = new SecureRandomNumberGenerator();
        String salt = secureRandomNumberGenerator.nextBytes().toHex();
        //组合username,两次迭代，对密码进行加密
        String passwordCrypt = new Md5Hash(password, username + salt, hashIterations).toString();
        return new String[]{passwordCrypt, salt};
    }

    public static void main(String[] args) {
        // String salt= "8d78869f470951332959580424d4bf4f";
        //组合username,两次迭代，对密码进行加密
        // String password_cryto = new Md5Hash("123456","administrator"+salt,2).toString();
        // System.out.println(password_cryto);

        String[] password = md5Password("admin", "123456", 1);
        System.out.println(password[0] + "->" + password[1]);
    }

}
*/
