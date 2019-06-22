if (!window.toughsocks.admin.user)
    toughsocks.admin.user={};


toughsocks.admin.user.dataViewID = "toughsocks.admin.user.dataViewID";
toughsocks.admin.user.loadPage = function(session,keyword){
    var tableid = webix.uid();
    var queryid = webix.uid();
    toughsocks.admin.user.reloadData = function(){
        $$(tableid).refresh();
        $$(tableid).clearAll();
        var params = $$(queryid).getValues();
        var args = [];
        for(var k in params){
            args.push(k+"="+params[k]);
        }
        $$(tableid).load('/admin/user/query?'+args.join("&"));
    }

    var reloadData = toughsocks.admin.user.reloadData;

    var cview = {
        id: "toughsocks.admin.user",
        css:"main-panel",padding:10,
        rows:[
            {
                id:toughsocks.admin.user.dataViewID,
                rows:[
                    {
                        view: "toolbar",
                        height:40,
                        css: "page-toolbar",
                        cols: [
                            {
                                view: "button", type: "form", width: 70, icon: "plus", label: "创建用户", click: function () {
                                    toughsocks.admin.user.OpenUserForm(session);
                                }
                            },
                            {
                                view: "button", type: "form", width: 70, icon: "plus", label: "批量创建", click: function () {
                                    toughsocks.admin.user.batchOpenUserForm(session);
                                }
                            },
                            {
                                view: "button", type: "form", width: 55, icon: "key", label: "改密码", click: function () {
                                    var item = $$(tableid).getSelectedItem();
                                    if (item) {
                                        toughsocks.admin.user.userUppwd(session, item, function () {
                                            reloadData();
                                        });
                                    } else {
                                        webix.message({ type: 'error', text: "请选择一项", expire: 1500 });
                                    }
                                }
                            },
                            {
                                view: "button", type: "danger", width: 45, icon: "times", label: "删除",  click: function () {
                                    var rows = [];
                                    $$(tableid).eachRow(
                                        function (row) {
                                            var item = $$(tableid).getItem(row);
                                            if (item && item.state === 1) {
                                                rows.push(item.id)
                                            }
                                        }
                                    );
                                    if (rows.length === 0) {
                                        webix.message({ type: 'error', text: "请至少勾选一项", expire: 1500 });
                                    } else {
                                        toughsocks.admin.user.userDelete(rows.join(","), function () {
                                            reloadData();
                                        });
                                    }
                                }
                            },
                            {}
                        ]
                    },
                    {
                        rows: [
                            {
                                id: queryid,
                                css:"query-form",
                                view: "form",
                                hidden: false,
                                paddingX: 10,
                                paddingY: 5,
                                elementsConfig: {minWidth:180},
                                elements: [
                                    {
                                       type:"space", id:"a1", paddingY:0, rows:[{
                                         type:"space", padding:0,responsive:"a1", cols:[
                                                { view: "datepicker", name: "createTime", label: "创建时间不超过", labelWidth:100, stringResult: true,timepicker: true, format: "%Y-%m-%d" },
                                                { view: "datepicker", name: "expireTime", label: "到期时间不超过", labelWidth:100,stringResult: true, format: "%Y-%m-%d" },
                                                {
                                                    view: "richselect", css:"nborder-input2", name: "status", value:"1", label: "用户状态", icon: "caret-down",
                                                    options: [
                                                        { id: '1', value: "正常" },
                                                        { id: '0', value: "停用" },
                                                        { id: '2', value: "已到期" }
                                                    ]
                                                },
                                                {view: "text", css:"nborder-input2",  name: "username", label: "用户名", placeholder: "帐号精确匹配", width:240},
                                                {view: "text", css:"nborder-input2",  name: "keyword", label: "",labelWidth:0,  value: keyword || "", placeholder: "帐号模糊匹配", width:180},

                                            {
                                                cols:[
                                                    {view: "button", label: "查询", type: "icon", icon: "search", borderless: true, width: 64, click: function () {
                                                        reloadData();
                                                    }},
                                                    {
                                                        view: "button", label: "重置", type: "icon", icon: "refresh", borderless: true, width: 64, click: function () {
                                                            $$(queryid).setValues({
                                                                createTime: "",
                                                                expireTime: "",
                                                                keyword: "",
                                                                status: ""
                                                            });
                                                        }
                                                    },{}
                                                ]
                                            }
                                         ]}
                                       ]
                                    }
                                ]
                            },
                            {
                                id: tableid,
                                view: "datatable",
                                rightSplit: 2,
                                columns: [
                                    { id: "state", header: { content: "masterCheckbox", css: "center" }, width: 35, css: "center", template: "{common.checkbox()}" },
                                    { id: "id", header: ["ID"], hidden:true},
                                    { id: "username", header: ["帐号"],adjust:true},
                                    { id: "realname", header: ["姓名"],adjust:true},
                                    {
                                        id: "status", header: ["状态"], sort: "string",  adjust:true, template: function (obj) {
                                            if(new Date(obj.expireTime) < new Date()) {
                                                return "<span style='color:orange;'>过期</span>";
                                            }else if (obj.status === 0) {
                                                return "<span style='color:orange;'>停用</span>";
                                            } else if (obj.status === 1) {
                                                return "<span style='color:green;'>正常</span>";
                                            }

                                        }
                                    },
                                    { id: "expireTime", header: ["过期时间"],sort:"date",adjust:true},
                                    { id: "mobile", header: ["电话"] ,adjust:true},
                                    { id: "email", header: ["电子邮件"] ,adjust:true},
                                    { id: "maxSession", header: ["最大在线"],adjust:true},
                                    { id: "upLimit", header: ["上行限速(KB)"],adjust:true},
                                    { id: "downLimit", header: ["下行限速(KB)"],adjust:true},
                                    { id: "remark", header: ["备注"],fillspace:true},
                                    { id: "opt", header: '操作', adjust:true,template: function(obj){
                                           var actions = [];
                                            actions.push("<span title='修改' class='table-btn do_update'><i class='fa fa-edit'></i></span> ");
                                            // actions.push("<span title='删除账号' class='table-btn do_delete'><i class='fa fa-times'></i></span> ");
                                           return actions.join(" ");
                                    }},
                                    { header: { content: "headerMenu" }, headermenu: false, width: 32 }
                                ],
                                select: true,
                                tooltip:true,
                                hover:"tab-hover",
                                autoConfig:true,
                                clipboard:true,
                                resizeColumn: true,
                                autoWidth: true,
                                autoHeight: true,
                                url: "/admin/user/query",
                                pager: "subs_dataPager",
                                datafetch: 40,
                                loadahead: 15,
                                ready: function () {
                                    if (keyword) {
                                        reloadData();
                                    }
                                },
                                onClick: {
                                    do_update: function(e, id){
                                        toughsocks.admin.user.userUpdate(session, this.getItem(id), function () {
                                            reloadData();
                                        });
                                    }
                                }
                            },
                            {
                                paddingY: 3,
                                cols: [
                                    {
                                        view: "richselect", name: "page_num", label: "每页显示", value: 20,width:130,labelWidth:60,
                                        options: [{ id: 20, value: "20" },
                                            { id: 50, value: "50" },
                                            { id: 100, value: "100" },
                                            { id: 500, value: "500" },
                                            { id: 1000, value: "1000" }],on: {
                                            onChange: function (newv, oldv) {
                                                $$("subs_dataPager").define("size",parseInt(newv));
                                                $$(tableid).refresh();
                                                reloadData();
                                            }
                                        }
                                    },
                                    {
                                        id: "subs_dataPager", view: 'pager', master: false, size: 20, group: 5,
                                        template: '{common.first()} {common.prev()} {common.pages()} {common.next()} {common.last()} total:#count#'
                                    },{},

                                ]
                            }
                        ]
                    },
                ]
            },
            {
                id: toughsocks.admin.user.detailFormID,
                hidden:true
            }
        ]
    };
    toughsocks.admin.methods.addTabView("toughsocks.admin.user","user-o","用户管理", cview, true);
    webix.extend($$(tableid), webix.ProgressBar);
};


