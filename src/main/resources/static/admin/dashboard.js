if (!window.toughproxy.admin.dashboard)
    toughproxy.admin.dashboard={};

toughproxy.admin.dashboard.UiCpuUseChart = function (uid) {
    return {
        id: uid,
        view: "highcharts",
        credits: {enabled: false},
        chart: {
            type: 'solidgauge',
            events: {
                load: function () {
                    toughproxy.admin.dashboard.updateCpuUseChart(uid);
                }
            }
        },
        title: null,
        pane: {
            center: ['50%', '85%'],
            size: '100%',
            startAngle: -90,
            endAngle: 90,
            background: {
                backgroundColor: (Highcharts.theme && Highcharts.theme.background2) || '#EEE',
                innerRadius: '60%',
                outerRadius: '100%',
                shape: 'arc'
            }
        },
        tooltip: {
            enabled: false
        },
        yAxis: {
            min: 0,
            max: 100,
            title: {
                y: -70,
                text: 'CPU使用率(%)'
            },
            stops: [
                [0.1, '#55BF3B'], // green
                [0.5, '#DDDF0D'], // yellow
                [0.9, '#DF5353'] // red
            ],
            lineWidth: 0,
            minorTickInterval: null,
            tickPixelInterval: 400,
            tickWidth: 0,
            labels: {
                y: 16
            }
        },
        plotOptions: {
            solidgauge: {
                dataLabels: {
                    y: 5,
                    borderWidth: 0,
                    useHTML: true
                }
            }
        },
        series: [{}]
    }
};

toughproxy.admin.dashboard.UiMemUseChart = function (uid) {
    return {
        id: uid,
        view: "highcharts",
        credits: {enabled: false},
        chart: {
            type: 'solidgauge',
            events: {
                load: function () {
                    toughproxy.admin.dashboard.updateMemUseChart(uid);
                }
            }
        },
        title: "内存使用率",
        pane: {
            center: ['50%', '85%'],
            size: '100%',
            startAngle: -90,
            endAngle: 90,
            background: {
                backgroundColor: (Highcharts.theme && Highcharts.theme.background2) || '#EEE',
                innerRadius: '60%',
                outerRadius: '100%',
                shape: 'arc'
            }
        },
        tooltip: {
            enabled: false
        },
        yAxis: {
            min: 0,
            max: 100,
            title: {
                 y: -70,
                text: '内存使用率(%)'
            },
            stops: [
                [0.1, '#55BF3B'], // green
                [0.5, '#DDDF0D'], // yellow
                [0.9, '#DF5353'] // red
            ],
            lineWidth: 0,
            minorTickInterval: null,
            tickPixelInterval: 400,
            tickWidth: 0,
            labels: {
                y: 16
            }
        },
        plotOptions: {
            solidgauge: {
                dataLabels: {
                    y: 5,
                    borderWidth: 0,
                    useHTML: true
                }
            }
        },
        series: [{}]
    }
};


toughproxy.admin.dashboard.UiDiskUseChart = function (uid) {
    return {
        id: uid,
        view: "highcharts",
        credits: {enabled: false},
        chart: {
            type: 'solidgauge',
            events: {
                load: function () {
                    toughproxy.admin.dashboard.updateDiskUseChart(uid);
                }
            }
        },
        title: "磁盘使用率",
        pane: {
            center: ['50%', '85%'],
            size: '100%',
            startAngle: -90,
            endAngle: 90,
            background: {
                backgroundColor: (Highcharts.theme && Highcharts.theme.background2) || '#EEE',
                innerRadius: '60%',
                outerRadius: '100%',
                shape: 'arc'
            }
        },
        tooltip: {
            enabled: false
        },
        yAxis: {
            min: 0,
            max: 100,
            title: {
                 y: -70,
                text: '磁盘使用率(%)'
            },
            stops: [
                [0.1, '#55BF3B'], // green
                [0.5, '#DDDF0D'], // yellow
                [0.9, '#DF5353'] // red
            ],
            lineWidth: 0,
            minorTickInterval: null,
            tickPixelInterval: 400,
            tickWidth: 0,
            labels: {
                y: 16
            }
        },
        plotOptions: {
            solidgauge: {
                dataLabels: {
                    y: 5,
                    borderWidth: 0,
                    useHTML: true
                }
            }
        },
        series: [{}]
    }
};

