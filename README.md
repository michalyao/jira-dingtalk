

# 概述
使用 JIRA webhook 与钉钉开发接口实现 JIRA 与钉钉的集成，实现以下自动化功能，工单变化时可以通过钉钉通知到相关人员。

# 原理
## Webhook 简介
Webhook是客户方（相对）通过HTTP协议定义的回调方法，在发生指定事件的时候向具体的应用发送通知。通过使用push取代pull的方式避免通过频繁的重复请求获取状态的更新。

## JIRA中的Webhook
#### 定义
在JIRA中Webhook的定义形式如下：
- name: hook的名称
- URL: hook需要发送的地址
- scope：hook的生命周期，issue相关
- events：hook发送的事件集合

``` json
Name: "My first webhook"
URL: "http://test.yoryor.me/jira/webhookserver"
Scope: all issues
Events: all issue events
```
#### 注册
注册hook主要有以下几种方式：
- 通过管理员控制台进行注册
- 通过JIRA REST API进行注册，需要全局管理员权限
- Atlassian Connect add-on(不常用). see [Connect documentation][1]

##### 通过控制台进行设置的操作步骤
1. JIRA控制台 -> System -> Webhooks（在Advanced一栏）
2. 点击 Create a webhook
3. 根据表单填写webhook的详细信息。[配置详情][2]
4. 点击 Create 完成webhook的配置。(后续可以点击edit进行配置信息的修改)

##### 通过 REST API 接口进行设置
1. 创建: POST <JIRA_URL>/rest/webhooks/1.0/webhook
body example
``` json
{
  "name": "my first webhook via rest",
  "url": "http://www.example.com/webhooks",
  "events": [
    "jira:issue_created",
    "jira:issue_updated"
  ],
  "jqlFilter": "Project = JRA AND resolution = Fixed",
  "excludeIssueDetails" : false
}
```
创建成功后会以json的形式返回webhook详情以及其他的一些信息。

2. 删除: DELETE <JIRA_URL>/rest/webhooks/1.0/webhook/{webhook_id} 
3. 查询: 
webhook 列表: GET <JIRA_URL>/rest/webhooks/1.0/webhook
webhook byID：GET <JIRA_URL>/rest/webhooks/1.0/webhook/{webhook_id}