/**
 * 新用户报装
 * @param session
 * @constructor
 */
toughsocks.admin.user.OpenUserForm = function(session){
    var winid = "toughsocks.admin.user.OpenUserForm";
    if($$(winid))
        return;
    var formid = winid+"_form";
    webix.ui({
        id:winid,
        view: "window",
        css:"win-body",
        move:true,
        width:340,
        height:480,
        position: "center",
        head: {
            view: "toolbar",
            css:"win-toolbar",

            cols: [
                {view: "icon", icon: "laptop", css: "alter"},
                {view: "label", label: "创建用户"},
                {view: "icon", icon: "times-circle", css: "alter", click: function(){
                        $$(winid).close();
                    }}
            ]
        },
        body: {
            rows:[
                {
                    id: formid,
                    view: "form",
                    scroll: 'y',
                    elementsConfig: { labelWidth: 110 },
                    elements: [
                        { view: "richselect", name: "groupId", label: "用户组(*)", icon: "caret-down", validate:webix.rules.isNotEmpty,
                            options: {view:"suggest",url:"/admin/group/options"}
                        },
                        { view: "text", name: "realname", label: "姓名", validate:webix.rules.isNotEmpty },
                        { view: "text", name: "username", label: "帐号", validate:webix.rules.isNotEmpty },
                        { view: "text", name: "email", label: "电子邮箱"},
                        { view: "text", name: "mobile", label: "电话"},
                        { view: "text", name: "password", label: "认证密码", validate:webix.rules.isNotEmpty},
                        { view: "datepicker", name: "expireTime", label: "过期时间", stringResult:true, timepicker: true, format: "%Y-%m-%d %h:%i", validate:webix.rules.isNotEmpty },
                        { view:"textarea", name:"remark", height:100, label:"描述"},
                    ]
                },
                {
                    view: "toolbar",
                    height:42,
                    css: "page-toolbar",
                    cols: [
                        {},
                        {
                            view: "button", type: "form", width: 100, icon: "check-circle", label: "提交", click: function () {
                                if (!$$(formid).validate()) {
                                    webix.message({ type: "error", text: "请正确填写资料", expire: 1000 });
                                    return false;
                                }
                                var btn = this;
                                btn.disable();
                                var params = $$(formid).getValues();
                                webix.ajax().post('/admin/user/create', params).then(function (result) {
                                    btn.enable();
                                    var resp = result.json();
                                    webix.message({ type: resp.msgtype, text: resp.msg, expire: 3000 });
                                    if (resp.code === 0) {
                                        toughsocks.admin.user.reloadData();
                                        $$(winid).close();
                                    }
                                });
                            }
                        },
                        {
                            view: "button", type: "base", width: 100, icon: "times-circle", label: "取消", click: function () {
                                $$(winid).close();
                            }
                        }
                    ]
                }
            ]
        }

    }).show();
};


