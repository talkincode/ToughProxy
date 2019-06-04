if (!window.toughsocks.admin.config)
    toughsocks.admin.config={};


toughsocks.admin.config.loadPage = function(session){
    var cview = {
        id:"toughsocks.admin.config",
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
    toughsocks.admin.methods.addTabView("toughsocks.admin.config","cogs","系统配置", cview, true);
};


