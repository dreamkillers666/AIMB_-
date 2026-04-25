<template>


  <div >
    <!--    <p class="title1">智达AI：智能赋能，精准高效解决您的需求！</p>-->
    <el-row :gutter="20">
      <el-col :span="13">
        <el-input
            style="margin-left:20px;font-size: 18px;"
            type="textarea"
            placeholder="智达AI乐意为您解答困惑，请输入需要我解答的问题！"
            v-model="userInput"
            maxlength="500"
            show-word-limit
            :autosize="{ minRows: 2}"
            resize="none"
        />
      </el-col>
      <el-col :span="11">
        <el-button @click="start" type="primary" style="float: right;margin-left:20px;width:30%;font-size: 18px" :loading="loading">{{ loading? '智达AI正在为您解答':'提问' }}</el-button>
      </el-col>
    </el-row>

    <div>
      <div v-for="chatConten in finalChat">
        <p style="color:green;font-size: 18px;margin-left: 8px;">User：{{ chatConten.user }}</p><p style="color:green;font-size: 18px;margin-left: 8px;">智达AI：</p>
        <p style="color:red;font-size: 18px;margin-left: 8px;" v-html="chatConten.ai"></p> <!-- 渲染HTML内容 -->

      </div>
    </div>

  </div>
</template>

<script>
import CryptoJS from 'crypto-js';
import axios from 'axios';
import nlp from "compromise";
//2024.12.24

export default {
  name: "ai.vue",
  data() {
    return {
      appId: 'a15a3ef9',
      status: 'init',
      ttsWS: null,
      totalRes: '',
      userInput: '',
      aiContentRequest: '',
      finalChat: [],
      loading:false,
      landQueryRegEx: /(台风|热带气旋).*(登陆|登录).*(查询|情况|信息)?/,  // 匹配与台风登陆相关的查询
      currentTyphoonQueryRegEx: /(当前|正在发生|目前|此刻|此时此刻|现在).*(台风|热带气旋).*(查询|情况|信息)?/  // 匹配与当前台风相关的查询
    };
  },
  methods: {

    getWebsocketUrl() {
      return new Promise((resolve, reject) => {
        const apiKey = '82f8c1b4df5b89805b3abe54947e65f8';
        const apiSecret = 'OWQxNzIwYzg1ZGMxYmRhNGQzOTJiNDEx';
        // const url = 'wss://spark-api.xf-yun.com/v3.5/chat'; //这里使用的是星火大模型1.x版本
        const url = 'wss://spark-api.xf-yun.com/v3.5/chat';
        const host = window.location.host;
        const date = new Date().toGMTString();
        const algorithm = 'hmac-sha256';
        const headers = 'host date request-line';
        const signatureOrigin = `host: ${host}\ndate: ${date}\nGET /v3.5/chat HTTP/1.1`;
        const signatureSha = CryptoJS.HmacSHA256(signatureOrigin, apiSecret);
        const signature = CryptoJS.enc.Base64.stringify(signatureSha);
        const authorizationOrigin = `api_key="${apiKey}", algorithm="${algorithm}", headers="${headers}", signature="${signature}"`;
        const authorization = window.btoa(authorizationOrigin);
        const finalUrl = `${url}?authorization=${authorization}&date=${date}&host=${host}`;
        console.log(finalUrl)
        resolve(finalUrl);
      });
    },

    async start() {
      this.loading = true
      this.totalRes = '';
      this.aiContentRequest = ''
      // 使用 NLP 来解析用户输入
      const doc = nlp(this.userInput);

      //if (this.userInput.includes('台风登陆情况查询') || this.userInput.includes('当前台风查询')) {
      // 判断用户输入是否符合台风相关查询
      if (this.landQueryRegEx.test(this.userInput) || this.currentTyphoonQueryRegEx.test(this.userInput)) {
        //if (doc.has('台风')) {
        await this.getTyphoonData(); // 调用获取台风数据的逻辑
      } else {
        this.connectWebSocket(); // 调用 WebSocket 逻辑处理其他问题
      }
      this.loading = false;
    },
    // 20241224新增方法获取台风数据
    setStatus(status) {
      this.status = status;
    },
    connectWebSocket() {
      this.setStatus('ttsing');
      this.getWebsocketUrl().then((url) => {
        let ttsWS;
        if ('WebSocket' in window) {
          ttsWS = new WebSocket(url);
        } else if ('MozWebSocket' in window) {
          ttsWS = new MozWebSocket(url);
        } else {
          alert('浏览器不支持WebSocket');
          return;
        }
        this.ttsWS = ttsWS;
        ttsWS.onopen = (e) => {
          this.webSocketSend();
        };
        ttsWS.onmessage = (e) => {
          this.result(e.data);
        };
        ttsWS.onerror = (e) => {
          clearTimeout(this.playTimeout);
          this.setStatus('error');
          alert('WebSocket报错，请f12查看详情');
          console.error(`详情查看：${encodeURI(url.replace('wss:', 'https:'))}`);
        };
        ttsWS.onclose = (e) => {
          console.log(e);
        };
      });
    },
    webSocketSend() {
      let that = this
      const params = {
        header: {
          app_id: this.appId,
          uid: '随意',
        },
        parameter: {
          chat: {
            domain: 'generalv3.5',//如果是chat2这里也需要进行相应修改
            temperature: 0.5,
            max_tokens: 1024,
          },
        },
        payload: {
          message: {
            text: [
              {"role": "user", "content": that.userInput}
            ]
          },
        },
      };
      console.log(JSON.stringify(params));
      this.ttsWS.send(JSON.stringify(params));
    },
    requestHandle(requestData) {//处理request
      this.aiContentRequest = this.aiContentRequest + requestData.payload.choices.text[0].content
    },
    result(resultData) {
      let jsonData = JSON.parse(resultData);
      //console.log(jsonData)
      this.totalRes += resultData;
      //this.$refs.outputText.value = this.totalRes;
      //加入到ai回答中
      if(jsonData.header.status!==2){//不为结束就进行添加
        this.requestHandle(jsonData)
      }else {
        let contentSomething = {
          ai: this.aiContentRequest,
          user: this.userInput
        }
        this.finalChat.push(contentSomething)
        this.userInput = ''
        this.loading = false

      }
      if (jsonData.header.code !== 0) {
        alert(`提问失败: ${jsonData.header.code}:${jsonData.header.message}`);
        console.error(`${jsonData.header.code}:${jsonData.header.message}`);
        return;
      }
      if (jsonData.header.code === 0 && jsonData.header.status === 2) {
        this.ttsWS.close();
        this.setStatus('init');
      }
    },
  },
}
</script>


<style>

.title1 {
  padding: 2px;
  font-size: 18px;
  font-weight: 400;
  background-color: #3a8ee6cc;
  color: white;
  width: 98%;
  justify-content: center;
  display: flex;
}

.container {
  width: auto;
  height: 100%;
  /*margin-top: 25px;*/
  /*margin: 0 20px;*/
  /*padding-left: 60px;*/
  margin-left: 70px;
  line-height: 1.8;
  text-align: justify;
  /*text-align-last: justify;*/
  font-size: 20px;
  /*position: absolute;*/
  /*display: flex;*/
  z-index: 330;
  /*height: 100vh; !* 使用视口高度 *!*/
  position: absolute; /* 允许绝对定位的子元素正常工作 */
  background-color: white;
  text-indent: 2em; /* 首行缩进两个字符 */
  padding-right: 15px;
  padding-left: 15px;
  margin-top:30px;
}


</style>