/**
 * 批量开用户
 * @param session
 * @constructor
 */
toughsocks.admin.user.batchOpenUserForm = function(session){
    var winid = "toughsocks.admin.user.batchOpenUserForm";
    if($$(winid))
        return;
    var formid = winid+"_form";
    webix.ui({
        id:winid,
        view: "window",
        css:"win-body",
        move:true,
        width:340,
        height:480,
        position: "center",
        head: {
            view: "toolbar",
            css:"win-toolbar",

            cols: [
                {view: "icon", icon: "laptop", css: "alter"},
                {view: "label", label: "批量创建用户"},
                {view: "icon", icon: "times-circle", css: "alter", click: function(){
                        $$(winid).close();
                    }}
            ]
        },
        body: {
            rows:[
                {
                    id: formid,
                    view: "form",
                    scroll: 'y',
                    elementsConfig: { labelWidth: 110 },
                    elements: [
                        { view: "richselect", name: "groupId", label: "用户组(*)", icon: "caret-down", validate:webix.rules.isNotEmpty,
                            options: {view:"suggest",url:"/admin/group/options"}
                        },
                        { view: "text", name: "userPrefix", label: "帐号前缀", validate:webix.rules.isNotEmpty },
                        { view: "counter", name: "openNum", label: "数量", placeholder: "数量（最大1000）", value: 10, min: 10, max: 1000},
                        { view: "radio", name: "randPasswd", label: "密码类型 ", value: '0', options: [{ id: '1', value: "随机" }, { id: '0', value: "固定" }] },
                        { view: "text", name: "password", label: "固定密码"},
                        { view: "datepicker", name: "expireTime", label: "过期时间", stringResult:true, timepicker: true, format: "%Y-%m-%d %h:%i", validate:webix.rules.isNotEmpty },
                    ]
                },
                {
                    view: "toolbar",
                    height:42,
                    css: "page-toolbar",
                    cols: [
                        {},
                        {
                            view: "button", type: "form", width: 100, icon: "check-circle", label: "提交", click: function () {
                                if (!$$(formid).validate()) {
                                    webix.message({ type: "error", text: "请正确填写资料", expire: 1000 });
                                    return false;
                                }
                                var btn = this;
                                btn.disable();
                                var params = $$(formid).getValues();
                                webix.ajax().post('/admin/user/batchcreate', params).then(function (result) {
                                    btn.enable();
                                    var resp = result.json();
                                    webix.message({ type: resp.msgtype, text: resp.msg, expire: 3000 });
                                    if (resp.code === 0) {
                                        toughsocks.admin.user.reloadData();
                                        $$(winid).close();
                                    }
                                });
                            }
                        },
                        {
                            view: "button", type: "base", width: 100, icon: "times-circle", label: "取消", click: function () {
                                $$(winid).close();
                            }
                        }
                    ]
                }
            ]
        }

    }).show();
};




