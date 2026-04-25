<template>
  <div>
    <el-card style="margin-top: 0px">
      <div style="color: #666">
        <div style="margin: 10px 0">
          <el-input size="small" style="width: 300px" placeholder="请输入名称" suffix-icon="el-icon-search" v-model="name"></el-input>
          <el-button class="ml-5" type="primary" @click="load" size="small">搜索</el-button>
          <el-button type="warning" @click="reset" size="small">重置</el-button>
          <el-button style="margin-left: 10px;width: 100px;height: 33px;margin-top:-20px" type="primary" @click="handleAdd">申请反馈 <i class="el-icon-circle-plus-outline"></i></el-button>
        </div>
        <div style="margin: 10px 0">
          <div style="padding: 10px 0; border-bottom: 1px dashed #ccc" v-for="item in tableData" :key="item.id">
            <div class="pd-10" style="font-size: 20px; color: #3F5EFB; cursor: pointer" @click="$router.push('/usermanage/comment?id=' + item.id)">{{ item.name }}</div>
            <div style="font-size: 14px; margin-top: 10px">
               <span><strong>反馈类型：</strong> {{ item.type }}</span>
<!--              <span style="margin-left: 10px"><strong>审批状态：</strong> {{ item.feedbackStatus}}</span>-->
              <span style="margin-left: 10px"><strong>审批状态：</strong>
                <template v-if="item.feedbackStatus === 0">待审批</template>
                <template v-else-if="item.feedbackStatus === 1">审批通过</template>
                <template v-else-if="item.feedbackStatus === -1">审核未通过</template>
                <template v-else>未知状态</template>
              </span>
              <i class="el-icon-time" style="margin-left: 10px"></i> <span>{{ item.time }}</span>
            </div>
          </div>
        </div>

        <div style="padding: 10px 0">
          <el-pagination
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              :current-page="pageNum"
              :page-sizes="[2, 5, 10, 20]"
              :page-size="pageSize"
              layout="total, prev, pager, next"
              :total="total">
          </el-pagination>
        </div>

        <el-dialog title="文章信息" :visible.sync="dialogFormVisible" width="60%" >
          <el-form label-width="80px" size="small">
            <el-form-item label="反馈标题">
              <el-input v-model="form.name" autocomplete="off"></el-input>
            </el-form-item>
            <el-form-item label="反馈内容">
              <mavon-editor ref="md" v-model="form.content" :ishljs="true" @imgAdd="imgAdd"/>
            </el-form-item>
            <el-form-item label="反馈类型">
              <el-select v-model="form.type" placeholder="请选择反馈类型" autocomplete="off">
                <el-option label="建议" value="建议"></el-option>
                <el-option label="工资" value="工资"></el-option>
                <el-option label="培训" value="培训"></el-option>
                <el-option label="合同" value="合同"></el-option>
                <el-option label="其他" value="其他"></el-option>
              </el-select>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button @click="dialogFormVisible = false">取 消</el-button>
            <el-button type="primary" @click="save">确 定</el-button>
          </div>
        </el-dialog>
      </div>
    </el-card>
  </div>

</template>

<script>

import axios from "axios";

export default {
  name: "Article",
  data() {
    return {
      form: {},
      tableData: [],
      name: '',
      multipleSelection: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      dialogFormVisible: false,
      teachers: [],
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
      content: '',
      viewDialogVis: false
    }
  },
  created() {
    this.load()
  },
  methods: {
    view(content) {
      this.content = content
      this.viewDialogVis = true
    },
    // 绑定@imgAdd event
    imgAdd(pos, $file) {
      let $vm = this.$refs.md
      // 第一步.将图片上传到服务器.
      const formData = new FormData();
      formData.append('file', $file);
      axios({
        url: 'http://localhost:9090/file/upload',
        method: 'post',
        data: formData,
        headers: {'Content-Type': 'multipart/form-data'},
      }).then((res) => {
        // 第二步.将返回的url替换到文本原位置![...](./0) -> ![...](url)
        $vm.$img2Url(pos, res.data);
      })
    },
    load() {
      this.request.get("/feedback/page", {
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
      this.request.post("/feedback/update", row).then(res => {
        if (res.code === '200') {
          this.$message.success("操作成功")
        }
      })
    },
    handleAdd() {
      this.dialogFormVisible = true
      this.form = {}
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.dialogFormVisible = true
    },
    handleSelectionChange(val) {
      console.log(val)
      this.multipleSelection = val
    },
    save() {
      this.request.post("/feedback", this.form).then(res => {
        if (res.code === '200') {
          this.$message.success("保存成功")
          this.dialogFormVisible = false
          this.load()
        } else {
          this.$message.error("保存失败")
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

  }
}
</script>

<style scoped>
.color-line {
  position: absolute;
  left: 0;
  top: 50%;
  width: 10px;
  height: 35px;
  background-color: #4682B4;
}
</style>
