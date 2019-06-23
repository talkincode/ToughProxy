if (!window.toughproxy)
    toughproxy={};

// if (!webix.env.touch && webix.ui.scrollSize){
//     webix.CustomScroll.init();
// }


currentLang = navigator.language;
if(!currentLang){
    currentLang = navigator.browserLanguage;
}
webix.i18n.setLocale(currentLang);

toughproxy.admin = {};
toughproxy.admin.tabsId = "toughproxy.admin-main-tabs";
toughproxy.admin.viewsId = "toughproxy.admin-main-views";
toughproxy.admin.tabviews = "toughproxy.admin-main-tabviews";
toughproxy.admin.toolbarId = "toughproxy.admin-main-toolbar";
toughproxy.admin.actions = {};
toughproxy.admin.methods = {};

toughproxy.admin.methods.addTabView = function (vid, icon, title, tabview, close){
    if(!$$(vid)){
        $$(toughproxy.admin.viewsId).addView(tabview);
        $$(toughproxy.admin.tabsId).addOption({id:vid, value:title, close:close, icon:icon}, true);
        $$(vid).show(true,false);
        $$(toughproxy.admin.viewsId).refresh();
        $$(toughproxy.admin.tabviews).refresh();
    }else{
        $$(toughproxy.admin.tabsId).addOption({id:vid, value:title, close:close, icon:icon}, true);
        $$(toughproxy.admin.tabsId).setValue(vid);
        $$(toughproxy.admin.tabsId).showOption(vid);
        $$(vid).show(true,false);
        $$(toughproxy.admin.viewsId).refresh();
        $$(toughproxy.admin.tabviews).refresh();
    }

};

toughproxy.admin.methods.doLogin = function (formValues){
    webix.ajax().post('/login',formValues).then(function (result) {
        var resp = result.json();
        if (resp.code===0){
            window.location.href = "/admin";
        }else{
            webix.message({type: resp.msgtype, text:resp.msg,expire:500});
        }
    }).fail(function (xhr) {
        webix.message({type: 'error', text: "登录失败:"+xhr.statusText,expire:500});
    });
};

toughproxy.admin.methods.showBusyBar = function (viewid,delay, callback){
    $$(viewid).disable();
    $$(viewid).showProgress({
        type:"top",
        delay:delay,
        hide:true
    });
    setTimeout(function(){
        callback();
        $$(viewid).enable();
    }, delay);
};

toughproxy.admin.initUploadApi = function(uid, uploadurl, callback){
     webix.ui({
        id:uid,
        view:"uploader",
        upload:uploadurl,
        on:{
            onBeforeFileAdd:function(item){
                 item.formData = {};
                 webix.message({type: "info", text: "正在上传..", expire: 3000})
            },
            onFileUpload:function(item){
                if(callback){
                    callback(item);
                }
            },
            onFileUploadError:function(item){
                webix.message({type:"error",text:"Error during file upload",expire:3000});
            },
            onUploadComplete:function(resp){
                webix.message({type: resp.msgtype, text: resp.msg, expire: 5000});
            }
        },
        apiOnly:true
    });
};


toughproxy.admin.methods.updatePassword = function(hnode){
    var pwinid = webix.uid();
    var formid = webix.uid();
    webix.ui({
        id:pwinid,
        view:"popup",
        width:270,
        height:270,
        body:{
            rows:[
                {
                    id: formid,
                    view: "form",
                    scroll: false,
                    elementsConfig: {},
                    elements: [
                        {view: "text", name: "oldpassword", type: "password", label: "原密码", validate:webix.rules.isNotEmpty},
                        {view: "text", name: "password1", type: "password", label: "新密码", validate:webix.rules.isNotEmpty},
                        {view: "text", name: "password2", type: "password", label: "确认新密码", validate:webix.rules.isNotEmpty}
                    ]
                },
                {
                    padding:5,
                    cols: [{},
                        {
                            view: "button", name: "submit", type: "form", value: "提交修改", width: 90, height: 36,
                            click: function () {
                                if (!$$(formid).validate()) {
                                    webix.message({type: "error", text: "请正确填写资料", expire: 1000});
                                    return false;
                                }
                                var btn = this;
                                webix.ajax().post('/admin/password', $$(formid).getValues()).then(function (result) {
                                    btn.enable();
                                    var resp = result.json();
                                    console.log(resp);
                                    webix.message({type: resp.msgtype, text: resp.msg, expire: 3000});
                                    if (resp.code === 0) {
                                        $$(pwinid).close();
                                    }
                                });
                            }
                        },
                        {
                            view: "button", type: "base", icon: "times-circle", width: 70, css: "alter", label: "取消", click: function () {
                                $$(pwinid).close();
                            }
                        }
                    ]
                }
            ]
        }
    }).show(hnode);
};