toughsocks.admin.user.userUpdate = function(session,item,callback){
    var updateWinid = "toughsocks.admin.user.userUpdate";
    if($$(updateWinid))
        return;
    var formid = updateWinid+"_form";
    webix.ajax().get('/admin/user/detail', {id:item.id}).then(function (result) {
        var resp = result.json();
        if(resp.code>0){
            webix.message({ type: "error", text: resp.msg, expire: 3000 });
            return;
        }
        var subs = resp.data;
        webix.ui({
            id:updateWinid,
            view: "window",
            css:"win-body",
            move:true,
            resize:true,
            width:360,
            height:480,
            position: "center",
            head: {
                view: "toolbar",
                css:"win-toolbar",

                cols: [
                    {view: "icon", icon: "laptop", css: "alter"},
                    {view: "label", label: "帐号修改"},
                    {view: "icon", icon: "times-circle", css: "alter", click: function(){
                        $$(updateWinid).close();
                    }}
                ]
            },
            body: {
                borderless: true,
                padding:5,
                rows:[
                {
                    id: formid,
                    view: "form",
                    scroll: "y",
                    elementsConfig: { labelWidth: 120 },
                    paddingX:10,
                    elements: [
                        { view: "text", name: "id",  hidden: true, value: subs.id },
                        { view: "text", name: "username", label: "帐号", css: "nborder-input", readonly: true, value: subs.username , validate:webix.rules.isNotEmpty},
                        { view: "text", name: "realname", label: "姓名",value: subs.realname , validate:webix.rules.isNotEmpty},
                        { view: "text", name: "email", label: "电子邮箱",value: subs.email},
                        { view: "text", name: "mobile", label: "电话",value: subs.mobile},
                        { view: "radio", name: "status", label: "状态", value: subs.status, options: [{ id: '1', value: "正常" }, { id: '0', value: "停用" }] },
                        {
                            view: "datepicker", name: "expireTime", timepicker: true, value:subs.expireTime,
                            label: "过期时间", stringResult: true,  format: "%Y-%m-%d %h:%i", validate: webix.rules.isNotEmpty
                        },
                        { view: "radio", name: "group_policy", label: "用户组策略", value: subs.groupPolicy, options: [{ id: '0', value: "否" }, { id: '1', value: "是" }] },
                        { view: "text", name: "maxSession", label: "最大连接数",  value: subs.maxSession },
                        { view: "text", name: "upLimit", label: "上行限速",  value: subs.upLimit },
                        { view: "text", name: "downLimit", label: "下行限速",  value: subs.downLimit },
                        {
                            cols:[
                                { view: "textarea", name: "remark", label: "备注",value: subs.remark, height: 80 }
                            ]
                        }
                    ]
                },
                {
                    height:36,
                    css: "panel-toolbar",
                    cols: [{},
                        {
                            view: "button", type: "form", width: 70, icon: "check-circle", label: "提交", click: function () {
                                if (!$$(formid).validate()) {
                                    webix.message({ type: "error", text: "请正确填写资料", expire: 1000 });
                                    return false;
                                }
                                var btn = this;
                                btn.disable();
                                var params = $$(formid).getValues();
                                params.subs_id = item.id;
                                webix.ajax().post('/admin/user/update', params).then(function (result) {
                                    btn.enable();
                                    var resp = result.json();
                                    webix.message({ type: resp.msgtype, text: resp.msg, expire: 3000 });
                                    if (resp.code === 0) {
                                        toughsocks.admin.user.reloadData();
                                         $$(updateWinid).close();
                                    }
                                });
                            }
                        },
                        {view: "button", type: "base", width: 70, icon: "check-circle", label: "取消", click: function(){$$(updateWinid).close()}}

                    ]
                }
            ]}
        }).show();
    })
};

