<template>
  <div>


    <el-card style="margin-top: 7px">
      <div style="flex: 1">

        <el-menu :default-active="'1'" class="el-menu-demo" mode="horizontal" router>
          <el-menu-item index="/usermanage/data_introduction">数据集介绍</el-menu-item>
          <el-menu-item index="/usermanage/data_download">数据集下载</el-menu-item>


        </el-menu>
      </div>
      <div style="margin: 10px 0">
        <el-input style="width: 200px" placeholder="请输入名称" suffix-icon="el-icon-search" v-model="name"></el-input>
        <el-button class="ml-5" type="primary" @click="load">搜索</el-button>
        <el-button type="warning" @click="reset">重置</el-button>
      </div>

      <el-table :data="tableData" border stripe :header-cell-class-name="'headerBg'"
                @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="name" label="数据名称"></el-table-column>
        <el-table-column prop="type" label="数据类型"></el-table-column>
        <el-table-column prop="size" label="数据大小(kb)"></el-table-column>

        <el-table-column label="下载">
          <template slot-scope="scope">
            <el-button type="primary" @click="download(scope.row.url)">下载</el-button>
          </template>
        </el-table-column>
        <el-table-column label="启用">
          <template slot-scope="scope">
            <el-switch v-model="scope.row.enable" active-color="#13ce66" inactive-color="#ccc"
                       @change="changeEnable(scope.row)"></el-switch>
          </template>
        </el-table-column>

      </el-table>

      <div style="padding: 10px 0">
        <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[2, 5, 10, 20]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total">
        </el-pagination>
      </div>
    </el-card>


  </div>

</template>

<script>
export default {
  name: "EnsoData",
  data() {
    return {
      tableData: [],
      name: '',
      multipleSelection: [],
      pageNum: 1,
      pageSize: 10,
      total: 0
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      this.request.get("/ensodata/page", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.name,
        }
      }).then(res => {
        this.tableData = res.data.records
        this.total = res.data.total

      })
    },
    changeEnable(row) {
      this.request.post("/ensodata/update", row).then(res => {
        if (res.code === '200') {
          this.$message.success("操作成功")
        }
      })
    },
    del(id) {
      this.request.delete("/ensodata/" + id).then(res => {
        if (res.code === '200') {
          this.$message.success("删除成功")
          this.load()
        } else {
          this.$message.error("删除失败")
        }
      })
    },
    handleSelectionChange(val) {
      console.log(val)
      this.multipleSelection = val
    },
    delBatch() {
      let ids = this.multipleSelection.map(v => v.id)  // [{}, {}, {}] => [1,2,3]
      this.request.post("/ensodata/del/batch", ids).then(res => {
        if (res.code === '200') {
          this.$message.success("批量删除成功")
          this.load()
        } else {
          this.$message.error("批量删除失败")
        }
      })
    },
    reset() {
      this.name = ""
      this.load()
    },
    handleSizeChange(pageSize) {
      console.log(pageSize)
      this.pageSize = pageSize
      this.load()
    },
    handleCurrentChange(pageNum) {
      console.log(pageNum)
      this.pageNum = pageNum
      this.load()
    },
    handleFileUploadSuccess(res) {
      console.log(res)
      this.load()
    },
    download(url) {
      window.open(url)
    },

  }
}
</script>

<style scoped>
.ah {
  display: inline-block;
  align-items: center;
  height: 100%;
  line-height: 35px;
  color: #fff;
  text-decoration: none;
}
.color-line {
  position: absolute;
  left: 0;
  top: 50%;
  width: 10px;
  height: 35px;
  background-color:#4682B4;
}

</style>