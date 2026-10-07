<template>
  <div class="home-container">
    <div class="welcome-section">
      <b class="welcome-text">您好！{{ user.username }}</b><br>
      <span class="welcome-sub">欢迎来到人工智能气候预测系统后台管理界面</span>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-icon"><i class="el-icon-user-solid"></i></div>
          <div class="stat-info">
            <div class="stat-label">系统用户总数</div>
            <div class="stat-value">{{ usertotal }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-icon"><i class="el-icon-s-custom"></i></div>
          <div class="stat-info">
            <div class="stat-label">员工总数</div>
            <div class="stat-value">{{ emptotal }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-icon"><i class="el-icon-office-building"></i></div>
          <div class="stat-info">
            <div class="stat-label">产业总数</div>
            <div class="stat-value">{{ buildtotal }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="announcement-card">
      <div class="announcement-header">
        <h3 class="announcement-title">{{ title }}</h3>
        <i class="el-icon-bell announcement-icon"></i>
      </div>
      <el-divider/>
      <p class="announcement-content">{{ content }}</p>
      <p class="announcement-meta">
        <span>发布时间：{{ releaseTime }}</span>
        <span>发布者：{{ author }}</span>
      </p>
    </el-card>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <div id="main" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <div id="pie" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-footer class="home-footer">
      <span>Copyright © AIMB | 2024 Artificial intelligence climate prediction system_v1</span>
    </el-footer>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: "Home",
  data(){
    return{
      announcement: {
        title: "",
        content: "",
        releaseTime: "",
        author:"",
      },
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
      usertotal:0,
      emptotal:0,
      buildtotal:0,
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      this.request.get("/announcement/maxId").then((res) => {
        const latestAnnouncement = res.data;
        this.announcement.title = latestAnnouncement.title;
        this.announcement.content = latestAnnouncement.content;
        this.announcement.releaseTime = latestAnnouncement.releaseTime;
        this.announcement.author = latestAnnouncement.author;
      });

      this.request.get("/user/total").then((res) => {
        this.usertotal = res.data;
      });
      this.request.get("/emp/total").then((res) => {
        this.emptotal = res.data;
      });
      this.request.get("/building/total").then((res) => {
        this.buildtotal = res.data;
      });
    },
  },
  computed: {
    title() {
      return this.announcement.title;
    },
    content() {
      return this.announcement.content;
    },
    releaseTime() {
      return this.announcement.releaseTime;
    },
    author() {
      return this.announcement.author;
    },
  },
  mounted() {
    var option = {
      title: {
        text: '用户所在省份统计图',
        subtext: '趋势图',
        left: 'center',
        textStyle: { color: '#cbd5e1' },
        subtextStyle: { color: '#64748b' },
      },
      tooltip: {
        trigger: 'item',
        backgroundColor: 'rgba(30, 41, 59, 0.9)',
        borderColor: 'rgba(100, 116, 139, 0.5)',
        textStyle: { color: '#e0e7ff' },
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        textStyle: { color: '#cbd5e1' },
      },
      xAxis: {
        type: 'category',
        data: ["江苏", "河南", "湖南", "浙江"],
        axisLabel: { color: '#cbd5e1' },
        axisLine: { lineStyle: { color: 'rgba(100, 116, 139, 0.3)' } },
      },
      yAxis: {
        type: 'value',
        axisLabel: { color: '#cbd5e1' },
        splitLine: { lineStyle: { color: 'rgba(100, 116, 139, 0.15)' } },
        axisLine: { lineStyle: { color: 'rgba(100, 116, 139, 0.3)' } },
      },
      series: [
        {
          name:"地址",
          data: [],
          type: 'line',
          lineStyle: { color: '#58a6ff' },
          itemStyle: { color: '#58a6ff' },
        },
        {
          name:"地址",
          data: [],
          type: 'bar',
          itemStyle: { color: '#67e8f9' },
        },
      ]
    };

    var chartDom = document.getElementById('main');
    var myChart = echarts.init(chartDom, 'dark');
    myChart.setOption(option);


    this.request.get("/echarts/addresss").then(res => {
      option.series[0].data = res.data
      option.series[1].data = res.data
      myChart.setOption(option);
    })


    var pieOption = {
      title: {
        text: '各季度系统用户比例统计图',
        subtext: '比例图',
        left: 'center',
        textStyle: { color: '#cbd5e1' },
        subtextStyle: { color: '#64748b' },
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a}<br/>{b}:{c} ({d}%)',
        backgroundColor: 'rgba(30, 41, 59, 0.9)',
        borderColor: 'rgba(100, 116, 139, 0.5)',
        textStyle: { color: '#e0e7ff' },
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        textStyle: { color: '#cbd5e1' },
      },
      series: [
        {
          name:'用户',
          type:'pie',
          radius: '60%',
          label:{
            normal:{
              show:true,
              position:'inner',
              textStyle : {
                fontWeight : 300 ,
                fontSize : 14,
                color: "#fff"
              },
              formatter:'{d}%'
            }
          },
          data: [],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        },
      ],
    };

    var pieDom = document.getElementById('pie');
    var pieChart = echarts.init(pieDom, 'dark');
    this.request.get("/echarts/members").then(res => {
      pieOption.series[0].data = [
        {name: "第一季度", value: res.data[0]},
        {name: "第二季度", value: res.data[1]},
        {name: "第三季度", value: res.data[2]},
        {name: "第四季度", value: res.data[3]},
      ]
      pieChart.setOption(pieOption)
    })
  }
}
</script>

<style scoped>
.home-container {
  color: #cbd5e1;
}
.welcome-section {
  padding-bottom: 20px;
}
.welcome-text {
  color: #e0e7ff;
  font-size: 18px;
  font-weight: bold;
}
.welcome-sub {
  color: #64748b;
  font-size: 14px;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 20px;
}
.stat-card {
  display: flex;
  align-items: center;
}
.stat-card >>> .el-card__body {
  display: flex;
  align-items: center;
  padding: 20px;
  width: 100%;
}
.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
  flex-shrink: 0;
  background: rgba(88, 166, 255, 0.12);
  color: #58a6ff;
}
.stat-info {
  flex: 1;
}
.stat-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 4px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #67e8f9;
}

/* 公告卡片 */
.announcement-card {
  margin-bottom: 20px;
}
.announcement-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.announcement-title {
  color: #f7ba1e;
  text-align: center;
  margin: 0;
  font-size: 18px;
}
.announcement-icon {
  color: #f7ba1e;
  font-size: 18px;
}
.announcement-content {
  text-align: justify;
  font-size: 14px;
  color: #cbd5e1;
  line-height: 1.8;
  text-indent: 2em;
  margin: 0;
}
.announcement-meta {
  text-align: right;
  font-size: 11px;
  color: #64748b;
  margin: 12px 0 0;
}
.announcement-meta span {
  margin-left: 16px;
}

/* 图表卡片 */
.chart-row {
  margin-bottom: 20px;
}
.chart-card {
  height: 500px;
}
.chart-card >>> .el-card__body {
  padding: 12px;
  height: calc(100% - 0px);
}
.chart-box {
  width: 100%;
  height: 450px;
}

/* 页脚 */
.home-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(12, 12, 30, 0.5);
  color: #64748b;
  font-size: 12px;
  margin-top: 10px;
}
</style>
