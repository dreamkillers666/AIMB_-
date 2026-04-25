<template>
  <el-row class="home-container">
    <el-row class="banner-box">
      <el-carousel class="home-carousel">
        <el-carousel-item class="home-carousel-item" v-for="(item, index) in bannerList" :key="index">
          <img class="lunboimg" :src=item.banner_url alt="" style="width: 100%;height: 100%;">
        </el-carousel-item>
      </el-carousel>
    </el-row>
    <el-row class="lesson-box">
      <el-row class="contain_title">
        <div class="titleInnerAidLine1"></div>
        <div class="mainTitle">精品课程</div>
      </el-row>
      <el-row class="lesson-list">
        <el-row class="lesson-item" v-for="(item,index) in lesson_list" :key="index">
          <el-card class="lesson-item-card">
            <router-link :to="{name:'lessonInfo',query:{'lessonId':item.lessonId}}">
              <img :src="item.pic_url" class="image" style="width: 100%;height: 250px" alt="">
            </router-link>
            <div style="padding: 14px;">
              <span>{{ item.lesson_name }}</span>
              <br>
              <span style="font-size: 15px">授课老师：{{ item.teacher_name }}</span>
            </div>
          </el-card>
        </el-row>
      </el-row>
      <el-row>
        <el-pagination
            @size-change='handleSizeChange'
            @current-change='handleCurrentChange'
            :current-page='pageInfo.pageNum'
            :page-sizes='[10, 50, 100, 500]'
            :page-size='pageInfo.pageSize'
            layout='total, sizes, prev, pager, next, jumper'
            :total='pageInfo.total'
        >
        </el-pagination>
      </el-row>
    </el-row>
    <router-view/>
  </el-row>
</template>
<script>

import {getLessonsByUserIdAndTagId} from "@/network/api/lesson"
import {getAllBanners} from "@/network/api/banner"

export default {
  data() {
    return {
      bannerList: [],
      lesson_list: [],
      userId: '',
      pageInfo: {
        pageNum: 1,
        pageSize: 10,
        size: 0,
        startRow: 0,
        endRow: 0,
        total: 0,
        pages: 0
      }
    }
  },
  created() {
    this.userId = localStorage.getItem("user_id")
    this.getAllLessons()
    this.getAllBanners()
  },
  methods: {
    getAllLessons() {
      let pageRequest = {
        userId: this.userId,
        tagId: 0,
        currentPage: this.pageInfo.pageNum,
        pageSize: this.pageInfo.pageSize
      }

      this.getLessons(pageRequest)
    },
    getLessons(pageRequest) {
      getLessonsByUserIdAndTagId(pageRequest).then(res => {
        if (res.status === '200') {
          let tableData = res.data.list
          let data = res.data

          if (tableData != null && tableData.length > 0) {
            this.lesson_list = tableData

            this.pageInfo = {
              pageNum: data.pageNum,
              pageSize: data.pageSize,
              size: data.size,
              startRow: data.startRow,
              endRow: data.endRow,
              total: data.total,
              pages: data.pages
            }
          }
        }
      })
    },
    getAllBanners() {
      getAllBanners().then(res => {
        if (res.status === '200') {
          this.bannerList = res.data
        }
      })
    },
    handleSizeChange(pageSize) {
      this.pageInfo.pageSize = pageSize
      this.getAllLessons()
    },
    handleCurrentChange(pageNum) {
      this.pageInfo.pageNum = pageNum
      this.getAllLessons()
    }
  }
}
</script>

<style>
.home-container {
  width: 100%;
  height: 100%;
}

.home-container {
  border-radius: 20px;
}

.lesson-box {
  margin-top: 10px;
}

.lesson-list {
  display: flex;
  flex-wrap: wrap;
}

.lesson-item {
  margin-left: 25px;
}

.lesson-item-card {
  width: 250px;
  height: 350px;
  margin-bottom: 25px;
}

.titleInnerAidLine1 {
  display: inline-block;
  width: 4px;
  height: 1.6rem;
  margin-left: 5px;
  background: #409EFF;
}

.mainTitle {
  color: #409EFF;
  /* font-size: 1.5rem; */
  font-size: 18px;
  display: inline-block;
  position: relative;
  height: auto;
  line-height: normal;
  vertical-align: middle;
  padding-bottom: 20px;
  padding-left: 8px;
}

.time {
  font-size: 13px;
  color: #999;
}

.bottom {
  margin-top: 13px;
  line-height: 12px;
}

.button {
  padding: 0;
  float: right;
}

.image {
  width: 100%;
  display: block;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}

.clearfix:after {
  clear: both
}

.home-carousel {
  border-radius: 20px;
}
</style>


