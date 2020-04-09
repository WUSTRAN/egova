package com.egova.rest;


/**
 * 响应结果对象工厂（各部门需要实现该接口来创建自己的结果包装类）
 */
public interface ResponseResultFactory {

    /**
     * 功能描述：获取成功响应结果
     *
     * @author chensoft
     * @params [t:响应结果, message:响应消息]
     * @return com.egova.rest.ResponseResult<T> 响应结果包装对象
     * @date 2020-04-09 23:16
     */
    <T> ResponseResult<T> success(T t, String message);

    /**
     * 功能描述：获取异常响应结果
     *
     * @author chensoft
     * @params [t:响应结果, message:响应消息]
     * @return com.egova.rest.ResponseResult<T> 响应结果包装对象
     * @date 2020-04-09 23:16
     */
    <T> ResponseResult<T> error(String message);
}