toughproxy.admin.dashboard.socksStatChart = function (session,uid) {
    return {
        id: uid,
        view: "highcharts",
        height:270,
        credits: {enabled: false},
        chart: {
            type: 'areaspline',
            events: {
                load: function () {
                    toughproxy.admin.dashboard.updateSocksStatChart(session, uid);
                }
            }
        },
        title: {
            text: "Proxy 统计",
            style:{
                color:"#2f2f31", fontSize:"16px"
            }
        },
        legend: {
            align: 'center',verticalAlign: 'top',x: 0,y: 0
        },
        colors:['#2fcc79', '#489f3e', '#b68c21','#cc5933','#94915e','#fbb079'],
        xAxis: {type: 'datetime',tickInterval : 300*1000},
        yAxis: {title: {text: '数量'},
            labels: {formatter: function() {return this.value;}}
        },
        tooltip: {
            shared: true,
            pointFormatter: function() {
                return "<span style='color: "+this.series.color+"'>"+ this.series.name +": <b> "+ this.y + "</b></span><br>";
            }
        },
        plotOptions: {areaspline: {
                stacking: 'normal',
                marker: {enabled: false,symbol: 'circle',radius: 2,states: {hover: {enabled: true}}},
                fillOpacity: 0.7,
                series: {
                    pointPlacement: "on"
                }
            }},
        series: [{},{},{},{}]
    }
};


toughproxy.admin.dashboard.updateSocksStatChart = function (session,uid) {
    webix.ajax().get('/admin/socks/stat',{}).then(function (result) {
        var data = result.json();
        try {
            var AuthSuccessStat = {name:'认证成功',data:data.AuthSuccessStat};
            var ConnectSuccessStat = {name:'连接成功',data:data.ConnectSuccessStat};
            var AuthFailureStat = {name:'认证失败',data:data.AuthFailureStat};
            var ConnectFailureStat = {name:'连接失败',data:data.ConnectFailureStat};
            var NotSupportStat = {name:'不支持协议',data:data.NotSupportStat};
            var OtherErrStat = {name:'其他错误',data:data.OtherErrStat};
            $$(uid).parse([AuthSuccessStat,ConnectSuccessStat,AuthFailureStat,ConnectFailureStat,NotSupportStat,OtherErrStat]);
        } catch(e){
            console.log(e);
        }
    }).fail(function (xhr) {
        webix.message({type: 'error', text: "加载数据失败:"+xhr.statusText,expire:700});
    });
};

toughproxy.admin.dashboard.aclStatChart = function (session,uid) {
    return {
        id: uid,
        view: "highcharts",
        height:270,
        credits: {enabled: false},
        chart: {
            type: 'areaspline',
            events: {
                load: function () {
                    toughproxy.admin.dashboard.updateAclStatChart(session, uid);
                }
            }
        },
        title: {
            text: "Acl 统计",
            style:{
                color:"#2f2f31", fontSize:"16px"
            }
        },
        legend: {
            align: 'center',verticalAlign: 'top',x: 0,y: 0
        },
        colors:['#2fcc79', '#9f5346', '#b68c21','#cc5933','#94915e','#fbb079'],
        xAxis: {type: 'datetime',tickInterval : 300*1000},
        yAxis: {title: {text: '数量'},
            labels: {formatter: function() {return this.value;}}
        },
        tooltip: {
            shared: true,
            pointFormatter: function() {
                return "<span style='color: "+this.series.color+"'>"+ this.series.name +": <b> "+ this.y + "</b></span><br>";
            }
        },
        plotOptions: {areaspline: {
                stacking: 'normal',
                marker: {enabled: false,symbol: 'circle',radius: 2,states: {hover: {enabled: true}}},
                fillOpacity: 0.7,
                series: {
                    pointPlacement: "on"
                }
            }},
        series: [{},{}]
    }
};


toughproxy.admin.dashboard.updateAclStatChart = function (session,uid) {
    webix.ajax().get('/admin/acl/stat',{}).then(function (result) {
        var data = result.json();
        try {
            var AclAcceptStat = {name:'允许连接',data:data.AclAcceptStat};
            var AclRejectStat = {name:'拒绝连接',data:data.AclRejectStat};
            $$(uid).parse([AclAcceptStat,AclRejectStat]);
        } catch(e){
            console.log(e);
        }
    }).fail(function (xhr) {
        webix.message({type: 'error', text: "加载数据失败:"+xhr.statusText,expire:700});
    });
};



