<script>
export default {
  name: "rainfall_resource"
}
</script>

<template>
  <el-card style="">
    <div style="flex: 1">

      <el-menu :default-active="'1'" class="el-menu-demo" mode="horizontal" router>
        <el-menu-item index="/usermanage/seasonal_introduce">次季节降水介绍</el-menu-item>
        <el-menu-item index="/usermanage/rainfall_forecast">预测结果</el-menu-item>
        <el-menu-item index="/usermanage/rainfall_data">数据</el-menu-item>
        <el-menu-item index="/usermanage/rainfall_resource">更多资源</el-menu-item>

      </el-menu>
    </div>

    <div style="margin: 20px 40px ;font-size: 20px;line-height: 1.5;text-align: justify">
      <h4 style="color: dodgerblue;margin-top: 10px">相关网站</h4>

      <el-row>
        <el-col :span="24">
          <ul>
            <li v-for="link in links" :key="link.text">
              <el-link :href="link.url" type="primary" target="_blank">{{ link.text }}</el-link>
            </li>
          </ul>
        </el-col>
      </el-row>

    </div>
  </el-card>

</template>

<script>
export default {
  name: "seasonal_introduce",
  data() {
    return {
      links: [
        { text: 'NOAA Climate.gov precipitation Timeseries', url: 'https://www.psl.noaa.gov/data/timeseries/month/' },
        { text: 'NOAA Climate precipitation datasets', url: 'https://www.psl.noaa.gov/data/gridded/index.html' },
        { text: 'ECMWF precipitation datasets', url: 'https://cds.climate.copernicus.eu/datasets/reanalysis-era5-single-levels?tab=overview' },
        { text: '国家气象科学数据中心降水数据', url: 'https://data.cma.cn/search/uSearch.html?keywords=%E9%99%8D%E6%B0%B4' }
      ],
    };
  }
}
</script>

<style scoped>

</style>