toughsocks.admin.user.userUppwd = function(session,item,callback){
    var winid = "toughsocks.admin.user.userUppwd";
    if($$(winid))
        return;
    var formid = winid+"_form";
    webix.ui({
        id:winid,
        view: "window",
        css:"win-body",
        move:true,
        width:360,
        height:480,
        position: "center",
        head: {
            view: "toolbar",
            css:"win-toolbar",

            cols: [
                {view: "icon", icon: "laptop", css: "alter"},
                {view: "label", label: "帐号密码修改"},
                {view: "icon", icon: "times-circle", css: "alter", click: function(){
                        $$(winid).close();
                    }}
            ]
        },
        body:{
            borderless: true,
            padding:5,
            rows:[
                {
                    id: formid,
                    view: "form",
                    scroll: "auto",
                    elementsConfig: { labelWidth: 120 },
                    elements: [
                        { view: "text", name: "id",  hidden: true, value: item.id },
                        { view: "text", name: "password", type: "password", label: "新密码(*)", placeholder: "新密码", validate: webix.rules.isNotEmpty },
                        { view: "text", name: "cpassword", type: "password", label: "确认新密码(*)", placeholder: "确认新密码", validate: webix.rules.isNotEmpty }
                    ]
                },
                {
                    height:36,
                    cols: [{},
                        {
                            view: "button", type: "form", width: 70, icon: "check-circle", label: "提交", click: function () {
                                if (!$$(formid).validate()) {
                                    webix.message({ type: "error", text: "请正确填写资料", expire: 1000 });
                                    return false;
                                }
                                var btn = this;
                                btn.disable();
                                var params = $$(formid).getValues();
                                params.id = item.id;
                                webix.ajax().post('/admin/user/uppwd', params).then(function (result) {
                                    btn.enable();
                                    var resp = result.json();
                                    webix.message({ type: resp.msgtype, text: resp.msg, expire: 3000 });
                                    if (resp.code === 0) {
                                        toughsocks.admin.user.reloadData();
                                        $$(winid).close();
                                    }
                                });
                            }
                        },
                        {
                            view: "button", type: "base", icon: "times-circle", width: 70, css: "alter", label: "关闭", click: function () {
                                $$(winid).close();
                            }
                        }
                    ]
                }
            ]
        }
    }).show(0)
};



toughsocks.admin.user.userDelete = function (ids,callback) {
    webix.confirm({
        title: "操作确认",
        ok: "是", cancel: "否",
        text: "删除帐号会同时删除相关所有数据，此操作不可逆，确认要删除吗？",
        width:360,
        callback: function (ev) {
            if (ev) {
                webix.ajax().get('/admin/user/delete', {ids:ids}).then(function (result) {
                    var resp = result.json();
                    webix.message({type: resp.msgtype, text: resp.msg, expire: 1500});
                    if(callback)
                        callback()
                });
            }
        }
    });
};