toughproxy.admin.dashboard.trafficStatChart = function (session,uid) {
    return {
        id: uid,
        view: "highcharts",
        height:270,
        credits: {enabled: false},
        chart: {
            type: 'areaspline',
            events: {
                load: function () {
                    toughproxy.admin.dashboard.updatetrafficStatChart(session, uid);
                }
            }
        },
        title: {
            text: "流量统计",
            style:{
                color:"#2f2f31", fontSize:"16px"
            }
        },
        legend: {
            align: 'center',verticalAlign: 'top',x: 0,y: 0
        },
        colors:['#3df171', '#36abec'],
        xAxis: {type: 'datetime',tickInterval : 300*1000},
        yAxis: {title: {text: '流量'},
            labels: {formatter: function() {return bytesToSize(this.value);}}
        },
        tooltip: {
            shared: true,
            pointFormatter: function() {
                return "<span style='color: "+this.series.color+"'>"+ this.series.name +": <b> "+ bytesToSize(this.y) + "</b></span><br>";
            }
        },
        plotOptions: {areaspline: {
                stacking: 'normal',
                marker: {enabled: false,symbol: 'circle',radius: 2,states: {hover: {enabled: true}}},
                fillOpacity: 0.7,
                series: {
                    pointPlacement: "on"
                }
            }},
        series: [{},{}]
    }
};

toughproxy.admin.dashboard.updatetrafficStatChart = function (session,uid) {
    webix.ajax().get('/admin/traffic/stat',{}).then(function (result) {
        var data = result.json();
        try {
            var writeStat = {name:'下行流量',data:data.writeStat};
            var readStat = {name:'上行流量',data:data.readStat};
            $$(uid).parse([readStat,writeStat]);
        } catch(e){
            console.log(e);
        }
    }).fail(function (xhr) {
        webix.message({type: 'error', text: "加载数据失败:"+xhr.statusText,expire:700});
    });
};


/**
 * 更新CPU性能数据
 */
toughproxy.admin.dashboard.updateCpuUseChart = function (uid) {
    webix.ajax().get('/admin/dashboard/cpuuse',{}).then(function (result) {
        var resp = result.json();
        if(resp.code===1){
            webix.message({type:'error', text:resp.msg,expire:700});
        }else{
            try {
               $$(uid).parse([{name:"使用率",data:[resp.data]}]);
            } catch(e){
                console.log(e);
            }
        }
    }).fail(function (xhr) {
        webix.message({type: 'error', text: "加载数据失败:"+xhr.statusText,expire:700});
    });
};
/**
 * 更新内存百分比数据
 */
toughproxy.admin.dashboard.updateMemUseChart = function (uid) {
    webix.ajax().get('/admin/dashboard/memuse',{}).then(function (result) {
        var resp = result.json();
        if(resp.code===1){
            webix.message({type:'error', text:resp.msg,expire:700});
        }else{
            try {
               $$(uid).parse([{name:"使用率",data:[resp.data]}]);
            } catch(e){
                console.log(e);
            }
        }
    }).fail(function (xhr) {
        webix.message({type: 'error', text: "加载数据失败:"+xhr.statusText,expire:700});
    });
};

toughproxy.admin.dashboard.updateDiskUseChart = function (uid) {
    webix.ajax().get('/admin/dashboard/diskuse',{}).then(function (result) {
        var resp = result.json();
        if(resp.code===1){
            webix.message({type:'error', text:resp.msg,expire:700});
        }else{
            try {
               $$(uid).parse([{name:"使用率",data:[resp.data]}]);
            } catch(e){
                console.log(e);
            }
        }
    }).fail(function (xhr) {
        webix.message({type: 'error', text: "加载数据失败:"+xhr.statusText,expire:700});
    });
};


toughproxy.admin.dashboard.basicInfo = function(session){
    return {
        // view:"portlet",
        icon:false,
        body:{
            borderless:true,
            css:"panel-box",
            rows:[
                {
                    view: "toolbar",
                    cols: [
                        { view: "label", label: " <i class='fa fa-info-circle'></i> 系统信息", css:"dash-title", inputWidth: 150, align: "left" },
                        {},
                    ]
                },
                {
                    padding: 10,
                    cols: [
                        { view: "text", name: "oprname", label: "当前操作员", css: "nborder-input", value: session.username, readonly: true },
                        { view: "text", name: "logintime", label: "登录时间", css: "nborder-input", value: session.lastLogin, readonly: true },

                    ]
                },
                {
                    hidden:session.level!=='super',
                    padding: 10,
                    cols: [
                        { view: "text", name: "ipaddr", label: "登录IP地址", css: "nborder-input", value: session.ipaddr, readonly: true },
                        { view: "text", name: "version", label: "系统版本", css: "nborder-input", value: session.version, readonly: true },
                    ]
                }
            ]
        }
    };
};