#### 配置
##### 事件
Webhook是基于事件触发的，比如创建一个新的issue，添加一条新的评论等等。接收webhook的服务端会接收到一个post请求，json中包含事件的详细信息。创建一个issue的例子如下, "jira:issue_created"就是对应的事件名称，body中还包括issue（project/comment）等相关的详细信息:
``` json
{
    "timestamp":1482907414627,
    "webhookEvent":"jira:issue_created",
    "issue_event_type_name":"issue_created",
    "user":{
        "self":"http://localhost:8080/rest/api/2/user?username=yaoyao",
        "name":"yaoyao",
        "key":"yaoyao",
        "emailAddress":"yaoyao@uyunsoft.cn",
        "avatarUrls":{
            "48x48":"http://localhost:8080/secure/useravatar?avatarId=10346",
            "24x24":"http://localhost:8080/secure/useravatar?size=small&avatarId=10346",
            "16x16":"http://localhost:8080/secure/useravatar?size=xsmall&avatarId=10346",
            "32x32":"http://localhost:8080/secure/useravatar?size=medium&avatarId=10346"
        },
        "displayName":"yaoyao",
        "active":true,
        "timeZone":"Asia/Shanghai"
    },
    "issue":{
        "id":"10000",
        "self":"http://localhost:8080/rest/api/2/issue/10000",
        "key":"TEST-1",
        "fields":{
            "issuetype":{
                "self":"http://localhost:8080/rest/api/2/issuetype/10006",
                "id":"10006",
                "description":"A problem which impairs or prevents the functions of the product.",
                "iconUrl":"http://localhost:8080/secure/viewavatar?size=xsmall&avatarId=10303&avatarType=issuetype",
                "name":"Bug",
                "subtask":false,
                "avatarId":10303
            },
            "components":[

            ],
            "timespent":null,
            "timeoriginalestimate":null,
            "description":"a",
            "project":{
                "self":"http://localhost:8080/rest/api/2/project/10000",
                "id":"10000",
                "key":"TEST",
                "name":"test",
                "avatarUrls":{
                    "48x48":"http://localhost:8080/secure/projectavatar?avatarId=10324",
                    "24x24":"http://localhost:8080/secure/projectavatar?size=small&avatarId=10324",
                    "16x16":"http://localhost:8080/secure/projectavatar?size=xsmall&avatarId=10324",
                    "32x32":"http://localhost:8080/secure/projectavatar?size=medium&avatarId=10324"
                }
            },
            "fixVersions":[

            ],
            "aggregatetimespent":null,
            "resolution":null,
            "timetracking":{

            },
            "customfield_10005":"0|hzzzzz:",
            "attachment":[

            ],
            "aggregatetimeestimate":null,
            "resolutiondate":null,
            "workratio":-1,
            "summary":"test",
            "lastViewed":null,
            "watches":{
                "self":"http://localhost:8080/rest/api/2/issue/TEST-1/watchers",
                "watchCount":0,
                "isWatching":false
            },
            "creator":{
                "self":"http://localhost:8080/rest/api/2/user?username=yaoyao",
                "name":"yaoyao",
                "key":"yaoyao",
                "emailAddress":"yaoyao@uyunsoft.cn",
                "avatarUrls":{
                    "48x48":"http://localhost:8080/secure/useravatar?avatarId=10346",
                    "24x24":"http://localhost:8080/secure/useravatar?size=small&avatarId=10346",
                    "16x16":"http://localhost:8080/secure/useravatar?size=xsmall&avatarId=10346",
                    "32x32":"http://localhost:8080/secure/useravatar?size=medium&avatarId=10346"
                },
                "displayName":"yaoyao",
                "active":true,
                "timeZone":"Asia/Shanghai"
            },
            "subtasks":[

            ],
            "created":"2016-12-28T14:43:33.762+0800",
            "reporter":{
                "self":"http://localhost:8080/rest/api/2/user?username=yaoyao",
                "name":"yaoyao",
                "key":"yaoyao",
                "emailAddress":"yaoyao@uyunsoft.cn",
                "avatarUrls":{
                    "48x48":"http://localhost:8080/secure/useravatar?avatarId=10346",
                    "24x24":"http://localhost:8080/secure/useravatar?size=small&avatarId=10346",
                    "16x16":"http://localhost:8080/secure/useravatar?size=xsmall&avatarId=10346",
                    "32x32":"http://localhost:8080/secure/useravatar?size=medium&avatarId=10346"
                },
                "displayName":"yaoyao",
                "active":true,
                "timeZone":"Asia/Shanghai"
            },
            "customfield_10000":null,
            "aggregateprogress":{
                "progress":0,
                "total":0
            },
            "priority":{
                "self":"http://localhost:8080/rest/api/2/priority/3",
                "iconUrl":"http://localhost:8080/images/icons/priorities/medium.svg",
                "name":"Medium",
                "id":"3"
            },
            "labels":[

            ],
            "customfield_10004":null,
            "environment":null,
            "timeestimate":null,
            "aggregatetimeoriginalestimate":null,
            "versions":[

            ],
            "duedate":null,
            "progress":{
                "progress":0,
                "total":0
            },
            "comment":{
                "comments":[

                ],
                "maxResults":0,
                "total":0,
                "startAt":0
            },
            "issuelinks":[

            ],
            "votes":{
                "self":"http://localhost:8080/rest/api/2/issue/TEST-1/votes",
                "votes":0,
                "hasVoted":false
            },
            "worklog":{
                "startAt":0,
                "maxResults":20,
                "total":0,
                "worklogs":[

                ]
            },
            "assignee":null,
            "updated":"2016-12-28T14:43:33.762+0800",
            "status":{
                "self":"http://localhost:8080/rest/api/2/status/10000",
                "description":"",
                "iconUrl":"http://localhost:8080/",
                "name":"To Do",
                "id":"10000",
                "statusCategory":{
                    "self":"http://localhost:8080/rest/api/2/statuscategory/2",
                    "id":2,
                    "key":"new",
                    "colorName":"blue-gray",
                    "name":"To Do"
                }
            }
        }
    }
}
```
详细的事件请查看官方文档[Registering events for a webhook][3]
一种方便的做法是注册一个所有事件的钩子，这样就可以根据后续的需求来修改代码而不需要对钩子的事件进行配置。

##### Webhook Callback 格式
一个和issue相关的回调JSON的结构如下，具体的对象与JIRA REST API 保持一致，发送成功返回200：
``` json
{ 
	"timestamp"
 	"event"
	"user": {
			   --> See User shape in table below
	},
	"issue": { 
               --> See Issue shape in table below
	},
	"changelog" : {
			   --> See Changelog shape in table below	
	},
	"comment" : {
			   --> See Comment shape in table below  
	}
}
```




  [1]: https://developer.atlassian.com/static/connect/docs/latest/modules/common/webhook.html
  [2]: https://developer.atlassian.com/jiradev/jira-apis/webhooks#Webhooks-configure
  [3]: https://developer.atlassian.com/jiradev/jira-apis/webhooks