toughproxy.admin.methods.requirejs = function(jsname, session,callback){
    console.log("load admin/" + jsname + ".js");
    if(session.dev_mode === 'enabled'){
         webix.require("admin/" + jsname + ".js?rand="+new Date().getTime(), function () {
            callback();
         });
    }else{
        webix.require("admin/" + jsname + ".js", function () {
             callback();
        });
    }
};


webix.ready(function() {
    webix.ajax().get('/admin/session',{v:new Date().getTime()}).then(function (result) {
        var resp = result.json();
        if(resp.code===1){
            webix.message({type:"error",text:resp.msg});
            setTimeout(function(){window.location.href = "/admin/login";},1000);
            return false;
        }
        var session = resp.data;
        webix.require("sidebar.js", function () {
            webix.require("css/sidebar.css");
            webix.ui({
                rows: [
                    {
                        view: "toolbar",
                        padding: 3,
                        height: 44,
                        css: "page-nav",
                        elements: [
                            {
                                cols: [
                                    { view: "template", css: "nav-logo", maxWidth:188, template: "<a href='/admin'><img src='/static/imgs/logo.png' width='175' height='34'/></a>", height:40},
                                    {
                                        view: "button", type: "icon", icon: "bars", width: 37, align: "left", css: "nav-item-color", click: function () {
                                            $$("$sidebar1").toggle()
                                        }
                                    },
                                    {},
                                    {
                                        view: "button", css: "nav-item-color", type: "icon", width: 90, maxWidth: 200, icon: "key",align:"right",
                                        label: "修改密码", click: function () {
                                            toughproxy.admin.methods.updatePassword(this.$view);
                                        }
                                    },
                                    {
                                        view: "button", css: "nav-item-color", type: "icon", width: 70, icon: "sign-out",align:"right",
                                        label: "退出", click: function () {
                                            window.location.href = "/admin/logout";
                                        }
                                    }
                                ]
                            }

                        ]
                    },
                    {
                        borderless:true,
                        cols: [
                            {
                                rows:[
                                    {
                                        rows: [
                                            { view: "label", height:40, css: "sideber-label", label: "<i class=\"fa fa-bars\" aria-hidden=\"true\"></i> 功能导航" },
                                            {
                                                view: "sidebar",
                                                scroll:"auto",
                                                width: 180,
                                                data: session.menudata,
                                                on: {
                                                    onAfterSelect: function (id) {
                                                        try {
                                                            console.log("action = " + id);
                                                            webix.require("admin/" + id + ".js?rand="+new Date().getTime(), function () {
                                                                toughproxy.admin[id].loadPage(session);
                                                            });
                                                        } catch (err) {
                                                            console.log(err);
                                                        }
                                                    }
                                                },
                                                ready: function () {
                                                    webix.require("admin/dashboard.js?rand="+new Date().getTime(), function () {
                                                        toughproxy.admin.dashboard.loadPage(session);
                                                    });
                                                }
                                            }
                                        ]
                                    }
                                ]
                            },
                            {
                                rows:[
                                    {
                                        id:toughproxy.admin.tabviews,
                                        rows:[
                                            {
                                                id:toughproxy.admin.tabsId, view:"tabbar",css:"main-tabs",
                                                animate:false,
                                                bottomOffset:10,
                                                optionWidth: 180,
                                                align:'left',
                                                multiview:true,
                                                options:[],
                                                height:50
                                            },
                                            { id:toughproxy.admin.viewsId, animate:false,cells:[
                                                {view:"template", id:"tpl", template:"0000"}
                                            ]}
                                        ]
                                    },
                                    {
                                        css:"page-footer",
                                        height:36,
                                        borderless:true,
                                        cols:[
                                            {},{view:"label", css:"Copyright", label:"Copyright © TOUGHPROXY 版权所有，侵权必究！"}, {}
                                        ]
                                    }
                                ]
                            }
                        ]
                    }
                ]
            });

        });
    });

});

