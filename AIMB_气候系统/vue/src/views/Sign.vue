<template>
  <div style="margin-top: 5px">
    <div id="container" style="width: 100%; height: calc(100vh - 100px)"></div>
    <div id="info"></div>
  </div>
</template>

<script>
export default {
  name: "Sign",
  mounted() {
    // 创建地图实例
    var map = new AMap.Map("container", {
      zoom: 11.2,
      center: [118.647258,32.088423],
      resizeEnable: true
    })

    var marker = new AMap.Marker({
      position: new AMap.LngLat(118.647258,32.088423),   // 经纬度对象，也可以是经纬度构成的一维数组[116.39, 39.9]
      title: '南工大',
      icon: '//vdata.amap.com/icons/b18/1/2.png',
    })
    // 将创建的点标记添加到已有的地图实例：
    map.add(marker);

    // 创建信息窗口
    const infoWindow = new AMap.InfoWindow({
      offset: new AMap.Pixel(0, -30),
      closeWhenClickMap: true, // 点击地图时关闭信息窗口
    });


    marker.on('click', (e) => {
      const lnglat = e.lnglat;
      const position = [lnglat.getLng(), lnglat.getLat()];
      console.log('签到位置：', position);

      const currentTime = new Date(); // 获取当前时间
      const startTime = new Date();
      startTime.setHours(8, 0, 0); // 设置签到开始时间为当天的8:00 AM
      const endTime = new Date();
      endTime.setHours(24, 0, 0); // 设置签到结束时间为当天的9:00 AM

      if (currentTime >= startTime && currentTime <= endTime) {
        this.workId = this.user.workId
        // 在签到时间范围内
        this.sign();
        // 显示签到成功信息窗口
        infoWindow.setContent("<span style='color: red;'>签到成功</span>");
        infoWindow.open(map, lnglat);

      } else {
        // 不在签到时间范围内
        console.log('不在签到时间范围内，请确认签到时间');
        // 显示签到失败信息窗口
        infoWindow.setContent("<span style='color: red;'>不在签到时间范围内，签到时间为8:00-9:00</span>");
        infoWindow.open(map, lnglat);
      }
    });


  },
  data() {
    return {
      workId: "",
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {}
    }
  },
  created() {
  },
  methods: {
    sign() {
      // 处理签到逻辑，比如发送签到请求等
      this.request.post("/attendance/sign/" + this.workId).then(res => {
        if (res.data) {
          this.$message.success("删除成功")
          this.load()
        } else {
          this.$message.error("删除失败")
        }
      })
    },
  }
}
</script>

<style>

</style>
