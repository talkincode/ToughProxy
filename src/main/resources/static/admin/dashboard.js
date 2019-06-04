if (!window.toughsocks.admin.dashboard)
    toughsocks.admin.dashboard={};

toughsocks.admin.dashboard.UiCpuUseChart = function (uid) {
    return {
        id: uid,
        view: "highcharts",
        credits: {enabled: false},
        chart: {
            type: 'solidgauge',
            events: {
                load: function () {
                    toughsocks.admin.dashboard.updateCpuUseChart(uid);
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

toughsocks.admin.dashboard.UiMemUseChart = function (uid) {
    return {
        id: uid,
        view: "highcharts",
        credits: {enabled: false},
        chart: {
            type: 'solidgauge',
            events: {
                load: function () {
                    toughsocks.admin.dashboard.updateMemUseChart(uid);
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


toughsocks.admin.dashboard.UiDiskUseChart = function (uid) {
    return {
        id: uid,
        view: "highcharts",
        credits: {enabled: false},
        chart: {
            type: 'solidgauge',
            events: {
                load: function () {
                    toughsocks.admin.dashboard.updateDiskUseChart(uid);
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

toughsocks.admin.dashboard.socksStatChart = function (session,uid) {
    return {
        id: uid,
        view: "highcharts",
        height:270,
        credits: {enabled: false},
        chart: {
            type: 'areaspline',
            events: {
                load: function () {
                    toughsocks.admin.dashboard.updateSocksStatChart(session, uid);
                }
            }
        },
        title: {
            text: "Socks 统计",
            style:{
                color:"#2f2f31", fontSize:"16px"
            }
        },
        legend: {
            align: 'center',verticalAlign: 'top',x: 0,y: 0
        },
        colors:['#00cca0', '#0080ff', '#ccae00','#cc0b2a'],
        xAxis: {type: 'datetime',tickInterval : 300*1000},
        yAxis: {title: {text: '数量'},
            labels: {formatter: function() {return this.value;}}
        },
        tooltip: {shared: true},
        plotOptions: {areaspline: {
                stacking: 'normal',
                marker: {enabled: false,symbol: 'circle',radius: 2,states: {hover: {enabled: true}}},
                fillOpacity: 0.2,
                series: {
                    pointPlacement: "on"
                }
            }},
        series: [{},{},{},{}]
    }
};


toughsocks.admin.dashboard.updateSocksStatChart = function (session,uid) {
    webix.ajax().get('/admin/socks/stat',{}).then(function (result) {
        var data = result.json();
        try {
            var AuthSuccessStat = {name:'认证成功',data:data.AuthSuccessStat};
            var AuthFailureStat = {name:'认证失败',data:data.AuthFailureStat};
            var NotSupportStat = {name:'不支持协议',data:data.NotSupportStat};
            var ConnectSuccessStat = {name:'连接成功',data:data.ConnectSuccessStat};
            var ConnectFailureStat = {name:'连接失败',data:data.ConnectFailureStat};
            var OtherErrStat = {name:'其他错误',data:data.OtherErrStat};
            $$(uid).parse([AuthSuccessStat,AuthFailureStat,NotSupportStat,ConnectSuccessStat,ConnectFailureStat,OtherErrStat]);
        } catch(e){
            console.log(e);
        }
    }).fail(function (xhr) {
        webix.message({type: 'error', text: "加载数据失败:"+xhr.statusText,expire:700});
    });
};



toughsocks.admin.dashboard.trafficStatChart = function (session,uid) {
    return {
        id: uid,
        view: "highcharts",
        height:270,
        credits: {enabled: false},
        chart: {
            type: 'areaspline',
            events: {
                load: function () {
                    toughsocks.admin.dashboard.updatetrafficStatChart(session, uid);
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
        colors:['#40cc6f', '#c4aaff', '#ccc300','#006bcc','#cc6d3a','#cc6587','#cc0018','#cc00b1','#6d00cc','#cc9a00'],
        xAxis: {type: 'datetime',tickInterval : 300*1000},
        yAxis: {title: {text: '流量'},
            labels: {formatter: function() {return bytesToSize(this.value);}}
        },
        tooltip: {shared: true},
        plotOptions: {areaspline: {
                stacking: 'normal',
                marker: {enabled: false,symbol: 'circle',radius: 2,states: {hover: {enabled: true}}},
                fillOpacity: 0.2,
                series: {
                    pointPlacement: "on"
                }
            }},
        series: [{},{}]
    }
};

toughsocks.admin.dashboard.updatetrafficStatChart = function (session,uid) {
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
toughsocks.admin.dashboard.updateCpuUseChart = function (uid) {
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
toughsocks.admin.dashboard.updateMemUseChart = function (uid) {
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

toughsocks.admin.dashboard.updateDiskUseChart = function (uid) {
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


toughsocks.admin.dashboard.basicInfo = function(session){
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

toughsocks.admin.dashboard.loadPage = function(session){
    var cpuchartUid = "toughsocks.admin.dashboard.cpuchart_viewid." + webix.uid();
    var memchartUid = "toughsocks.admin.dashboard.memchart_viewid." + webix.uid();
    var diskchartUid = "toughsocks.admin.dashboard.diskchart_viewid." + webix.uid();
    var socksstatchartid = "toughsocks.admin.dashboard.socksstatchart_view." + webix.uid();
    var trafficstatchartid = "toughsocks.admin.dashboard.trafficstatchart_viewid." + webix.uid();
    var uptimeid = "toughsocks.admin.dashboard.uptime.label";
    var cview = {
        id:"toughsocks.admin.dashboard",
        css:"main-panel",padding:10,
        rows:[
            {
                view:"scrollview",
                css:"dashboard",
                scroll:'y',
                body:{
                    type:"wide",
                    rows: [
                        toughsocks.admin.dashboard.basicInfo(session),
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
                                                toughsocks.admin.dashboard.updateCpuUseChart(cpuchartUid);
                                                toughsocks.admin.dashboard.updateMemUseChart(memchartUid);
                                                toughsocks.admin.dashboard.updateDiskUseChart(memchartUid);
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
                                        toughsocks.admin.dashboard.UiCpuUseChart(cpuchartUid),
                                        toughsocks.admin.dashboard.UiMemUseChart(memchartUid),
                                        toughsocks.admin.dashboard.UiDiskUseChart(diskchartUid)
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
                                                toughsocks.admin.dashboard.updateSocksStatChart(session,socksstatchartid);
                                                toughsocks.admin.dashboard.updatetrafficStatChart(session,trafficstatchartid);
                                            }
                                        }
                                    ]
                                },
                                {
                                    rows:[
                                        toughsocks.admin.dashboard.trafficStatChart(session,trafficstatchartid),
                                        toughsocks.admin.dashboard.socksStatChart(session,socksstatchartid)
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
    toughsocks.admin.methods.addTabView("toughsocks.admin.dashboard","dashboard","控制面板", cview, false);
    webix.ajax().get('/admin/dashboard/uptime',{}).then(function (result) {
        $$(uptimeid).define("template",result.text());
        $$(uptimeid).refresh();
    });

    //定时刷新消息统计
    if(toughsocks.admin.dashboard.msgRefershTimer){
        clearInterval(toughsocks.admin.dashboard.msgRefershTimer);
    }
    var reffunc = function(){
        toughsocks.admin.dashboard.updatetrafficStatChart(session, trafficstatchartid);
        toughsocks.admin.dashboard.updateSocksStatChart(session, socksstatchartid);
    };
    toughsocks.admin.dashboard.msgRefershTimer = setInterval(reffunc,60*1000)
};
