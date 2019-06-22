if (!window.toughsocks.admin.group)
    toughsocks.admin.group={};


toughsocks.admin.group.dataViewID = "toughsocks.admin.group.dataViewID";
toughsocks.admin.group.loadPage = function(session,keyword){
    var tableid = webix.uid();
    var queryid = webix.uid();
    toughsocks.admin.group.reloadData = function(){
        $$(tableid).refresh();
        $$(tableid).clearAll();
        var params = $$(queryid).getValues();
        var args = [];
        for(var k in params){
            args.push(k+"="+params[k]);
        }
        $$(tableid).load('/admin/group/query?'+args.join("&"));
    };

    var reloadData = toughsocks.admin.group.reloadData;

    var cview = {
        id: "toughsocks.admin.group",
        css:"main-panel",padding:10,
        rows:[
            {
                id:toughsocks.admin.group.dataViewID,
                rows:[
                    {
                        view: "toolbar",
                        height:40,
                        css: "page-toolbar",
                        cols: [
                            {
                                view: "button", type: "form", width: 70, icon: "plus", label: "创建组", click: function () {
                                    toughsocks.admin.group.newGroupForm(session);
                                }
                            },
                            {
                                view: "button", type: "danger", width: 45, icon: "times", label: "删除",  click: function () {
                                    var rows = [];
                                    $$(tableid).eachRow(
                                        function (row) {
                                            var item = $$(tableid).getItem(row);
                                            if (item && item.state === 1) {
                                                console.log(item);
                                                rows.push(item.id)
                                            }
                                        }
                                    );
                                    if (rows.length === 0) {
                                        webix.message({ type: 'error', text: "请至少勾选一项", expire: 1500 });
                                    } else {
                                        toughsocks.admin.group.grpDelete(rows.join(","), function () {
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
                                                {view: "text", css:"nborder-input2",  name: "name", label: "名称", placeholder: "用户组名称", width:240},
                                                {
                                                    cols:[
                                                        {view: "button", label: "查询", type: "icon", icon: "search", borderless: true, width: 64, click: function () {
                                                                reloadData();
                                                            }},
                                                        {
                                                            view: "button", label: "重置", type: "icon", icon: "refresh", borderless: true, width: 64, click: function () {
                                                                $$(queryid).setValues({
                                                                    name: "",
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
                                    { id: "name", header: ["名称"],adjust:true},
                                    {
                                        id: "status", header: ["状态"], sort: "string",  adjust:true, template: function (obj) {
                                            if (obj.status === 0) {
                                                return "<span style='color:orange;'>停用</span>";
                                            } else if (obj.status === 1) {
                                                return "<span style='color:green;'>正常</span>";
                                            }
                                        }
                                    },
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
                                url: "/admin/group/query",
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
                                        toughsocks.admin.group.grpUpdate(session, this.getItem(id), function () {
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
                id: toughsocks.admin.group.detailFormID,
                hidden:true
            }
        ]
    };
    toughsocks.admin.methods.addTabView("toughsocks.admin.group","user-o","访问控制", cview, true);
    webix.extend($$(tableid), webix.ProgressBar);
};


/**
 * 新用户报装
 * @param session
 * @constructor
 */
toughsocks.admin.group.newGroupForm = function(session){
    var winid = "toughsocks.admin.group.newGroupForm";
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
                {view: "icon", icon: "users", css: "alter"},
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
                        { view: "text", name: "name", label: "名称",validate:webix.rules.isNotEmpty},
                        { view: "text", name: "maxSession", label: "最大连接数",  validate:webix.rules.isNumber},
                        { view: "text", name: "upLimit", label: "上行限速",  validate:webix.rules.isNumber },
                        { view: "text", name: "downLimit", label: "下行限速",   validate:webix.rules.isNumber},
                        { view:"textarea", name:"remark", height:140, label:"描述"},
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
                                webix.ajax().post('/admin/group/create', params).then(function (result) {
                                    btn.enable();
                                    var resp = result.json();
                                    webix.message({ type: resp.msgtype, text: resp.msg, expire: 3000 });
                                    if (resp.code === 0) {
                                        $$(winid).close();
                                        toughsocks.admin.group.reloadData();

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




toughsocks.admin.group.grpUpdate = function(session,item,callback){
    var updateWinid = "toughsocks.admin.group.grpUpdate";
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
                {view: "icon", icon: "users", css: "alter"},
                {view: "label", label: "用户组修改"},
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
                        { view: "radio", name: "status", label: "状态", value: item.status, options: [{ id: '1', value: "正常" }, { id: '0', value: "停用" }] },
                        { view: "text", name: "name", label: "名称", value: item.name, validate:webix.rules.isNotEmpty},
                        { view: "text", name: "maxSession", label: "最大连接数", value:item.maxSession, validate:webix.rules.isNumber},
                        { view: "text", name: "upLimit", label: "上行限速",  value:item.upLimit,validate:webix.rules.isNumber },
                        { view: "text", name: "downLimit", label: "下行限速",   value:item.downLimit,validate:webix.rules.isNumber},
                        { view:"textarea", name:"remark", height:140, label:"描述", value:item.remark},
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
                                webix.ajax().post('/admin/group/update', params).then(function (result) {
                                    btn.enable();
                                    var resp = result.json();
                                    webix.message({ type: resp.msgtype, text: resp.msg, expire: 3000 });
                                    if (resp.code === 0) {
                                        toughsocks.admin.group.reloadData();
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

toughsocks.admin.group.grpDelete = function (ids,callback) {
    webix.confirm({
        title: "操作确认",
        ok: "是", cancel: "否",
        text: "此操作不可逆，确认要删除吗？",
        width:360,
        callback: function (ev) {
            if (ev) {
                webix.ajax().get('/admin/group/delete', {ids:ids}).then(function (result) {
                    var resp = result.json();
                    webix.message({type: resp.msgtype, text: resp.msg, expire: 1500});
                    if(callback)
                        callback()
                });
            }
        }
    });
};


