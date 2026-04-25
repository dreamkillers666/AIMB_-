<template>
  <div>
    <div style="color: #666;font-size: 14px;">
      <div style="padding-bottom: 20px;">
        <b>您好！{{ user.username }}</b><br>
        <b>欢迎来到人工智能气候预测系统后台管理界面！</b>
      </div>
      <el-card style="margin: 10px 0;">
        <h3 style="text-align: center;margin-top: -10px; color: #E6A23C">{{title}}<i style="color: #E6A23C" class="header-icon el-icon-info"></i></h3>
        <el-divider/>
        <p style="text-align: justify;font-size: 14px; text-indent: 2em;">{{content}}<br></p>
        <p style="text-align: right;font-size: 11px; margin: 0;"><strong>发布时间：</strong>{{releaseTime}}  <strong>发布者: </strong>{{author}}</p>
      </el-card><br>
    </div>

    <el-row :gutter="10" style="margin-bottom: 50px;">
      <el-col>
        <el-card style="color: #409EFF">
          <div><i class="el-icon-user-solid"></i>本系统用户总数</div>
          <div style="padding: 10px 0;text-align: center;font-weight: bold">
            <el-tag type="primary" style="font-size: 20px">{{this.usertotal}}</el-tag>
          </div>
        </el-card>
      </el-col>


    </el-row>

    <el-row>
      <el-col :span="12">
        <el-card>
          <div id="main" style="width: 500px; height: 450px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div id="pie" style="width: 500px; height: 450px"></div>
        </el-card>
      </el-col>
    </el-row><br>

    <el-footer class="index-footer" style="margin-top:5px;height: 40px;bottom: 0;width: 100%">
       <span >
        Copyright © AIMB | 2024 Artificial intelligence climate prediction system_v1
      </span>
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
  mounted() {  //页面元素渲染之后再触发


    var option = {
      title: {
        text: '用户所在省份统计图',
        subtext: '趋势图',
        left: 'center'
      },
      tooltip: {
        trigger: 'item'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      xAxis: {
        type: 'category',
        data: ["江苏", "河南", "湖南", "浙江"]
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name:"地址",
          data: [],
          type: 'line'
        },
        {
          name:"地址",
          data: [],
          type: 'bar'
        },

      ]
    };

    var chartDom = document.getElementById('main');
    var myChart = echarts.init(chartDom);
    // myChart.setOption(option);



    this.request.get("/echarts/addresss").then(res => {

      option.series[0].data = res.data
      option.series[1].data = res.data
      myChart.setOption(option);
    })





    var pieOption = {
      title: {
        text: '各季度系统用户比例统计图',
        subtext: '比例图',
        left: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a}<br/>{b}:{c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name:'用户',
          type: 'pie',
          radius: '60%',
          label:{            //饼图图形上的文本标签
            normal:{
              show:true,
              position:'inner', //标签的位置
              textStyle : {
                fontWeight : 300 ,
                fontSize : 14,    //文字的字体大小
                color: "#fff"
              },
              formatter:'{d}%'
            }
          },
          data: [],  // 填空
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
    var pieChart = echarts.init(pieDom);
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
.index-footer{
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #333;
  color: #989898;
  font-size: 12px;
}
</style>