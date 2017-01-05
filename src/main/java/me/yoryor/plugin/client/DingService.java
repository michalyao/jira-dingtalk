package me.yoryor.plugin.client;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * DingDing 开放服务接口
 * 详细参数与接口说明可以参照 <a href="https://open-doc.dingtalk.com/doc2/detail?spm=0.0.0.0.4y2zn0&treeId=172&articleId=104981&docType=1">钉钉服务端开发文档</a>
 */
public interface DingService {
    /**
     * 获取钉钉接口 AccessToken，基于此Token调用其他接口。
     *
     * @param corpID 企业UID
     * @param corpSecret 企业应用的凭证密钥
     * @return a token
     */
    Future<String> getToken(String corpID, String corpSecret);

    /**
     * 发送企业会话消息
     *
     * @param token AccessToken
     * @param agentId 企业应用ID
     * @param userId 员工id列表
     * @param partyId 部门id列表
     * @param msg 消息体
     * @return 返回企业消息的id标识
     */
     Future<String> corpMsgTo(String token, String agentId, String[] userId, String[] partyId, JsonObject msg);

    /**
     * 获取公司员工id与名字的映射。名字为中文
     *
     * @param token AccessToken
     * @return a Map of users.
     */
    Future<Void> getUserMap(String token);

}
