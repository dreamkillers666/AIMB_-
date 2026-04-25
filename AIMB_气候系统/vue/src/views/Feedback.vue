<template>
  <div>
    <el-row class="common-content" style="background-color: #dde7ee;height: 35px;margin-top: -12px">
      <el-row>
        <div class="color-line" style="margin-top: -14px;margin-left: 1px"></div>
        <span style="margin-left: 25px; font-size: 1.3em;">用户反馈</span>
      </el-row>
    </el-row>
    <el-card style="margin-top: 7px">
      <div style="margin: 10px 0">
        <el-input style="width: 200px" placeholder="请输入名称" suffix-icon="el-icon-search" v-model="name"></el-input>
        <el-button class="ml-5" type="primary" @click="load">搜索</el-button>
        <el-button type="warning" @click="reset">重置</el-button>
      </div>
      <div style="margin: 10px 0">
        <el-button type="primary" @click="handleAdd">新增 <i class="el-icon-circle-plus-outline"></i></el-button>
        <el-popconfirm
            class="ml-5"
            confirm-button-text='确定'
            cancel-button-text='我再想想'
            icon="el-icon-info"
            icon-color="red"
            title="您确定批量删除这些数据吗？"
            @confirm="delBatch"
        >
          <el-button type="danger" slot="reference">批量删除 <i class="el-icon-remove-outline"></i></el-button>
        </el-popconfirm>
        <el-upload
            action="http://localhost:9090/feedback/import"
            :show-file-list="false"
            accept="xlsx"
            :on-success="handleExcelImportSuccess"
            style="display: inline-block">
          <el-button type="primary" class="ml-5">导入 <i class="el-icon-bottom"></i></el-button>
        </el-upload>
        <el-button type="primary" @click="exp">导出 <i class="el-icon-top"></i></el-button>
      </div>
      <el-table :data="tableData" border stripe :header-cell-class-name="'headerBg'"
                @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="name" label="反馈标题"></el-table-column>
        <el-table-column prop="content" label="反馈内容">
          <template slot-scope="scope">
            <el-button @click="view(scope.row.content)" type="primary">查看内容</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="user" label="申请用户"></el-table-column>
        <el-table-column prop="time" label="申请时间"></el-table-column>
        <el-table-column prop="type" label="反馈类型"></el-table-column>
        <el-table-column prop="feedbackStatus" label="审批状态">
          <template slot-scope="scope">
            <el-tag type="success" v-if="scope.row.feedbackStatus === 1">审批通过</el-tag>
            <el-tag type="danger" v-if="scope.row.feedbackStatus === -1">审批未通过</el-tag>
            <el-tag type="primary" v-if="scope.row.feedbackStatus === 0">待审批</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审批" width="210">
          <template slot-scope="scope">
            <el-button type="primary" @click="auditSuccess(scope.row.id)">审批通过</el-button>
            <el-button type="warning" @click="auditFail(scope.row.id)">审批不通过</el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center">
          <template slot-scope="scope">
            <el-button type="success" @click="handleEdit(scope.row)">编辑 <i class="el-icon-edit"></i></el-button>
            <el-popconfirm
                class="ml-5"
                confirm-button-text='确定'
                cancel-button-text='我再想想'
                icon="el-icon-info"
                icon-color="red"
                title="您确定删除吗？"
                @confirm="del(scope.row.id)"
            >
              <el-button type="danger" slot="reference">删除 <i class="el-icon-remove-outline"></i></el-button>
            </el-popconfirm>
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

      <el-dialog title="文章信息" :visible.sync="dialogFormVisible" width="60%">
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

      <el-dialog title="详细内容" :visible.sync="viewDialogVis" width="60%">
        <el-card>
          <div>
            <mavon-editor
                class="md"
                :value="content"
                :subfield="false"
                :defaultOpen="'preview'"
                :toolbarsFlag="false"
                :editable="false"
                :scrollStyle="true"
                :ishljs="true"
            />
          </div>
        </el-card>
      </el-dialog>
    </el-card>
  </div>

</template>

<script>

import axios from "axios";

export default {
  name: "Feedback",
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
    del(id) {
      this.request.delete("/feedback/" + id).then(res => {
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
      this.request.post("/feedback/del/batch", ids).then(res => {
        if (res.code === '200') {
          this.$message.success("批量删除成功")
          this.load()
        } else {
          this.$message.error("批量删除失败")
        }
      })
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
    handleExcelImportSuccess() {
      this.$message.success("导入成功")
      this.load()
    },
    exp() {
      window.open("http://localhost:9090/feedback/export")
    },
    auditSuccess(id) {
      this.request.post("/feedback/auditSuccess/" + id).then(res => {
        if (res.data) {
          this.$message.success("审核成功")
          this.load()
        } else {
          this.$message.error("审核失败")
        }
      })
    },

    auditFail(id) {
      this.request.post("/feedback/auditFail/" + id).then(res => {
        if (res.data) {
          this.$message.success("审核成功")
          this.load()
        } else {
          this.$message.error("审核失败")
        }
      })
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
