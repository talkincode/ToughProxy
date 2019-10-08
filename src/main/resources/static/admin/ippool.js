if (!window.toughproxy.admin.ippool)
    toughproxy.admin.ippool={};


toughproxy.admin.ippool.dataViewID = "toughproxy.admin.ippool.dataViewID";
toughproxy.admin.ippool.loadPage = function(session,keyword){
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
        $$(tableid).load('/admin/ippool/query?'+args.join("&"));
    };

    var cview = {
        id: "toughproxy.admin.ippool",
        css:"main-panel",padding:10,
        rows:[
            {
                id:toughproxy.admin.ippool.dataViewID,
                rows:[
                    {
                        view: "toolbar",
                        height:40,
                        css: "page-toolbar",
                        cols: [
                    //         {
                    //             view: "button", type: "form", width: 100, icon: "times", label: "设为长效",  click: function () {
                    //                 var rows = [];
                    //                 $$(tableid).eachRow(
                    //                     function (row) {
                    //                         var item = $$(tableid).getItem(row);
                    //                         if (item && item.state === 1) {
                    //                             rows.push(item.id)
                    //                         }
                    //                     }
                    //                 );
                    //                 if (rows.length === 0) {
                    //                     webix.message({ type: 'error', text: "请至少勾选一项", expire: 1500 });
                    //                 } else {
                    //                     toughproxy.admin.ippool.setLong(rows.join(","), function () {
                    //                         reloadData();
                    //                     });
                    //                 }
                    //             }
                    //         },
                    //         {
                    //             view: "button", type: "form", width: 100, icon: "times", label: "设为短效",  click: function () {
                    //                 var rows = [];
                    //                 $$(tableid).eachRow(
                    //                     function (row) {
                    //                         var item = $$(tableid).getItem(row);
                    //                         if (item && item.state === 1) {
                    //                             rows.push(item.id)
                    //                         }
                    //                     }
                    //                 );
                    //                 if (rows.length === 0) {
                    //                     webix.message({ type: 'error', text: "请至少勾选一项", expire: 1500 });
                    //                 } else {
                    //                     toughproxy.admin.ippool.setShort(rows.join(","), function () {
                    //                         reloadData();
                    //                     });
                    //                 }
                    //             }
                    //         },
                            {
                                view: "button", type: "base", width: 70, icon: "times", label: "重拨",  click: function () {
                                    var btn = this;
                                    var rows = [];
                                    $$(tableid).eachRow(
                                        function (row) {
                                            var item = $$(tableid).getItem(row);
                                            if (item && item.state === 1) {
                                                rows.push(item.name)
                                            }
                                        }
                                    );
                                    if (rows.length === 0) {
                                        webix.message({ type: 'error', text: "请至少勾选一项", expire: 1500 });
                                    } else {
                                        webix.message({type: "info", text: "开始重拨， 短时间内请勿重复操作", expire: 5000});
                                        btn.disable();
                                        toughproxy.admin.ippool.reDial(rows.join(","), function () {
                                            btn.enable();
                                            reloadData();
                                        });
                                    }
                                }
                            },
                            {
                                view: "button", type: "base", width: 100, icon: "times", label: "重拨本机",  click: function () {
                                    webix.message({type: "info", text: "开始重拨， 短时间内请勿重复操作", expire: 5000});
                                    var btn = this;
                                    btn.disable();
                                    toughproxy.admin.ippool.reDialAll(function () {
                                        btn.enable();
                                        reloadData();
                                    });
                                }
                            },
                            {
                                view: "button", type: "danger", width: 70, icon: "times", label: "删除",  click: function () {
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
                                        toughproxy.admin.ippool.delete(rows.join(","), function () {
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
                                                { view: "datepicker", name: "lastDia", label: "拨号时间不超过", width:240, labelWidth:100, stringResult: true,timepicker: true, format: "%Y-%m-%d" },
                                                {view: "text", css:"nborder-input2",  name: "poolname", label: "IP 池",  width:200},
                                                {view: "text", css:"nborder-input2",  name: "areaCode", label: "区域代码",  width:200},
                                                {view: "text", css:"nborder-input2",  name: "ipaddr", label: "IP 地址",  width:200},
                                                {
                                                    view: "richselect", css:"nborder-input2", name: "timeType", value:"1", label: "ip类型",width:200, icon: "caret-down",
                                                    options: [
                                                        { id: '0', value: "所有" },
                                                        { id: '1', value: "短效IP" },
                                                        { id: '2', value: "长效IP" }
                                                    ]
                                                },
                                                {
                                                    cols:[
                                                        {view: "button", label: "查询", type: "icon", icon: "search", borderless: true, width: 64, click: function () {
                                                                reloadData();
                                                            }},
                                                        {
                                                            view: "button", label: "重置", type: "icon", icon: "refresh", borderless: true, width: 64, click: function () {
                                                                $$(queryid).setValues({
                                                                    createTime: "",
                                                                    poolname: "",
                                                                    ipaddr: "",
                                                                    timeType: ""
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
                                    { id: "poolname", header: ["IP池"],adjust:true},
                                    { id: "name", header: ["拨号名"],adjust:true},
                                    { id: "areaCode", header: ["区域代码"],adjust:true},
                                    {
                                        id: "timeType", header: ["IP类型"], sort: "string",  adjust:true, template: function (obj) {
                                            if (obj.timeType === 1) {
                                                return "<span style='color:blue;'>短效IP</span>";
                                            } else if (obj.timeType === 2) {
                                                return "<span style='color:green;'>长效IP</span>";
                                            }

                                        }
                                    },
                                    { id: "ipaddr", header: ["IP地址"],adjust:true},
                                    { id: "diaTimes", header: ["拨号次数"],adjust:true},
                                    { id: "lastDia", header: ["最后拨号"],sort:"date",fillspace:true},
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
                                pager: "ippool_dataPager",
                                datafetch: 40,
                                loadahead: 15,
                                ready: function () {
                                    reloadData();
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
                                                $$("ippool_dataPager").define("size",parseInt(newv));
                                                $$(tableid).refresh();
                                                reloadData();
                                            }
                                        }
                                    },
                                    {
                                        id: "ippool_dataPager", view: 'pager', master: false, size: 20, group: 5,
                                        template: '{common.first()} {common.prev()} {common.pages()} {common.next()} {common.last()} total:#count#'
                                    },{},

                                ]
                            }
                        ]
                    },
                ]
            },
            {
                id: toughproxy.admin.ippool.detailFormID,
                hidden:true
            }
        ]
    };
    toughproxy.admin.methods.addTabView("toughproxy.admin.ippool","user-o","IP池管理", cview, true);
    webix.extend($$(tableid), webix.ProgressBar);
};


toughproxy.admin.ippool.reDial = function (names,callback) {
    webix.ajax().get('/admin/ippool/redial', {names:names}).then(function (result) {
        var resp = result.json();
        webix.message({type: resp.msgtype, text: resp.msg, expire: 5000});
        if(callback)
            callback()
    });
};

toughproxy.admin.ippool.reDialAll = function (callback) {
    webix.ajax().get('/admin/ippool/redialall', {}).then(function (result) {
        var resp = result.json();
        webix.message({type: resp.msgtype, text: resp.msg, expire: 5000});
        if(callback)
            callback()
    });
};

toughproxy.admin.ippool.setLong = function (ids,callback) {
    webix.ajax().get('/admin/ippool/setlong', {ids:ids}).then(function (result) {
        var resp = result.json();
        webix.message({type: resp.msgtype, text: resp.msg, expire: 1500});
        if(callback)
            callback()
    });
};

toughproxy.admin.ippool.setShort = function (ids,callback) {
    webix.ajax().get('/admin/ippool/setshort', {ids:ids}).then(function (result) {
        var resp = result.json();
        webix.message({type: resp.msgtype, text: resp.msg, expire: 1500});
        if(callback)
            callback()
    });
};

toughproxy.admin.ippool.delete = function (ids,callback) {
    webix.confirm({
        title: "操作确认",
        ok: "是", cancel: "否",
        text: "删除帐号会同时删除相关所有数据，此操作不可逆，确认要删除吗？",
        width:360,
        callback: function (ev) {
            if (ev) {
                webix.ajax().get('/admin/ippool/delete', {ids:ids}).then(function (result) {
                    var resp = result.json();
                    webix.message({type: resp.msgtype, text: resp.msg, expire: 1500});
                    if(callback)
                        callback()
                });
            }
        }
    });

};

