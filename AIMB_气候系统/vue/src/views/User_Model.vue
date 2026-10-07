<template>
  <div>
    <el-card style="margin-top: 20px; background-color: transparent">
      <div style="margin: 10px 0">
        <el-input style="width: 200px" placeholder="请输入姓名" suffix-icon="el-icon-search" v-model="name"></el-input>
        <el-button class="ml-5" type="primary" @click="load">搜索</el-button>
        <el-button class="ml-5" type="warning" @click="reset">重置</el-button>
      </div>


      <el-table :data="tableData" border stripe
                :header-cell-class-name="'headerBg'"
                @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="模型编号"></el-table-column>
        <el-table-column prop="name" label="模型名称"></el-table-column>
        <el-table-column prop="mon1" label="提前1个月"></el-table-column>
        <el-table-column prop="mon3" label="提前3个月"></el-table-column>
        <el-table-column prop="mon6" label="提前6个月"></el-table-column>
        <el-table-column prop="mon12" label="提前12个月"></el-table-column>
        <el-table-column prop="mon20" label="提前20个月"></el-table-column>

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

      <el-dialog title="模型数据" :visible.sync="dialogFormVisible" width="30%">
        <el-form label-width="80px" size="small">
          <el-form-item label="模型">
            <el-input v-model="form.name" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="mon1">
            <el-input v-model="form.mon1" autocomplete="off" type="number" controls-position="right" :min="0" :max="1" :step="0.01"></el-input>
          </el-form-item>
          <el-form-item label="mon2">
            <el-input v-model="form.mon2" autocomplete="off" type="number" controls-position="right" :min="0" :max="1" :step="0.01"></el-input>
          </el-form-item>
          <el-form-item label="mon3">
            <el-input v-model="form.mon3" autocomplete="off" type="number" controls-position="right" :min="0" :max="1" :step="0.01"></el-input>
          </el-form-item>
          <el-form-item label="mon4">
            <el-input v-model="form.mon4" autocomplete="off" type="number" controls-position="right" :min="0" :max="1" :step="0.01"></el-input>
          </el-form-item>
          <el-form-item label="mon5">
            <el-input v-model="form.mon5" autocomplete="off" type="number" controls-position="right" :min="0" :max="1" :step="0.01"></el-input>
          </el-form-item>
          <el-form-item label="mon6">
            <el-input v-model="form.mon6" autocomplete="off" type="number" controls-position="right" :min="0" :max="1" :step="0.01"></el-input>
          </el-form-item>
          <el-form-item label="mon7">
            <el-input v-model="form.mon7" autocomplete="off" type="number" controls-position="right" :min="0" :max="1" :step="0.01"></el-input>
          </el-form-item>
          <el-form-item label="mon8">
            <el-input v-model="form.mon8" autocomplete="off" type="number" controls-position="right" :min="0" :max="1" :step="0.01"></el-input>
          </el-form-item>
          <el-form-item label="mon9">
            <el-input v-model="form.mon9" autocomplete="off" type="number" controls-position="right" :min="0" :max="1" :step="0.01"></el-input>
          </el-form-item>
          <el-form-item label="mon10">
            <el-input v-model="form.mon10" autocomplete="off" type="number" controls-position="right" :min="0" :max="1" :step="0.01"></el-input>
          </el-form-item>
          <el-form-item label="mon11">
            <el-input v-model="form.mon11" autocomplete="off" type="number" controls-position="right" :min="0" :max="1" :step="0.01"></el-input>
          </el-form-item>
          <el-form-item label="mon12">
            <el-input v-model="form.mon12" autocomplete="off" type="number" controls-position="right" :min="0" :max="1" :step="0.01"></el-input>
          </el-form-item>
          <el-form-item label="mon13">
            <el-input v-model="form.mon13" autocomplete="off" type="number" controls-position="right" :min="0" :max="1" :step="0.01"></el-input>
          </el-form-item>
          <el-form-item label="mon14">
            <el-input v-model="form.mon14" autocomplete="off" type="number" controls-position="right" :min="0" :max="1" :step="0.01"></el-input>
          </el-form-item>
          <el-form-item label="mon15">
            <el-input v-model="form.mon15" autocomplete="off" type="number" controls-position="right" :min="0" :max="1" :step="0.01"></el-input>
          </el-form-item>
          <el-form-item label="mon16">
            <el-input v-model="form.mon16" autocomplete="off" type="number" controls-position="right" :min="0" :max="1" :step="0.01"></el-input>
          </el-form-item>
          <el-form-item label="mon17">
            <el-input v-model="form.mon17" autocomplete="off" type="number" controls-position="right" :min="0" :max="1" :step="0.01"></el-input>
          </el-form-item>
          <el-form-item label="mon18">
            <el-input v-model="form.mon18" autocomplete="off" type="number" controls-position="right" :min="0" :max="1" :step="0.01"></el-input>
          </el-form-item>
          <el-form-item label="mon19">
            <el-input v-model="form.mon19" autocomplete="off" type="number" controls-position="right" :min="0" :max="1" :step="0.01"></el-input>
          </el-form-item>
          <el-form-item label="mon20">
            <el-input v-model="form.mon20" autocomplete="off" type="number" controls-position="right" :min="0" :max="1" :step="0.01"></el-input>
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
  name: "Model",
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
  methods: {
    load() {
      this.request.get("/model/page", {
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
      this.form.actualSalary=this.actualSalary
      this.request.post("/model", this.form).then(res => {
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
      this.request.delete("/model/" + id).then(res => {
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
      this.request.post("/model/del/batch", ids).then(res => {
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
      window.open("http://localhost:9090/model/export")
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
.headerBg {
  background: #eee !important;
}

.color-line {
  position: absolute;
  left: 0;
  top: 50%;
  width: 10px;
  height: 35px;
  background-color: #4682B4;
}
</style>