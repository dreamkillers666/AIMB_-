<template>
  <div>
    <el-row class="common-content" style="background-color: #dde7ee;height: 35px;margin-top: -12px">
      <el-row class="title">
        <div class="color-line" style="margin-top: -14px;margin-left: 1px"></div>
        <span style="margin-left: 25px; font-size: 1.3em;">考勤管理</span>
      </el-row>
    </el-row>

    <el-card style="margin-top: 7px">
      <div style="margin: 10px 0">
        <el-input style="width: 200px" placeholder="请输入姓名" suffix-icon="el-icon-search" v-model="name"></el-input>
        <el-button class="ml-5" type="primary" @click="load">搜索</el-button>
        <el-button class="ml-5" type="warning" @click="reset">重置</el-button>
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
            action="http://localhost:9090/attendance/import"
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
        <el-table-column prop="id" label="考勤编号"></el-table-column>
        <el-table-column prop="name" label="员工姓名"></el-table-column>
        <el-table-column prop="workId" label="员工工号"></el-table-column>
        <el-table-column prop="attendanceDate" label="考勤日期"></el-table-column>
        <el-table-column prop="startTime" label="上班时间/时"></el-table-column>
        <el-table-column prop="endTime" label="下班时间/时"></el-table-column>
        <el-table-column prop="workHours" label="工作时长"></el-table-column>
        <el-table-column prop="lateEarlyTimes" label="迟到/早退次数"></el-table-column>

        <el-table-column prop="isSign" label="签到情况">
          <template slot-scope="scope">
            <el-tag type="primary" v-if="scope.row.isSign === 1">成功</el-tag>
            <el-tag type="warning" v-if="scope.row.isSign === 0">失败</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" align="center">
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

      <el-dialog title="考勤信息" :visible.sync="dialogFormVisible" width="30%">
        <el-form label-width="80px" size="small">
          <el-form-item label="员工姓名">
            <el-input v-model="form.name" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="员工工号">
            <el-input v-model="form.workId" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="考勤日期">
            <el-date-picker
                v-model="form.attendanceDate"
                type="date"
                placeholder="请选择日期"
                value-format="yyyy-MM-dd"
                clearable
                style="width: 100%;"
            />
          </el-form-item>
          <el-form-item label="上班时间">
            <el-input-number v-model="form.startTime"  placeholder="8" controls-position="right" :min="0" :max="23" :step="0.5" />
          </el-form-item>
          <el-form-item label="下班时间">
            <el-input-number v-model="form.endTime" placeholder="17" controls-position="right" :min="0" :max="23" :step="0.5" />
          </el-form-item>
          <el-form-item label="工作时长">
            <el-input-number v-model="workHours" controls-position="right" :min="0" :max="23" :step="0.5" />
          </el-form-item>
          <el-form-item label="迟到/早退次数">
            <el-input v-model="form.lateEarlyTimes" autocomplete="off"></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="dialogFormVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">确 定</el-button>
        </div>
      </el-dialog>
    </el-card>
  </div>

</template>

<script>
export default {
  name: "Attendance",
  data() {
    return {
      tableData: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      name: "",
      form: {},
      dialogFormVisible: false,
      multipleSelection: [],
    }
  },
  created() {
    this.load()
  },
  computed: {
    workHours() {
      return parseFloat(this.form.endTime)- parseFloat(this.form.startTime)
    }
  },
  methods: {
    load() {
      this.request.get("/attendance/page", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.name,
        }
      }).then(res => {
        // 注意data
        this.tableData = res.data.records
        this.total = res.data.total

      })
    },
    save() {
      this.form.workHours=this.workHours
      this.request.post("/attendance", this.form).then(res => {
        if (res.data) {
          this.$message.success("保存成功")
          this.dialogFormVisible = false
          this.load()
        } else {
          this.$message.error("保存失败")
        }
      })
    },
    del(id) {
      this.request.delete("/attendance/" + id).then(res => {
        if (res.data) {
          this.$message.success("删除成功")
          this.load()
        } else {
          this.$message.error("删除失败")
        }
      })
    },
    delBatch() {
      let ids = this.multipleSelection.map(v => v.id)  // [{}, {}, {}] => [1,2,3]
      this.request.post("/attendance/del/batch", ids).then(res => {
        if (res.data) {
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
    exp() {
      window.open("http://localhost:9090/attendance/export")
    },
    handleExcelImportSuccess() {
      this.$message.success("导入成功")
      this.load()
    },
    handleAdd() {
      this.dialogFormVisible = true
      this.form = {}
    },
    handleEdit(row) {
      this.form = row
      this.dialogFormVisible = true
    },
    handleSelectionChange(val) {
      console.log(val)
      this.multipleSelection = val
    }
  }
}
</script>


<style>
.color-line {
  position: absolute;
  left: 0;
  top: 50%;
  width: 10px;
  height: 35px;
  background-color: #4682B4;
}
</style>