<template>
  <div>
    <el-row class="common-content" style="background-color: #dde7ee;height: 35px;margin-top: -12px">
      <el-row class="title">
        <div class="color-line" style="margin-top: -14px;margin-left: 1px"></div>
        <span style="margin-left: 25px; font-size: 1.3em;">员工管理</span>
      </el-row>
    </el-row>

    <el-card style="margin-top: 7px">
      <div style="margin: 10px 0">
        <el-input style="width: 200px" placeholder="请输入名称" suffix-icon="el-icon-search" v-model="name"></el-input>
        <el-input style="width: 200px" placeholder="请输入邮箱" suffix-icon="el-icon-message" class="ml-5"
                  v-model="email"></el-input>
        <el-input style="width: 200px" placeholder="请输入地址" suffix-icon="el-icon-position" class="ml-5"
                  v-model="address"></el-input>
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
            action="http://localhost:9090/emp/import"
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
        <el-table-column prop="id" label="员工编号"></el-table-column>
        <el-table-column prop="name" label="姓名"></el-table-column>
        <el-table-column prop="gender" label="性别"></el-table-column>
        <el-table-column prop="age" label="年龄"></el-table-column>
        <el-table-column prop="nation" label="民族"></el-table-column>
        <el-table-column prop="phone" label="电话"></el-table-column>
        <el-table-column prop="email" label="邮箱"></el-table-column>
        <el-table-column prop="address" label="地址"></el-table-column>
        <el-table-column prop="dept" label="部门"></el-table-column>
        <el-table-column prop="postion" label="职位"></el-table-column>
        <el-table-column prop="degree" label="学历"></el-table-column>
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

      <el-dialog title="员工信息" :visible.sync="dialogFormVisible" width="30%">
        <el-form label-width="80px" size="small">
          <el-form-item label="姓名">
            <el-input v-model="form.name" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="form.gender">
              <el-radio v-model="radio" label="男">男</el-radio>
              <el-radio v-model="radio" label="女">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="年龄">
            <el-input v-model="form.age" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="身份证号">
            <el-input v-model="form.idCard" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="民族">
            <el-input v-model="form.nation" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="form.email" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="电话">
            <el-input v-model="form.phone" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="地址">
            <el-input v-model="form.address" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="部门">
            <el-select v-model="form.dept" placeholder="请选择部门">
              <el-option label="行政部" value="行政部"></el-option>
              <el-option label="财务部" value="财务部"></el-option>
              <el-option label="销售部" value="销售部"></el-option>
              <el-option label="营运部" value="营运部"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="职位">
            <el-select v-model="form.postion" placeholder="请选择职位">
              <el-option label="普通员工" value="普通员工"></el-option>
              <el-option label="助理" value="助理"></el-option>
              <el-option label="总监" value="总监"></el-option>
              <el-option label="主管" value="主管"></el-option>
              <el-option label="经理" value="经理"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="学历">
            <el-select v-model="form.degree" placeholder="请选择学历">
              <el-option label="高中" value="高中"></el-option>
              <el-option label="大专" value="大专"></el-option>
              <el-option label="本科" value="本科"></el-option>
              <el-option label="研究生" value="研究生"></el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="入职日期">
            <el-date-picker
                v-model="form.edate"
                type="date"
                placeholder="请选择日期"
                value-format="yyyy-MM-dd"
                clearable
                style="width: 100%;"
            />
<!--            <el-input v-model="form.edate" placeholder="2000-12-20" autocomplete="off"></el-input>-->
          </el-form-item>
          <el-form-item label="在职状态">
            <el-radio-group v-model="form.workState">
              <el-radio v-model="radio" label="在职">在职</el-radio>
              <el-radio v-model="radio" label="离职">离职</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="工号">
            <el-input v-model="form.workId" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="转正日期">
<!--            <el-input v-model="form.conversionTime" placeholder="2000-12-20" autocomplete="off">  </el-input>-->
            <el-date-picker
                v-model="form.conversionTime"
                type="date"
                placeholder="请选择日期"
                value-format="yyyy-MM-dd"
                clearable
                style="width: 100%;"
            />
          </el-form-item>
          <el-form-item label="工龄">
            <el-input v-model="form.workAge" autocomplete="off"></el-input>
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
  name: "Emp",
  data() {
    return {
      tableData: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      name: "",
      email: "",
      address: "",
      form: {},
      dialogFormVisible: false,
      multipleSelection: [],

      radio: '男',
      edu: '本科'
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      this.request.get("/emp/page", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.name,
          email: this.email,
          address: this.address,
        }
      }).then(res => {
        // 注意data
        this.tableData = res.data.records
        this.total = res.data.total

      })
    },
    save() {
      this.request.post("/emp", this.form).then(res => {
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
      this.request.delete("/emp/" + id).then(res => {
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
      this.request.post("/emp/del/batch", ids).then(res => {
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
      this.email = ""
      this.address = ""
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
      window.open("http://localhost:9090/emp/export")
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
  background-color:#4682B4;
}
</style>