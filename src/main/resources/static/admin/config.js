if (!window.toughproxy.admin.config)
    toughproxy.admin.config={};


toughproxy.admin.config.loadPage = function(session){
    var cview = {
        id:"toughproxy.admin.config",
        css:"main-panel",
        padding:10,
        // borderless:true,
        view:"tabview",
        tabbar:{
            optionWidth:160,
        },
        cells:[
            {
                header:"系统设置",
                body:{
                    id: "system_settings",
                    view: "form",
                    paddingX:10,
                    elementsConfig: {
                        labelWidth:160,
                        // labelPosition:"top"
                    },
                    url:"/admin/config/load/system",
                    elements: [
                        { view: "fieldset", label: "基本设置",  body: {
                            rows:[
                                {view: "counter", name: "systemTicketHistoryDays", label: "网络日志保存最大天数",  value:180},
                                {view: "richselect", name: "systemSocksUserAuthMode", label: "用户认证模式",value:"free", options:[
                                        {id:"free",value:"免认证"},
                                        {id:"local",value:"本地"},
                                        {id:"radius",value:"RADIUS"}
                                ]},
                                {view: "text", name: "systemSocksRadiusNasid", label: "RADIUS NAS标识"},
                                {view: "text", name: "systemSocksRadiusAuthServer", label: "RADIUS 服务器"},
                                {view: "text", name: "systemSocksRadiusAuthPort", label: "RADIUS 认证端口"},
                                {view: "text", type:"password", name: "systemSocksRadiusAuthSecret", label: "RADIUS 共享密钥"}
                            ]
                        }},
                        {
                            cols: [
                                {view: "button", name: "submit", type: "form", value: "保存配置", width: 120, height:36, click: function () {
                                        if (!$$("system_settings").validate()){
                                            webix.message({type: "error", text:"请正确填写",expire:1000});
                                            return false;
                                        }
                                        var param =  $$("system_settings").getValues();
                                        param['ctype'] = 'system';
                                        webix.ajax().post('/admin/config/system/update',param).then(function (result) {
                                            var resp = result.json();
                                            webix.message({type: resp.msgtype, text: resp.msg, expire: 3000});
                                        });
                                    }
                                },
                                {}
                            ]
                        },{}
                    ]
                }
            },
            {
                header:"Socks 设置",
                body:{
                    id: "socks_settings",
                    view: "form",
                    paddingX:10,
                    elementsConfig: {
                        labelWidth:160,
                        // labelPosition:"top"
                    },
                    url:"/admin/config/load/socks",
                    elements: [
                        { view: "fieldset", label: "Socks 设置",  body: {
                                rows:[
                                    {view: "counter", name: "socksGlobalUplimit", label: "全局上行限速(MB)",  value:100},
                                    {view: "counter", name: "socksGlobalDownlimit", label: "全局上行限速(MB)",  value:100},
                                    {view: "counter", name: "sockConnUplimit", label: "默认每连接上行限速(MB)",  value:100},
                                    {view: "label", css:"form-desc", label:"对于强制认证的Socks5，取用户的上行限速值，非认证的代理采用该值，0表示不限制"},
                                    {view: "counter", name: "socksConnDownlimit", label: "默认每连接下行限速(KB)",  value:100},
                                    {view: "label", css:"form-desc", label:"对于强制认证的Socks5，取用户的下行限速值，非认证的代理采用该值，0表示不限制"},
                                    {view: "counter", name: "socksIpMaxSession", label: "默认每IP最大并发",  value:100},
                                    {view: "label", css:"form-desc", label:"对于强制认证的Socks5，并发数取用户的并发值，非认证的代理采用该值，0表示不限制"}
                                ]
                            }},
                        {
                            cols: [
                                {view: "button", name: "submit", type: "form", value: "保存配置", width: 120, height:36, click: function () {
                                        if (!$$("socks_settings").validate()){
                                            webix.message({type: "error", text:"请正确填写",expire:1000});
                                            return false;
                                        }
                                        var param =  $$("socks_settings").getValues();
                                        param['ctype'] = 'socks';
                                        webix.ajax().post('/admin/config/socks/update',param).then(function (result) {
                                            var resp = result.json();
                                            webix.message({type: resp.msgtype, text: resp.msg, expire: 3000});
                                        });
                                    }
                                },
                                {}
                            ]
                        },{}
                    ]
                }
            },
            {
                header:"短信配置",
                body:{
                    id: "sms_settings",
                    view: "form",
                    paddingX:10,
                    elementsConfig: {
                        labelWidth:160,
                        // labelPosition:"top"
                    },
                    url:"/admin/config/load/sms",
                    elements: [
                        { view: "fieldset", label: "短信网关",  body: {
                            rows:[
                                {view: "richselect", name: "smsGateway", label: "短信网关:",value:"qcloud", options:[{id:"qcloud",value:"腾讯云短信"}]},
                                {view: "text", name: "smsAppid", label: "短信网关APPID"},
                                {view: "text", type:"password", name: "smsAppkey", label: "短信网关APPKEY"},
                                {view: "text", name: "smsVcodeTemplate", label: "短信验证码模板"}
                            ]
                        }},
                        {
                            cols: [
                                {view: "button", name: "submit", type: "form", value: "保存配置", width: 120, height:36, click: function () {
                                        if (!$$("sms_settings").validate()){
                                            webix.message({type: "error", text:"请正确填写",expire:1000});
                                            return false;
                                        }
                                        var param =  $$("sms_settings").getValues();
                                        param['ctype'] = 'sms';
                                        webix.ajax().post('/admin/config/sms/update',param).then(function (result) {
                                            var resp = result.json();
                                            webix.message({type: resp.msgtype, text: resp.msg, expire: 3000});
                                        });
                                    }
                                },
                                {}
                            ]
                        },{}
                    ]
                }
            },
            {
                header:"API 设置",
                body:{
                    id: "api_settings",
                    view: "form",
                    paddingX:10,
                    elementsConfig: {
                        labelWidth:160,
                        // labelPosition:"top"
                    },
                    url:"/admin/config/load/api",
                    elements: [
                        { view: "fieldset", label: "API",  body: {
                                rows:[
                                    {view: "richselect", name: "apiType", label: "API 类型:",value:"basic", options:[{id:"basic",value:"Basic"}]},
                                    {view: "text", name: "apiUsername", label: "Basic 用户"},
                                    {view: "text", type:"password", name: "apiPasswd", label: "Basic 密码"},
                                    {view: "text", name: "apiAllowIplist", label: "IP 白名单"},
                                    {view: "text", name: "apiBlackIplist", label: "IP 黑名单"}
                                ]
                            }},
                        {
                            cols: [
                                {view: "button", name: "submit", type: "form", value: "保存配置", width: 120, height:36, click: function () {
                                        if (!$$("api_settings").validate()){
                                            webix.message({type: "error", text:"请正确填写",expire:1000});
                                            return false;
                                        }
                                        var param =  $$("api_settings").getValues();
                                        param['ctype'] = 'sms';
                                        webix.ajax().post('/admin/config/api/update',param).then(function (result) {
                                            var resp = result.json();
                                            webix.message({type: resp.msgtype, text: resp.msg, expire: 3000});
                                        });
                                    }
                                },
                                {}
                            ]
                        },{}
                    ]
                }
            },
        ]


    };
    toughproxy.admin.methods.addTabView("toughproxy.admin.config","cogs","系统配置", cview, true);
};


