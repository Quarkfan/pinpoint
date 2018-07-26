(function() {
    'use strict';
    var oHelp = {
        configuration: {
            general: {
                warning: "(用户信息暂时缓存在浏览器端，服务端将在后续版本中支持。)",
                empty: "关注列表为空"
            },
            alarmRules: {
                mainStyle: "",
                title: "报警规则类型",
                desc: "支持以下报警规则类型",
                category: [{
                    title: "[类型]",
                    items: [{
                        name: "慢请求阈值",
                        desc: "当应用程序发送的慢请求数量超过给定的阈值时，发出警报."
                    },{
                        name: "慢请求比例",
                        desc: "当应用程序发送的慢请求比例超过给定的阈值时，发出警报."
                    },{
                        name: "ERROR请求阈值",
                        desc: "当应用程序发送的错误请求数量超过给定的阈值时，发出警报."
                    },{
                        name: "ERROR请求比例",
                        desc: "当应用程序发送的错误请求比例超过给定的阈值时，发出警报."
                    },{
                        name: "总数量阈值",
                        desc: "当应用程序发送的所有请求的数量超过配置的阈值时，发送警报."
                    },{
                        name: "慢响应阈值",
                        desc: "当应用程序返回的慢响应数量超过给定的阈值时，发出警报."
                    },{
                        name: "慢响应比例",
                        desc: "当应用程序返回的慢响应比例超过给定的阈值时，发出警报."
                    },{
                        name: "ERROR响应阈值",
                        desc: "当应用程序返回的错误响应数量超过给定的阈值时，发出警报."
                    },{
                        name: "ERROR响应比例",
                        desc: "当应用程序返回的错误响应比例超过给定的阈值时，发出警报."
                    },{
                        name: "响应总数阈值",
                        desc: "当应用程序返回的所有响应总数超过给定的阈值时，发出警报."
                    },{
                        name: "堆内存使用比例",
                        desc: "当应用程序堆内存使用比例超过给定阈值时，发出警报."
                    },{
                        name: "JVM CPU使用比例",
                        desc: "当应用程序JVM CPU使用比例超过给定阈值时，发出警报."
                    },{
                        name: "数据源连接使用比例",
                        desc: "当应用程序数据源连接使用的比例超过给定的阈值时，发出警报"
                    }, {
                        name: "死锁报警",
                        desc: "当应用程序发生死锁时，发出警报."
                    }]
                }]
            },
            installation: {
                desc: "* 检查应用程序或者Agent的ID是否发生重复.",
                lengthGuide: "您可以输入 {{MAX_CHAR}} 个字符."
            }
        },
        navbar : {
            searchPeriod : {
                guideDateMax: "检索的时间区间不能超过 {{day}} 天.",
                guideDateOrder: "日期或时间设置错误"
            },
            applicationSelector: {
                mainStyle: "",
                title: "应用列表",
                desc: "显示所有安装了监控的应用.",
                category : [{
                    title: "[示例]",
                    items: [{
                        name: "图标",
                        desc: "应用类型"
                    }, {
                        name: "文本",
                        desc: "应用名称. 探针端参数 <code>-Dpinpoint.applicationName</code> 所配置的内容。."
                    }]
                }]
            },
            depth : {
                mainStyle: "",
                title: '<img src="images/inbound.png" width="22px" height="22px" style="margin-top:-4px;"> Inbound 和 <img src="images/outbound.png" width="22px" height="22px" style="margin-top:-4px"> Outbound',
                desc: "服务器拓扑的检索深度.",
                category : [{
                    title: "[示例]",
                    items: [{
                        name: "Inbound",
                        desc: "选定节点的请求的渲染深度。"
                    }, {
                        name: "Outbound",
                        desc: "选定节点的响应的渲染深度。"
                    }]
                }]
            },
            bidirectional : {
                mainStyle: "",
                title: '<img src="images/bidirect_on.png" width="22px" height="22px" style="margin-top:-4px;"> Bidirectional Search',
                desc: "服务器拓扑图的搜索方法",
                category : [{
                    title: "[示例]",
                    items: [{
                        name: "Bidirectional",
                        desc: "Renders inbound/outbound nodes for each and every node (within limit) even if they are not directly related to the selected node.<br>Note that checking this option may lead to overly complex server maps."
                    }]
                }]
            },
            periodSelector: {
                mainStyle: "",
                title: "Period Selector",
                desc: "Selects the time range for search data.",
                category: [{
                    title: "[Usage]",
                    items: [{
                        name: "<button type='button' class='btn btn-success btn-xs'><span class='glyphicon glyphicon-th-list'></span></button>",
                        desc: "Query for data traced during the most recent selected time-period.<br/>Auto-refresh is supported for 5m, 10m, 3h time-period."
                    },{
                        name: "<button type='button' class='btn btn-success btn-xs'><span class='glyphicon glyphicon-calendar'></span></button>",
                        desc: "Query for traced data between two selected times with the maximum of 48 hours."
                    }]
                }]
            }
        },
        servermap : {
            "default": {
                mainStyle: "width:560px;",
                title: "Server Map",
                desc: "Displays a topological view of the distributed server map.",
                category: [{
                    title: "[Node]",
                    list: [
                        "Each node is a logical unit of application.",
                        "The value on the top-right corner represents the number of server instances assigned to that application. (Not shown when there is only one such instance)",
                        "An alarm icon is displayed on the top-left corner if an error/exception is detected in one of the server instances.",
                        "Clicking a node shows information on all incoming transactions on the right-hand side of the screen."
                    ]
                },{
                    title: "[Arrow]",
                    list: [
                        "Each arrow represents a transaction flow.",
                        "The number shows the transaction count. Displayed in red for error counts that exceeds the threshold.",
                        "<span class='glyphicon glyphicon-filter' style='color:green;'></span> is shown when a filter is applied.",
                        "Clicking an arrow shows information on all transactions passing through the selected section on the right-hand side of the screen."
                    ]
                },{
                    title: "[Usage of the Node]",
                    list: [
                        "When the node is selected, the transaction information flowing into the application is displayed on the right side of the screen."
                    ]
                },{
                    title: "[Usage of the Arrow]",
                    list: [
                        "Select the arrow to show the transaction information that passes through the selected section on the right side of the screen.",
                        "The Filter in the Context menu shows only the transactions that pass through the selected section.",
                        "Filter wizard allows you to configure more detailed filters.",
                        "When the filter is applied, <span class = 'glyphicon glyphicon-filter' style = 'color: green;'> </span> icon will be displayed on the arrow."
                    ]
                },{
                    title: "[Applying Filter]",
                    list: [
                        "Right-clicking on an arrow displays the filter menu.",
                        "'Filter' filters the server map to show transactions that have passed through the selected section.",
                        "'Filter Wizard' allows additional filter configurations."
                    ]
                },{
                    title: "[Chart Configuration]",
                    list: [
                        "Right-clicking on any blank area displays a chart configuration menu.",
                        "Node Setting / Merge Unknown : Groups all applications without agent and displays it as a single node.",
                        "Double-clicking on any blank area resets the zoom level of the server map."
                    ]
                }]
            }
        },
        scatter : {
            "default": {
                mainStyle: "",
                title: "Response Time Scatter Chart",
                desc: "",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "<span class='glyphicon glyphicon-stop' style='color:#2ca02c'></span>",
                        desc: "Successful Transaction"
                    },{
                        name: "<span class='glyphicon glyphicon-stop' style='color:#f53034'></span>",
                        desc: "Failed Transaction"
                    },{
                        name: "X-axis",
                        desc: "Transaction Timestamp (hh:mm)"
                    },{
                        name: "Y-axis",
                        desc: "Response Time (ms)"
                    }]
                },{
                    title: "[Usage]",
                    image: "<img src='images/help/scatter_01.png' width='200px' height='125px'>",
                    items: [{
                        name: "<span class='glyphicon glyphicon-plus'></span>",
                        desc: "Drag on the scatter chart to show detailed information on selected transactions."
                    },{
                        name: "<span class='glyphicon glyphicon-cog'></span>",
                        desc: "Set the min/max value of the Y-axis (Response Time)."
                    },{
                        name: "<span class='glyphicon glyphicon-download-alt'></span>",
                        desc: "Download the chart as an image file."
                    },{
                        name: "<span class='glyphicon glyphicon-fullscreen'></span>",
                        desc: "Open the chart in a new window."
                    }]
                }]
            }
        },
        realtime: {
            "default": {
                mainStyle: "",
                title: "Realtime Active Thread Chart",
                desc: "Shows the Active Thread count of each agent in realtime.",
                category: [{
                    title: "[Error Messages]",
                    items: [{
                        name: "UNSUPPORTED VERSION",
                        desc: "Agent version too old. (Please upgrade the agent to 1.5.0+)",
                        nameStyle: "width:120px;border-bottom:1px solid gray",
                        descStyle: "border-bottom:1px solid gray"
                    },{
                        name: "CLUSTER OPTION NOTSET",
                        desc: "Option disabled by agent. (Please set profiler.pinpoint.activethread to true in profiler.config)",
                        nameStyle: "width:120px;border-bottom:1px solid gray",
                        descStyle: "border-bottom:1px solid gray"
                    },{
                        name: "TIMEOUT",
                        desc: "Agent connection timed out receiving active thread count. Please contact the administrator if problem persists.",
                        nameStyle: "width:120px;border-bottom:1px solid gray",
                        descStyle: "border-bottom:1px solid gray"
                    },{
                        name: "NOT FOUND",
                        desc: "Agent not found. (If you get this message while the agent is running, please set profiler.tcpdatasender.command.accept.enable to true in profiler.config)",
                        nameStyle: "width:120px;border-bottom:1px solid gray",
                        descStyle: "border-bottom:1px solid gray"
                    },{
                        name: "CLUSTER CHANNEL CLOSED",
                        desc: "Agent session expired.",
                        nameStyle: "width:120px;border-bottom:1px solid gray",
                        descStyle: "border-bottom:1px solid gray"
                    },{
                        name: "PINPOINT INTERNAL ERROR",
                        desc: "Pinpoint internal error. Please contact the administrator.",
                        nameStyle: "width:120px;border-bottom:1px solid gray",
                        descStyle: "border-bottom:1px solid gray"
                    },{
                        name: "No Active Thread",
                        desc: "The agent has no threads that are currently active.",
                        nameStyle: "width:120px;border-bottom:1px solid gray",
                        descStyle: "border-bottom:1px solid gray"
                    },{
                        name: "No Response",
                        desc: "No response from Pinpoint Web. Please contact the administrator.",
                        nameStyle: "width:120px;border-bottom:1px solid gray",
                        descStyle: "border-bottom:1px solid gray"
                    }]
                }]
            }
        },
        nodeInfoDetails: {
            responseSummary: {
                mainStyle: "",
                title: "Response Summary Chart",
                desc: "",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "X-Axis",
                        desc: "Response Time"
                    },{
                        name: "Y-Axis",
                        desc: "Transaction Count"
                    },{
                        name: "<spanstyle='color:#2ca02c'>1s</span>",
                        desc: "No. of Successful transactions (less than 1 second)"
                    },{
                        name: "<span style='color:#3c81fa'>3s</span>",
                        desc: "No. of Successful transactions (1 ~ 3 seconds)"
                    },{
                        name: "<span style='color:#f8c731'>5s</span>",
                        desc: "No. of Successful transactions (3 ~ 5 seconds)"
                    },{
                        name: "<span style='color:#f69124'>Slow</span>",
                        desc: "No. of Successful transactions (greater than 5 seconds)"
                    },{
                        name: "<span style='color:#f53034'>Error</span>",
                        desc: "No. of Failed transactions regardless of response time"
                    }]
                }]
            },
            load: {
                mainStyle: "",
                title: "Load Chart",
                desc: "",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "X-Axis",
                        desc: "Transaction Timestamp (in minutes)"
                    },{
                        name: "Y-Axis",
                        desc: "Transaction Count"
                    },{
                        name: "<spanstyle='color:#2ca02c'>1s</span>",
                        desc: "No. of Successful transactions (less than 1 second)"
                    },{
                        name: "<span style='color:#3c81fa'>3s</span>",
                        desc: "No. of Successful transactions (1 ~ 3 seconds)"
                    },{
                        name: "<span style='color:#f8c731'>5s</span>",
                        desc: "No. of Successful transactions (3 ~ 5 seconds)"
                    },{
                        name: "<span style='color:#f69124'>Slow</span>",
                        desc: "No. of Successful transactions (greater than 5 seconds)"
                    },{
                        name: "<span style='color:#f53034'>Error</span>",
                        desc: "No. of Failed transactions regardless of response time"
                    }]
                },{
                    title: "[Usage]",
                    list: [
                        "Clicking on a legend item shows/hides all transactions within the selected group.",
                        "Dragging on the chart zooms in to the dragged area."
                    ]
                }]
            },
            nodeServers: {
                mainStyle: "width:400px;",
                title: "Server Information",
                desc: "List of physical servers and their server instances.",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "<span class='glyphicon glyphicon-home'></span>",
                        desc: "Hostname of the physical server"
                    },{
                        name: "<span class='glyphicon glyphicon-hdd'></span>",
                        desc: "AgentId of the Pinpoint agent installed on the server instance running on the physical server"
                    }]
                },{
                    title: "[Usage]",
                    items: [{
                        name: "<button type='button' class='btn btn-default btn-xs'>Inspector</button>",
                        desc: "Open a new window with detailed information on the WAS with Pinpoint installed."
                    },{
                        name: "<span class='glyphicon glyphicon-record' style='color:#3B99FC'></span>",
                        desc: "Display statistics on transactions carried out by the server instance."
                    },{
                        name: "<span class='glyphicon glyphicon-hdd' style='color:red'></span>",
                        desc: "Display statistics on transactions (with error) carried out by the server instance."
                    }]
                }]
            },
            unknownList: {
                mainStyle: "",
                title: "UnknownList",
                desc: "From the chart's top-right icon",
                category: [{
                    title: "[Usage]",
                    items: [{
                        name: "1st",
                        desc: "Toggle between Response Summary Chart / Load Chart"
                    },{
                        name: "2nd",
                        desc: "Show Node Details"
                    }]
                }]
            },
            searchAndOrder: {
                mainStyle: "",
                title: "Search and Filter",
                desc: "You can search with server name and Counts.",
                category: [{
                    title: "[Usage]",
                    items: [{
                        name: "Name",
                        desc: "Sort names in ascending / descending order."
                    },{
                        name: "Count",
                        desc: "Sort counts in ascending / descending order."
                    }]
                }]
            }
        },
        linkInfoDetails: {
            responseSummary: {
                mainStyle: "",
                title: "Response Summary Chart",
                desc: "",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "X-Axis",
                        desc: "Response Time"
                    },{
                        name: "Y-Axis",
                        desc: "Transaction Count"
                    },{
                        name: "<spanstyle='color:#2ca02c'>1s</span>",
                        desc: "No. of Successful transactions (less than 1 second)"
                    },{
                        name: "<span style='color:#3c81fa'>3s</span>",
                        desc: "No. of Successful transactions (1 ~ 3 seconds)"
                    },{
                        name: "<span style='color:#f8c731'>5s</span>",
                        desc: "No. of Successful transactions (3 ~ 5 seconds)"
                    },{
                        name: "<span style='color:#f69124'>Slow</span>",
                        desc: "No. of Successful transactions (greater than 5 seconds)"
                    },{
                        name: "<span style='color:#f53034'>Error</span>",
                        desc: "No. of Failed transactions regardless of response time"
                    }]
                },{
                    title: "[Usage]",
                    list: ["Click on the bar to query for transactions within the selected response time."]
                }]
            },
            load: {
                mainStyle: "",
                title: "Load Chart",
                desc: "",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "X-Axis",
                        desc: "Transaction Timestamp (in minutes)"
                    },{
                        name: "Y-Axis",
                        desc: "Transaction Count"
                    },{
                        name: "<spanstyle='color:#2ca02c'>1s</span>",
                        desc: "No. of Successful transactions (less than 1 second)"
                    },{
                        name: "<span style='color:#3c81fa'>3s</span>",
                        desc: "No. of Successful transactions (1 ~ 3 seconds)"
                    },{
                        name: "<span style='color:#f8c731'>5s</span>",
                        desc: "No. of Successful transactions (3 ~ 5 seconds)"
                    },{
                        name: "<span style='color:#f69124'>Slow</span>",
                        desc: "No. of Successful transactions (greater than 5 seconds)"
                    },{
                        name: "<span style='color:#f53034'>Error</span>",
                        desc: "No. of Failed transactions regardless of response time"
                    }]
                },{
                    title: "[Usage]",
                    list: [
                        "Clicking on a legend item shows/hides all transactions within the selected group.",
                        "Dragging on the chart zooms in to the dragged area."
                    ]
                }]
            },
            linkServers: {
                mainStyle: "width:350px;",
                title: "Server Information",
                desc: "List of physical servers and their server instances.",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "<span class='glyphicon glyphicon-home'></span>",
                        desc: "Hostname of the physical server"
                    },{
                        name: "<span class='glyphicon glyphicon-hdd'></span>",
                        desc: "AgentId of the Pinpoint agent installed on the server instance running on the physical server"
                    }]
                },{
                    title: "[Usage]",
                    items: [{
                        name: "<button type='button' class='btn btn-default btn-xs'>Inspector</button>",
                        desc: "Open a new window with detailed information on the WAS where Pinpoint is installed."
                    },{
                        name: "<button type='button' class='btn btn-default btn-xs'><span class='glyphicon glyphicon-plus'></span></button>",
                        desc: "Display statistics on transactions carried out by the server instance."
                    },{
                        name: "<button type='button' class='btn btn-danger btn-xs'><span class='glyphicon glyphicon-plus'></span></button>",
                        desc: "Display statistics on transactions (with error) carried out by the server instance."
                    }]
                }]
            },
            unknownList: {
                mainStyle: "",
                title: "UnknownList",
                desc: "From the chart's top-right icon,",
                category: [{
                    title: "[Usage]",
                    items: [{
                        name: "1st",
                        desc: "Toggle between Response Summary Chart"
                    },{
                        name: "2dn",
                        desc: "Show Node Details"
                    }]
                }]
            },
            searchAndOrder: {
                mainStyle: "",
                title: "Search and Filter",
                desc: "You can search with server name and Counts.",
                category: [{
                    title: "[Usage]",
                    items: [{
                        name: "Name",
                        desc: "Sort names in ascending / descending order."
                    },{
                        name: "Count",
                        desc: "Sort counts in ascending / descending order."
                    }]
                }]
            }
        },
        inspector: {
            noDataCollected: "No data collected",
            list: {
                mainStyle: "",
                title: "Agent list",
                desc: "List of agents registered under the current Application Name",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "<span class='glyphicon glyphicon-home'></span>",
                        desc: "Hostname of the agent's machine"
                    },{
                        name: "<span class='glyphicon glyphicon-hdd'></span>",
                        desc: "Agent-id of the installed agent"
                    },{
                        name: "<span class='glyphicon glyphicon-ok-sign' style='color:#40E340'></span>",
                        desc: "Agent was running at the time of query"
                    },{
                        name: "<span class='glyphicon glyphicon-minus-sign' style='color:#F00'></span>",
                        desc: "Agent was shutdown at the time of query"
                    },{
                        name: "<span class='glyphicon glyphicon-remove-sign' style='color:#AAA'></span>",
                        desc: "Agent was disconnected at the time of query"
                    },{
                        name: "<span class='glyphicon glyphicon-question-sign' style='color:#AAA'></span>",
                        desc: "Agent status was unknown at the time of query"
                    }]
                }]
            },
            heap: {
                mainStyle: "",
                title: "Heap",
                desc: "JVM's heap information and full garbage collection time required",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "Max",
                        desc: "Maximum heap size"
                    },{
                        name: "Used",
                        desc: "Heap size currently in use"
                    },{
                        name: "FGC",
                        desc: "Time required for full garbage collection (number of FGCs in parenthesis if it occurred more than once)"
                    }]
                }]
            },
            permGen: {
                mainStyle: "",
                title: "Non-Heap",
                desc: "JVM's non-heap information and full garbage collection time required",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "Max",
                        desc: "Maximum non-heap size"
                    },{
                        name: "Used",
                        desc: "Non-heap size currently in use"
                    },{
                        name: "FGC",
                        desc: "Time required for full garbage collection (number of FGCs in parenthesis if it occurred more than once)"
                    }]
                }]
            },
            cpuUsage: {
                mainStyle: "",
                title: "Cpu Usage",
                desc: "JVM/System's CPU Usage - For multi-core CPUs, displays the average CPU usage of all the cores",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "Java 1.6",
                        desc: "Only JVM's CPU usage is collected"
                    },{
                        name: "Java 1.7+",
                        desc: "Both JVM's and system's CPU usage are collected"
                    }]
                }]
            },
            tps: {
                mainStyle: "",
                title: "TPS",
                desc: "Transactions received by the server per second ",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "Sampled New (S.N)",
                        desc: "Profiled transactions that started from the selected agent"
                    },{
                        name: "Sampled Continuation (S.C)",
                        desc: "Profiled transactions that started from another agent"
                    },{
                        name: "Unsampled New (U.N)",
                        desc: "Unprofiled transactions that started from the selected agent"
                    },{
                        name: "Unsampled Continuation (U.C)",
                        desc: "Unprofiled transactions that started from another agent"
                    },{
                        name: "Total",
                        desc: "All transactions"
                    }]
                }]
            },
            activeThread: {
                mainStyle: "",
                title: "Active Thread",
                desc: "Snapshots of the agent's active thread status, categorized by how long they have been active for serving a request.",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "Fast (1s)",
                        desc: "Number of threads that have been active for less than or equal to 1s"
                    },{
                        name: "Normal (3s)",
                        desc: "Number of threads that have been active for less than or equal to 3s but longer than 1s"
                    },{
                        name: "Slow (5s)",
                        desc: "Number of threads that have been active for less than or equal to 5s but longer than 3s"
                    },{
                        name: "Very Slow (slow)",
                        desc: "Number of threads that have been active for longer than 5s"
                    }]
                }]
            },
            dataSource: {
                mainStyle: "",
                title: "Data Source",
                desc: "Show the status of agent's data source.",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "Active Avg",
                        desc: "Average number of active connections"
                    },{
                        name: "Active Max",
                        desc: "Maximum number of active connections"
                    },{
                        name: "Total Max",
                        desc: "The maximum number of connections that can be allocated at the same time"
                    },{
                        name: "Type",
                        desc: "Type of DB Connection Pool"
                    }]
                }]
            },
            responseTime: {
                mainStyle: "",
                title: "Response time",
                desc: "Shows the status of agent's response time.",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "Avg",
                        desc: "Average Response Time (unit : milliseconds)"
                    }]
                }]
            },
            openFileDescriptor: {
                mainStyle: "",
                title: "File Descriptor",
                desc: "Shows the status of agent's file descriptors.",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "Open File Descriptor",
                        desc: "Number of open file descriptor currently used"
                    }]
                }]
            },
            directBufferCount: {
                mainStyle: "",
                title: "Direct Buffer",
                desc: "Shows the status of agent's direct buffer",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "Direct Buffer Count",
                        desc: "Number of direct buffer"
                    }]
                }]
            },
            directBufferMemory: {
                mainStyle: "",
                title: "Direct Buffer Memory",
                desc: "Shows the status of agent's used direct buffer memory",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "Direct Buffer Memory Used",
                        desc: "Currently used direct buffer memory"
                    }]
                }]
            },
            mappedBufferCount: {
                mainStyle: "",
                title: "Mapped Buffer",
                desc: "Shows the status of agent's Mapped buffer",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "Mapped Buffer Count",
                        desc: "Number of Mapped buffer"
                    }]
                }]
            },
            mappedBufferMemory: {
                mainStyle: "",
                title: "Mapped Buffer Memory",
                desc: "Shows the status of agent's used Mapped buffer memory",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "Mapped Buffer Memory Used",
                        desc: "Currently used Mapped buffer memory"
                    }]
                }]
            },
            wrongApp: [
                "<div style='font-size:12px'>The agent is currently registered under {{application2}} due to the following:<br>",
                "1. The agent has moved from {{application1}} to {{application2}}<br>",
                "2. A different agent with the same agent id has been registered to {{application2}}<hr>",
                "For case1, you should delete the mapping between {{application1}} and {{agentId}}.<br>",
                "For case2, the agent id of the duplicate agent must be changed.</div>"
            ].join(""),
            statHeap: {
                mainStyle: "",
                title: "Heap",
                desc: "Heap size used by the agent JVMs",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "MAX",
                        desc: "Largest heap size used by an agent JVM"
                    },{
                        name: "AVG",
                        desc: "Average heap size used by the agent JVMs"
                    },{
                        name: "MIN",
                        desc: "Smallest heap size used by an agent JVM"
                    }]
                }]
            },
            statPermGen: {
                mainStyle: "",
                title: "Non-Heap",
                desc: "Non-heap size used by the agent JVMs",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "MAX",
                        desc: "Largest non-heap size used by an agent JVM"
                    },{
                        name: "AVG",
                        desc: "Average non-heap size used by the agent JVMs"
                    },{
                        name: "MIN",
                        desc: "Smallest non-heap size used by an agent JVM"
                    }]
                }]
            },
            statJVMCpu: {
                mainStyle: "",
                title: "JVM Cpu Usage",
                desc: "CPU used by agent JVM processes - For multi-core CPUs, displays the average CPU usage of all the cores.",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "MAX",
                        desc: "Largest CPU usage of agent JVM processes"
                    },{
                        name: "AVG",
                        desc: "Average CPU usage of agent JVM processes"
                    },{
                        name: "MIN",
                        desc: "Smallest CPU usage of agent JVM processes"
                    }]
                }]
            },
            statSystemCpu: {
                mainStyle: "",
                title: "System pu Usage",
                desc: "CPU usage of every agent's system - For multi-core CPUs, displays the average CPU usage of all cores.",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "MAX",
                        desc: "Largest system CPU usage of agents"
                    },{
                        name: "AVG",
                        desc: "Average system CPU usage of agents"
                    },{
                        name: "MIN",
                        desc: "Smallest system CPU usage of agents"
                    }]
                },{
                    title: "[Reference]",
                    items: [{
                        name: "Java 1.6",
                        desc: "Only the JVM's CPU usage is collected"
                    },{
                        name: "Java 1.7+",
                        desc: "Both JVM's and system's CPU usage are collected"
                    }]
                }]
            },
            statTPS: {
                mainStyle: "",
                title: "TPS",
                desc: "Number of transactions received by the agents per second",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "MAX",
                        desc: "Highest TPS of the agents"
                    },{
                        name: "AVG",
                        desc: "Average TPS of the agents"
                    },{
                        name: "MIN",
                        desc: "Lowest TPS of the agents"
                    }]
                }]
            },
            statActiveThread: {
                mainStyle: "",
                title: "Active Thread",
                desc: "Number of active threads serving user requests",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "MAX",
                        desc: "Highest active thread count of the agents serving user requests"
                    },{
                        name: "AVG",
                        desc: "Average active thread count of the agents serving user requests"
                    },{
                        name: "MIN",
                        desc: "Lowest active thread count of the agents serving user requests"
                    }]
                }]
            },
            statResponseTime: {
                mainStyle: "",
                title: "Response Time",
                desc: "Average response times served by the agents",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "MAX",
                        desc: "Highest value of agents' average response time"
                    },{
                        name: "AVG",
                        desc: "Average value of agents' average response time"
                    },{
                        name: "MIN",
                        desc: "Lowest value of agents' average response time"
                    }]
                }]
            },
            statDataSource: {
                mainStyle: "",
                title: "Data Source",
                desc: "Status of the agents' data source",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "MAX",
                        desc: "Largest data source connection count of the agents"
                    },{
                        name: "AVG",
                        desc: "Average data source connection count of the agents"
                    },{
                        name: "MIN",
                        desc: "Smallest data source connection count of the agents"
                    }]
                }]
            },
            statOpenFileDescriptor: {
                mainStyle: "",
                title: "File Descriptor",
                desc: "Number of file descriptors used by agents",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "MAX",
                        desc: "Max number of open file descriptors"
                    },{
                        name: "AVG",
                        desc: "average open file descriptors"
                    },{
                        name: "MIN",
                        desc: "Min number of open file descriptors"
                    }]
                }]
            },
            statDirectBufferCount: {
                mainStyle: "",
                title: "Direct Buffer",
                desc: "Number of direct buffer used by agents",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "MAX",
                        desc: "Max number of direct buffer"
                    },{
                        name: "AVG",
                        desc: "average number of direct buffer"
                    },{
                        name: "MIN",
                        desc: "Min number of direct buffer"
                    }]
                }]
            },
            statDirectBufferMemory: {
                mainStyle: "",
                title: "Direct Buffer Memory",
                desc: "Number of Direct buffer used by agents",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "MAX",
                        desc: "Max number of direct buffer"
                    },{
                        name: "AVG",
                        desc: "average number of direct buffer"
                    },{
                        name: "MIN",
                        desc: "Min number of direct buffer"
                    }]
                }]
            },
            statMappedBufferCount: {
                mainStyle: "",
                title: "Mapped Buffer",
                desc: "Number of Mapped buffer used by agents",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "MAX",
                        desc: "Max number of Mapped buffer"
                    },{
                        name: "AVG",
                        desc: "average number of Mapped buffer"
                    },{
                        name: "MIN",
                        desc: "Min number of Mapped buffer"
                    }]
                }]
            },
            statMappedBufferMemory: {
                mainStyle: "",
                title: "Mapped Buffer Memory",
                desc: "Number of Mapped buffer used by agents",
                category: [{
                    title: "[Legend]",
                    items: [{
                        name: "MAX",
                        desc: "Max number of Mapped buffer"
                    },{
                        name: "AVG",
                        desc: "average number of Mapped buffer"
                    },{
                        name: "MIN",
                        desc: "Min number of Mapped buffer"
                    }]
                }]
            }
        },
        callTree: {
            column: {
                mainStyle: "",
                title: "Call Tree",
                desc: "",
                category: [{
                    title: "[Column]",
                    items: [{
                        name: "Gap",
                        desc: "Elapsed time between the start of the previous method and the entry of this method"
                    },{
                        name: "Exec",
                        desc: "Duration of the method call from entry to exit"
                    },{
                        name: "Exec(%)",
                        desc: "<img src='images/help/callTree_01.png'/>"
                    },{
                        name: "",
                        desc: "<span style='background-color:#FFFFFF;color:#5bc0de'>Light blue</span> The execution time of the method call as a percentage of the total execution time of the transaction"
                    },{
                        name: "",
                        desc: "<span style='background-color:#FFFFFF;color:#4343C8'>Dark blue</span> A percentage of the self execution time"
                    },{
                        name: "Self",
                        desc: "Duration of the method call from entry to exit, excluding time consumed in nested methods call"
                    }]
                }]
            }
        },
        transactionTable: {
            log: {}
        },
        transactionList: {
            openError: {
                noParent: "Unable to scan any more data due to the change of\r\nscatter data ",
                noData: "父窗口中没有 {{application}} 的 scatter data ."
            }
        },
        applicationInspectorGuideMessage: "Application Inspector 未启用.<br>" +
        "启用 Application Inspector请参考文档."
    };
    pinpointApp.constant('helpContent-cn', oHelp );
})();