toughproxy.admin.dashboard.loadPage = function(session){
    var cpuchartUid = "toughproxy.admin.dashboard.cpuchart_viewid." + webix.uid();
    var memchartUid = "toughproxy.admin.dashboard.memchart_viewid." + webix.uid();
    var diskchartUid = "toughproxy.admin.dashboard.diskchart_viewid." + webix.uid();
    var socksstatchartid = "toughproxy.admin.dashboard.socksstatchart_view." + webix.uid();
    var aclstatchartid = "toughproxy.admin.dashboard.aclstatchart_view." + webix.uid();
    var trafficstatchartid = "toughproxy.admin.dashboard.trafficstatchart_viewid." + webix.uid();
    var uptimeid = "toughproxy.admin.dashboard.uptime.label";
    var cview = {
        id:"toughproxy.admin.dashboard",
        css:"main-panel",padding:10,
        rows:[
            {
                view:"scrollview",
                css:"dashboard",
                scroll:'y',
                body:{
                    type:"wide",
                    rows: [
                        toughproxy.admin.dashboard.basicInfo(session),
                        // {height:10},
                        {
                            // view: "portlet",
                            icon: false,
                            borderless:true,
                            css:"panel-box",
                            rows:[
                                {
                                    view: "toolbar",
                                    height:40,
                                    cols: [
                                        {id:uptimeid,view:"label", height:33,paddingX:20, borderless: true, label:"", css:"dash-title"},
                                        {},
                                        {
                                            view: "button",
                                            label: "刷新",
                                            type: "icon",
                                            icon: "refresh",
                                            // borderless: true,
                                            width: 60,
                                            click: function () {
                                                toughproxy.admin.dashboard.updateCpuUseChart(cpuchartUid);
                                                toughproxy.admin.dashboard.updateMemUseChart(memchartUid);
                                                toughproxy.admin.dashboard.updateDiskUseChart(memchartUid);
                                                webix.ajax().get('/admin/dashboard/uptime',{}).then(function (result) {
                                                    $$(uptimeid).define("template",  result.text());
                                                    $$(uptimeid).refresh();
                                                });
                                            }
                                        }
                                    ]
                                },
                                {
                                    height:180,
                                    cols:[
                                        toughproxy.admin.dashboard.UiCpuUseChart(cpuchartUid),
                                        toughproxy.admin.dashboard.UiMemUseChart(memchartUid),
                                        toughproxy.admin.dashboard.UiDiskUseChart(diskchartUid)
                                    ]
                                }
                            ]
                        },
                        // {height:10},
                        {
                            // view: "portlet",
                            icon: false,
                            borderless:true,
                            css:"panel-box",
                            rows:[
                                {
                                    view: "toolbar",
                                    cols: [
                                        {view:"label", label:" <i class='fa fa-line-chart'></i> 60分钟消息统计 刷新间隔：60妙",css:"dash-title", width: 240},
                                        {},
                                        {
                                            view: "button",
                                            label: "刷新",
                                            type: "icon",
                                            icon: "refresh",
                                            borderless: true,
                                            width:60,
                                            click: function () {
                                                toughproxy.admin.dashboard.updateSocksStatChart(session,socksstatchartid);
                                                toughproxy.admin.dashboard.updateAclStatChart(session,aclstatchartid);
                                                toughproxy.admin.dashboard.updatetrafficStatChart(session,trafficstatchartid);
                                            }
                                        }
                                    ]
                                },
                                {
                                    rows:[
                                        toughproxy.admin.dashboard.trafficStatChart(session,trafficstatchartid),
                                        toughproxy.admin.dashboard.aclStatChart(session,aclstatchartid),
                                        toughproxy.admin.dashboard.socksStatChart(session,socksstatchartid)
                                    ]
                                }
                            ]
                        },
                        // {height:10},
                    ]
                }
            }
        ]
    };
    toughproxy.admin.methods.addTabView("toughproxy.admin.dashboard","dashboard","控制面板", cview, false);
    webix.ajax().get('/admin/dashboard/uptime',{}).then(function (result) {
        $$(uptimeid).define("template",result.text());
        $$(uptimeid).refresh();
    });

    //定时刷新消息统计
    if(toughproxy.admin.dashboard.msgRefershTimer){
        clearInterval(toughproxy.admin.dashboard.msgRefershTimer);
    }
    var reffunc = function(){
        toughproxy.admin.dashboard.updatetrafficStatChart(session, trafficstatchartid);
        toughproxy.admin.dashboard.updateSocksStatChart(session, socksstatchartid);
        toughproxy.admin.dashboard.updateAclStatChart(session, aclstatchartid);
    };
    toughproxy.admin.dashboard.msgRefershTimer = setInterval(reffunc,60*1000)
};
