<template>
  <!--网站底部-->
  <div class="footer">


    <el-row :gutter="24" style="padding-top: 30px">
      <el-col :sm="8">
        <el-row :gutter="24" class="footer-title">
          <el-col :span="4" :offset="7">联系博主</el-col>
        </el-row>
        <el-row :gutter="24" class="footer-content">
          <el-col :sm="10" :offset="4">
            QQ：767299026<br>
            邮箱：767299026@qq.com
          </el-col>
          <el-col :sm="10">
            <el-image :src="src" style="width: 60px; height: 60px; margin-top: -25px"></el-image>
          </el-col>
        </el-row>
      </el-col>



      <el-col :sm="8">
        <el-row :gutter="24" class="footer-title">
          <el-col :span="24">签名</el-col>
        </el-row>
        <el-row :gutter="24" class="footer-content">
          <el-col :span="24" style="font-size: 15px">
            转身离开之前，别忘了说最后一次再见
          </el-col>
        </el-row>
      </el-col>
      <el-col :sm="8">
        <el-row :gutter="24" class="footer-title">
          <el-col :span="24">网站信息</el-col>
        </el-row>
        <el-row :gutter="24" class="footer-content">
          <el-col :span="24">
            {{'总访问量: '+pv}} <br>
            {{'访客量: '+uv}}
          </el-col>
        </el-row>
      </el-col>
    </el-row>

    <el-divider ></el-divider>

    <el-row :gutter="24">
      <el-col :sm="7">
        <el-row :gutter="24" class="footer-title">
          <el-col :span="24" />
        </el-row>
      </el-col>

      <el-col :sm="10">
        <el-row :gutter="24" style="height: 35px;font-size: 12px">
          <el-col :span="24">
            Copyright&nbsp;&copy;&nbsp;2021-2022&nbsp;YIQU's&nbsp;Blog.&nbsp;All&nbsp;rights&nbsp;reserved.&nbsp;
          </el-col>
        </el-row>
      </el-col>

      <el-col :sm="7" style="font-size: 8px">
        <el-row :gutter="24" >
          <el-col :span="24">
            <a href="//beian.miit.gov.cn" target="_blank" style="color: #b3b0b0;text-decoration:none">粤ICP备2022006544号</a>
          </el-col>
        </el-row>
      </el-col>

    </el-row>

  </div>
</template>

<script>
export default {
  name: "Footer",
  data() {
    return {
      pv: 0,
      uv: 0,
      input: "",
      src: 'https://cdn.xian-yun.xyz/static/blog/footer.webp',
    }
  },
  methods:{
    //获取pv和uv
    getPVAndUV(){
      const _this = this
      this.$axios.get('/visitornum').then(res => {
        _this.uv = res.data.data.uv
        _this.pv = res.data.data.pv
      })

    }
  },
  // 监听,当路由发生变化的时候执行
  watch: {
    '$route':'getPVAndUV'
  },
  created() {
    this.getPVAndUV()
  }
}
</script>

<style scoped>
.footer-title {
  font-size: 13px;
  font-weight: bold;
  margin-bottom: 5px;
}

.footer-content {
  font-size: 13px;
}

.footer {
  margin: 0 auto;
  text-align: center;
  min-height: 20px;
  max-width: 100%;
  padding: 0 15px;
}
</style>