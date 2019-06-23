if (!window.toughproxy.admin.acl)
    toughproxy.admin.acl={};


toughproxy.admin.acl.dataViewID = "toughproxy.admin.acl.dataViewID";
toughproxy.admin.acl.loadPage = function(session,keyword){
    var tableid = webix.uid();
    var queryid = webix.uid();
    var reloadData = function(){
        $$(tableid).refresh();
        $$(tableid).clearAll();
        var params = $$(queryid).getValues();
        var args = [];
        for(var k in params){
            args.push(k+"="+params[k]);
        }
        $$(tableid).load('/admin/acl/query?'+args.join("&"));
    };

    var cview = {
        id: "toughproxy.admin.acl",
        css:"main-panel",padding:10,
        rows:[
            {
                id:toughproxy.admin.acl.dataViewID,
                rows:[
                    {
                        view: "toolbar",
                        height:40,
                        css: "page-toolbar",
                        cols: [
                            {
                                view: "button", type: "form", width: 70, icon: "plus", label: "创建策略", click: function () {
                                    toughproxy.admin.acl.newAclForm(session,function () {
                                        reloadData();
                                    });
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
                                        toughproxy.admin.acl.aclDelete(rows.join(","), function () {
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
                                                {
                                                    view: "richselect", width:180, css:"nborder-input2", name: "policy", value:"", label: "访问策略", icon: "caret-down",
                                                    options: [
                                                        { id: 'all', value: "所有" },
                                                        { id: 'accept', value: "允许" },
                                                        { id: 'reject', value: "拒绝" }
                                                    ]
                                                },
                                                {view: "text", css:"nborder-input2",  name: "src", label: "来源IP地址", placeholder: "来源IP地址/段", width:240},
                                                {view: "text", css:"nborder-input2",  name: "target", label: "目标IP地址", placeholder: "目标IP地址/段", width:240},
                                                {
                                                    cols:[
                                                        {view: "button", label: "查询", type: "icon", icon: "search", borderless: true, width: 64, click: function () {
                                                                reloadData();
                                                            }},
                                                        {
                                                            view: "button", label: "重置", type: "icon", icon: "refresh", borderless: true, width: 64, click: function () {
                                                                $$(queryid).setValues({
                                                                    ipaddr: "",
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
                                    { id: "priority", header: ["优先级"],adjust:true},
                                    { id: "src", header: ["来源 IP 地址/段"],adjust:true,template: function (obj){
                                        if (obj.src==null || obj.src === '') {
                                            return "<span style='color:blue;'>任意</span>";
                                        } else {
                                            return  obj.src;
                                        }
                                    }},
                                    { id: "target", header: ["目标 IP 地址/段"],adjust:true,template: function (obj){
                                        if (obj.target==null || obj.target === '') {
                                            return "<span style='color:blue;'>任意</span>";
                                        } else {
                                            return  obj.target;
                                        }
                                    }},
                                    { id: "domain", header: ["目标域名"],adjust:true,template: function (obj){
                                        if (obj.domain==null || obj.domain === '') {
                                            return "<span style='color:blue;'>任意</span>";
                                        } else {
                                            return  obj.domain;
                                        }
                                    }},
                                    {
                                        id: "policy", header: ["访问策略"], sort: "string",  adjust:true, template: function (obj) {
                                            if (obj.policy === 'accept') {
                                                return "<span style='color:green;'>允许</span>";
                                            } else if (obj.policy === 'reject') {
                                                return "<span style='color:orangered;'>拒绝</span>";
                                            }
                                        }
                                    },
                                    {
                                        id: "status", header: ["状态"], sort: "string",  adjust:true, template: function (obj) {
                                            if (obj.status === 1) {
                                                return "<span style='color:green;'>正常</span>";
                                            } else if (obj.status === 0) {
                                                return "<span style='color:orangered;'>停用</span>";
                                            }
                                        }
                                    },
                                    { id: "hits", header: ["匹配次数"],fillspace:true},
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
                                url: "/admin/acl/query",
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
                                        toughproxy.admin.acl.aclUpdate(session, this.getItem(id), function () {
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
                id: toughproxy.admin.acl.detailFormID,
                hidden:true
            }
        ]
    };
    toughproxy.admin.methods.addTabView("toughproxy.admin.acl","key","访问控制", cview, true);
    webix.extend($$(tableid), webix.ProgressBar);
};


/**
 * 新用户报装
 * @param session
 * @constructor
 */
toughproxy.admin.acl.newAclForm = function(session,callback){
    var winid = "toughproxy.admin.acl.newAclForm";
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
                {view: "icon", icon: "key", css: "alter"},
                {view: "label", label: "创建访问策略"},
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
                        { view: "radio", name: "policy", label: "访问策略", icon: "caret-down", validate:webix.rules.isNotEmpty,
                            options: [{id:"accept",value:"允许"},{id:"reject",value:"拒绝"}]
                        },
                        {view: "text", name: "priority", label: "优先级", validate:webix.rules.isNumber },
                        {view: "text", name: "src", label: "来源IP地址/段"},
                        {view: "label", css:"form-desc", label:"支持格式 x.x.x.x；x.x.x.x/x； x.x.x.x-x.x.x.x"},
                        {view: "text", name: "target", label: "目标IP地址/段" },
                        {view: "label", css:"form-desc", label:"支持格式 x.x.x.x；x.x.x.x/x； x.x.x.x-x.x.x.x"},
                        {view: "text", name: "domain", label: "目标域名" },
                        {view: "label", css:"form-desc", label:"目标域名和目标IP地址/段只填写一项即可"},
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
                                webix.ajax().post('/admin/acl/create', params).then(function (result) {
                                    btn.enable();
                                    var resp = result.json();
                                    webix.message({ type: resp.msgtype, text: resp.msg, expire: 3000 });
                                    if (resp.code === 0) {
                                        if(callback)
                                            callback();
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




toughproxy.admin.acl.aclUpdate = function(session,item,callback){
    var updateWinid = "toughproxy.admin.acl.aclUpdate";
    if($$(updateWinid))
        return;
    var formid = updateWinid+"_form";
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
                {view: "icon", icon: "key", css: "alter"},
                {view: "label", label: "访问策略修改"},
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
                        { view: "text", name: "id",  hidden: true, value: item.id },
                        { view: "radio", name: "policy", label: "访问策略", icon: "caret-down", value:item.policy, validate:webix.rules.isNotEmpty,
                            options: [{id:"accept",value:"允许"},{id:"reject",value:"拒绝"}]
                        },
                        { view: "radio", name: "status", label: "状态", icon: "caret-down", value:item.status, validate:webix.rules.isNotEmpty,
                            options: [{id:"1",value:"启用"},{id:"0",value:"停用"}]
                        },
                        {view: "text", name: "priority", label: "优先级", value:item.priority, validate:webix.rules.isNumber },
                        {view: "text", name: "src", label: "来源IP地址/段", value:item.src},
                        {view: "text", name: "target", label: "目标IP地址/段", value:item.target},
                        {view: "text", name: "domain", label: "目标域名", value:item.domain },
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
                                webix.ajax().post('/admin/acl/update', params).then(function (result) {
                                    btn.enable();
                                    var resp = result.json();
                                    webix.message({ type: resp.msgtype, text: resp.msg, expire: 3000 });
                                    if (resp.code === 0) {
                                        if(callback)
                                            callback();
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

};

toughproxy.admin.acl.aclDelete = function (ids,callback) {
    webix.confirm({
        title: "操作确认",
        ok: "是", cancel: "否",
        text: "此操作不可逆，确认要删除吗？",
        width:360,
        callback: function (ev) {
            if (ev) {
                webix.ajax().get('/admin/acl/delete', {ids:ids}).then(function (result) {
                    var resp = result.json();
                    webix.message({type: resp.msgtype, text: resp.msg, expire: 1500});
                    if(callback)
                        callback()
                });
            }
        }
    });
};


